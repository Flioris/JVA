package net.flioris.jva;

import net.flioris.jva.action.document.GetDocumentUploadServerAction;
import net.flioris.jva.action.document.SaveDocumentAction;
import net.flioris.jva.action.document.UploadDocumentAction;
import net.flioris.jva.action.message.GetConversationByIdAction;
import net.flioris.jva.action.message.GetConversationsByIdAction;
import net.flioris.jva.action.message.SendMessageAction;
import net.flioris.jva.action.message.SendMessagesAction;
import net.flioris.jva.action.photo.GetPhotoUploadServerAction;
import net.flioris.jva.action.photo.SavePhotoAction;
import net.flioris.jva.action.photo.UploadPhotoAction;
import net.flioris.jva.action.user.GetUserByIdAction;
import net.flioris.jva.action.wall.PostAction;
import net.flioris.jva.event.chat.CommandEvent;
import net.flioris.jva.event.chat.MessageEvent;
import net.flioris.jva.models.Message;
import net.flioris.jva.event.EventListener;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Provides methods to execute API requests and handles events.
 */
public class JVA {
    private EventListener[] botListener = {};
    private final String ACCESS_TOKEN;
    private final String GROUP_ID;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Logger LOGGER = LoggerFactory.getLogger("JVA");
    private static final String VK_API_VERSION = "5.199";
    private static final int RECONNECT_DELAY_SECONDS = 8;
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * Creates a new instance of the JVA bot with the specified access token and group ID.
     *
     * @param accessToken
     *        The access token used to authenticate API requests.
     * @param groupId
     *        The ID of the group the bot belongs to.
     */
    public JVA(String accessToken, String groupId) {
        ACCESS_TOKEN = accessToken;
        GROUP_ID = groupId;
        JSONObject longPollServer = getLongPollServer();

        connectToLongPollServer(longPollServer);
        LOGGER.info("VK bot is running.");
    }

    /**
     * Sets the event listeners that will handle incoming bot events.
     *
     * @param  listeners
     *         The event listeners to be registered
     */
    public void setListeners(EventListener... listeners) {
        botListener = listeners;
    }

    private HttpUrl.Builder getBaseUrlBuilder(String method) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host("api.vk.com")
                .addPathSegment("method")
                .addPathSegment(method)
                .addQueryParameter("access_token", ACCESS_TOKEN)
                .addQueryParameter("v", VK_API_VERSION);
    }

    private JSONObject getLongPollServer() {
        HttpUrl url = getBaseUrlBuilder("groups.getLongPollServer")
                .addQueryParameter("group_id", GROUP_ID)
                .build();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            JSONObject json = new JSONObject(response.body().string());
            if (response.isSuccessful() && json.has("response")) {
                return json.getJSONObject("response");
            } else {
                LOGGER.error("Error getting server.");
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("Error getting server: {}", e.getMessage());
            return null;
        }
    }

    private void connectToLongPollServer(JSONObject longPollServer) {
        if (longPollServer == null) {
            reconnectWithDelay();
            return;
        }

        HttpUrl longPollUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("lp.vk.com")
                .addPathSegments("whp/" + GROUP_ID)
                .addQueryParameter("act", "a_check")
                .addQueryParameter("key", longPollServer.getString("key"))
                .addQueryParameter("ts", longPollServer.getString("ts"))
                .addQueryParameter("wait", "25")
                .build();
        Request request = new Request.Builder().url(longPollUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOGGER.error("Error connecting to Long Poll server: {}", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    int newTs;
                    if (jsonResponse.has("ts")) {
                        newTs = jsonResponse.getInt("ts");
                        JSONArray updates = jsonResponse.getJSONArray("updates");
                        for (int i = 0; i < updates.length(); i++) {
                            onUpdateReceived(updates.getJSONObject(i));
                        }
                        longPollServer.put("ts", newTs);
                        connectToLongPollServer(longPollServer);
                    } else {
                        reconnectWithDelay();
                    }
                } else {
                    LOGGER.error("Error receiving events: {}", response.code());
                }
            }
        });
    }

    private void reconnectWithDelay() {
        scheduler.schedule(() -> {
            JSONObject longPollServer = getLongPollServer();
            connectToLongPollServer(longPollServer);
        }, RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void onUpdateReceived(JSONObject update) {
        if (update.getString("type").equals("message_new")) {
            Message message = Message.fromJSON(update.getJSONObject("object").getJSONObject("message"));
            String text = message.getText();

            if (text.startsWith("/")) {
                String[] command = text.substring(1, text.length()).split(" ");
                CommandEvent event = new CommandEvent(message, command[0], command, update);
                for (EventListener listener : botListener) {
                    listener.onCommand(event);
                }
            } else {
                MessageEvent event = new MessageEvent(message, update);
                for (EventListener listener : botListener) {
                    listener.onMessage(event);
                }
            }

        }
    }

    /**
     * Gets a photos upload server.
     *
     * @param  peerId
     *         The ID of the user or conversation to which you want to upload a photo.
     *
     * @return Photos upload server.
     */
    public GetPhotoUploadServerAction getPhotoUploadServer(int peerId) {
        okhttp3.HttpUrl.Builder builder = getBaseUrlBuilder("photos.getMessagesUploadServer")
                .addQueryParameter("peer_id", String.valueOf(peerId));

        return new GetPhotoUploadServerAction(client, builder);
    }

    /**
     * Gets a documents upload server (currently used for uploading group photos).
     *
     * @return Documents upload server.
     */
    public GetDocumentUploadServerAction getDocumentUploadServer() {
        okhttp3.HttpUrl.Builder builder = getBaseUrlBuilder("docs.getWallUploadServer")
                .addQueryParameter("group_id", GROUP_ID);

        return new GetDocumentUploadServerAction(client, builder);
    }

    /**
     * Uploads a photo.
     *
     * @param  uploadUrl
     *         Just the response of the getPhotosUploadServer method.
     * @param  photo
     *         The photo file to upload.
     *
     * @return uploadPhotoResponse.
     */
    public UploadPhotoAction uploadPhoto(String uploadUrl, File photo) {
        okhttp3.MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("photo", photo.getName(),
                        RequestBody.create(photo, MediaType.parse("image/png")));

        return new UploadPhotoAction(client, builder, uploadUrl);
    }

    /**
     * Uploads a document (currently used for uploading group photos).
     *
     * @param  uploadUrl
     *         Just the response of the getDocsUploadServer method.
     * @param  photo
     *         The photo file to upload.
     *
     * @return uploadDocumentResponse.
     */
    public UploadDocumentAction uploadDocument(String uploadUrl, File photo) {
        okhttp3.MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", photo.getName(),
                        RequestBody.create(photo, MediaType.parse("image/png")));

        return new UploadDocumentAction(client, builder, uploadUrl);
    }

    /**
     * Saves a photo.
     *
     * @param  uploadResponse
     *         Just the response of the uploadPhoto method.
     *
     * @return savePhotoResponse.
     */
    public SavePhotoAction savePhoto(JSONObject uploadResponse) {
        HttpUrl.Builder builder = getBaseUrlBuilder("photos.saveMessagesPhoto")
                .addQueryParameter("photo", uploadResponse.getString("photo"))
                .addQueryParameter("server", String.valueOf(uploadResponse.getInt("server")))
                .addQueryParameter("hash", uploadResponse.getString("hash"));

        return new SavePhotoAction(client, builder);
    }

    /**
     * Saves a document (currently used for uploading group photos).
     *
     * @param  uploadResponse
     *         Just the response of the uploadDocument method.
     *
     * @return saveDocumentResponse.
     */
    public SaveDocumentAction saveDocument(JSONObject uploadResponse) {
        HttpUrl.Builder builder = getBaseUrlBuilder("docs.save")
                .addQueryParameter("file", uploadResponse.getString("file"))
                .addQueryParameter("group_id", GROUP_ID);

        return new SaveDocumentAction(client, builder);
    }

    /**
     * Sends a message to the user or conversation.
     *
     * @param  targetId
     *         The ID of the user or conversation to which you want to post a message with text and a photo.
     *
     * @return The ID of the message that was sent.
     */
    public SendMessageAction send(int targetId) {
        HttpUrl.Builder builder = getBaseUrlBuilder("messages.send")
                .addQueryParameter(targetId < 2000000000 ? "user_id" : "peer_id", String.valueOf(targetId))
                .addQueryParameter("random_id", String.valueOf(System.currentTimeMillis()));

        return new SendMessageAction(client, builder);
    }

    /**
     * Sends a messages to users OR conversations. Do not include both users and conversations. If the bot is unable to send any message, no messages will be sent and an empty list will be returned to you.
     *
     * @param  targetIds
     *         User IDs OR Conversations IDs where you want to send the message.
     *
     * @return А list of message IDs.
     */
    public SendMessagesAction send(int... targetIds) {
        String userIds = Arrays.stream(targetIds)
                .filter(id -> id < 2000000000)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
        String peerIds = Arrays.stream(targetIds)
                .filter(id -> id > 2000000000)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));

        HttpUrl.Builder builder = getBaseUrlBuilder("messages.send")
                .addQueryParameter("random_id", String.valueOf(System.currentTimeMillis()));

        if (!peerIds.isEmpty()) {
            builder.addQueryParameter("peer_ids", peerIds);
        } else if (!userIds.isEmpty()) {
            builder.addQueryParameter("user_ids", userIds);
        }

        return new SendMessagesAction(client, builder);
    }

    /**
     * Publishes a post on the wall of a bot group.
     *
     * @return The ID of the message that was sent.
     */
    public PostAction post() {
        HttpUrl.Builder builder = getBaseUrlBuilder("wall.post")
                .addQueryParameter("owner_id", "-" + GROUP_ID);

        return new PostAction(client, builder);
    }

    /**
     * Returns the Conversation by its ID.
     *
     * @param  conversationId
     *         The ID of the Conversation you want to receive. To get group chats use -1, -2 etc.
     *
     * @return The Conversation with this ID. May return null.
     */
    public GetConversationByIdAction getConversationById(String conversationId) {
        HttpUrl.Builder builder = getBaseUrlBuilder("messages.getConversationsById")
                .addQueryParameter("peer_ids", String.valueOf(conversationId));

        return new GetConversationByIdAction(client, builder);
    }

    /**
     * Returns a List of Conversation by their ID. May return an empty list if at least 1 conversation is not found.
     *
     * @param  conversationIds
     *         IDs of the Conversations you want to receive. To get group chats use -1, -2 etc.
     *
     * @return A List of Conversation.
     */
    public GetConversationsByIdAction getConversationsById(String... conversationIds) {
        HttpUrl.Builder builder = getBaseUrlBuilder("messages.getConversationsById")
                .addQueryParameter("peer_ids", String.join(",", conversationIds));

        return new GetConversationsByIdAction(client, builder);
    }

    /**
     * Returns the User by its ID.
     *
     * @param  userId
     *         The ID of the User you want to receive.
     *
     * @return The User with this ID. May return null.
     */
    public GetUserByIdAction getUserById(String userId) {
        HttpUrl.Builder builder = getBaseUrlBuilder("users.get")
                .addQueryParameter("user_ids", String.valueOf(userId));

        return new GetUserByIdAction(client, builder);
    }
}
