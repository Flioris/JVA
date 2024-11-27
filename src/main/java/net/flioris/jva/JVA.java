package net.flioris.jva;

import net.flioris.jva.event.chat.CommandEvent;
import net.flioris.jva.event.chat.MessageEvent;
import net.flioris.jva.models.Message;
import net.flioris.jva.event.EventListener;
import net.flioris.jva.models.User;
import net.flioris.jva.models.conversation.Conversation;
import net.flioris.jva.util.RestAction;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
                    String newTs;
                    if (jsonResponse.has("ts")) {
                        newTs = jsonResponse.getString("ts");
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
    public RestAction<String> getPhotosUploadServer(int peerId) {
        HttpUrl url = getBaseUrlBuilder("photos.getMessagesUploadServer")
                .addQueryParameter("peer_id", String.valueOf(peerId))
                .build();
        Request request = new Request.Builder().url(url).build();

        return new RestAction<>(client.newCall(request), response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    return json.has("response") ? json.getJSONObject("response").getString("upload_url") : null;
                }
                return null;
            }
        });
    }

    /**
     * Gets a documents upload server (currently used for uploading group photos).
     *
     * @return Documents upload server.
     */
    public RestAction<String> getDocsUploadServer() {
        HttpUrl url = getBaseUrlBuilder("docs.getWallUploadServer")
                .addQueryParameter("group_id", GROUP_ID)
                .build();
        Request request = new Request.Builder().url(url).build();

        return new RestAction<>(client.newCall(request), response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    return json.has("response") ? json.getJSONObject("response").getString("upload_url") : null;
                }
                return null;
            }
        });
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
    public RestAction<JSONObject> uploadPhoto(String uploadUrl, File photo) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("photo", photo.getName(),
                        RequestBody.create(photo, MediaType.parse("image/png")))
                .build();
        Request request = new Request.Builder().url(uploadUrl).post(requestBody).build();

        return new RestAction<>(client.newCall(request), response -> {
            try (response) {
                if (response.isSuccessful()) {
                    return new JSONObject(response.body().string());
                }
                return null;
            }
        });
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
    public RestAction<JSONObject> uploadDocument(String uploadUrl, File photo) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", photo.getName(),
                        RequestBody.create(photo, MediaType.parse("image/png")))
                .build();
        Request request = new Request.Builder().url(uploadUrl).post(requestBody).build();

        return new RestAction<>(client.newCall(request), response -> {
            try (response) {
                if (response.isSuccessful()) {
                    return new JSONObject(response.body().string());
                }
                return null;
            }
        });
    }

    /**
     * Saves a photo.
     *
     * @param  uploadResponse
     *         Just the response of the uploadPhoto method.
     *
     * @return savePhotoResponse.
     */
    public RestAction<JSONObject> savePhoto(JSONObject uploadResponse) {
        if (uploadResponse == null || !uploadResponse.has("photo")) {
            return null;
        }

        HttpUrl url = getBaseUrlBuilder("photos.saveMessagesPhoto")
                .addQueryParameter("photo", uploadResponse.getString("photo"))
                .addQueryParameter("server", String.valueOf(uploadResponse.getInt("server")))
                .addQueryParameter("hash", uploadResponse.getString("hash"))
                .build();
        Request request = new Request.Builder().url(url).build();

        return new RestAction<>(client.newCall(request), response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    return json.has("response") ? json.getJSONArray("response").getJSONObject(0) : null;
                }
                return null;
            }
        });
    }

    /**
     * Saves a document (currently used for uploading group photos).
     *
     * @param  uploadResponse
     *         Just the response of the uploadDocument method.
     *
     * @return saveDocumentResponse.
     */
    public RestAction<JSONObject> saveDocument(JSONObject uploadResponse) {
        HttpUrl url = getBaseUrlBuilder("docs.save")
                .addQueryParameter("file", uploadResponse.getString("file"))
                .addQueryParameter("group_id", GROUP_ID)
                .build();
        Request request = new Request.Builder().url(url).build();

        return new RestAction<>(client.newCall(request), response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    return json.has("response") ? json.getJSONObject("response").getJSONObject("doc") : null;
                }
                return null;
            }
        });
    }

    /**
     * Sends a message with text and a photo in a private message to a user or conversation.
     *
     * @param  id
     *         The ID of the user or conversation to which you want to post a message with text and a photo.
     * @param  message
     *         The text that will be attached.
     * @param  photo
     *         The photo that will be attached to the message.
     */
    public void send(int id, String message, File photo) {
        getPhotosUploadServer(id).queue(uploadUrl ->
                uploadPhoto(uploadUrl, photo).queue(uploadResponse ->
                        savePhoto(uploadResponse).queue(savePhotoResponse ->
                            send(id, message, savePhotoResponse))));
    }

    /**
     * Publishes a post with a photo and text on the wall of a bot group.
     *
     * @param  message
     *         The text that will be attached.
     * @param  photo
     *         The photo that will be attached.
     */
    public void post(String message, File photo) {
        getDocsUploadServer().queue(uploadUrl ->
                uploadDocument(uploadUrl, photo).queue(uploadResponse ->
                        saveDocument(uploadResponse).queue(saveDocResponse ->
                                post(message, saveDocResponse))));
    }

    /**
     *Publishes a post with a document and text on the wall of a bot group (currently used for uploading group photos).
     *
     * @param  message
     *         The text that will be attached.
     * @param  saveDocResponse
     *         Just the response of the saveDocument method.
     */
    public void post(String message, JSONObject saveDocResponse) {
        String attachment = "doc" + saveDocResponse.getInt("owner_id") + "_" + saveDocResponse.getInt("id");
        HttpUrl url = getBaseUrlBuilder("wall.post")
                .addQueryParameter("owner_id", "-" + GROUP_ID)
                .addQueryParameter("message", message)
                .addQueryParameter("attachments", attachment)
                .build();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOGGER.error("Error publishing post: {}", e.getMessage(), e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    LOGGER.error("Error publishing post: {}", response.code());
                }
                response.close();
            }
        });
    }

    /**
     * Sends a message in a private message to a user or conversation.
     *
     * @param  id
     *         The ID of the user or conversation to which you want to post to.
     * @param  message
     *         The text that will be attached.
     */
    public void send(int id, String message) {
        HttpUrl url = getBaseUrlBuilder("messages.send")
                .addQueryParameter(id < 200000000 ? "user_id" : "peer_id", String.valueOf(id))
                .addQueryParameter("message", message)
                .addQueryParameter("random_id", String.valueOf(System.currentTimeMillis()))
                .build();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOGGER.error("Error sending message: {}", e.getMessage(), e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    LOGGER.error("Error sending message: {}", response.code());
                }
                response.close();
            }
        });
    }

    /**
     * Sends a message with text and a photo in a private message to a user or conversation.
     *
     * @param  id
     *         The ID of the user or conversation to which you want to post a message with text and an image.
     * @param  message
     *         The text that will be attached.
     * @param  savePhotoResponse
     *         Just the response of the savePhoto method.
     */
    public void send(int id, String message, JSONObject savePhotoResponse) {
        if (savePhotoResponse == null) {
            return;
        }

        String attachment = "photo" + savePhotoResponse.getInt("owner_id") + "_" + savePhotoResponse.getInt("id") + "_"
                + savePhotoResponse.getString("access_key");
        HttpUrl url = getBaseUrlBuilder("messages.send")
                .addQueryParameter(id < 200000000 ? "user_id" : "peer_id", String.valueOf(id))
                .addQueryParameter("message", message)
                .addQueryParameter("attachment", attachment)
                .addQueryParameter("random_id", String.valueOf(System.currentTimeMillis()))
                .build();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOGGER.error("Error sending message with photo: {}", e.getMessage(), e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    LOGGER.error("Error sending message with photo: {}", response.code());
                }
                response.close();
            }
        });
    }

    /**
     *Sends a response to a message in a private message to a user or to a conversation.
     *
     * @param  id
     *         The ID of the user or conversation to which you want to post a reply.
     * @param  messageId
     *         The ID of the message you want to reply to.
     * @param  message
     *         The text you want to reply with.
     */
    public void reply(int id, String messageId, String message) {
        HttpUrl url = getBaseUrlBuilder("messages.send")
                .addQueryParameter(id < 200000000 ? "user_id" : "peer_id", String.valueOf(id))
                .addQueryParameter("reply_to", messageId)
                .addQueryParameter("message", message)
                .addQueryParameter("random_id", String.valueOf(System.currentTimeMillis()))
                .build();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LOGGER.error("Error sending response: {}", e.getMessage(), e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    LOGGER.error("Error sending response: {}", response.code());
                }
                response.close();
            }
        });
    }

    /**
     * Returns the Conversation by its ID.
     *
     * @param  conversationId
     *         The ID of the Conversation you want to receive.
     *
     * @return The Conversation with this ID. May return null.
     */
    public RestAction<Conversation> getConversationById(String conversationId) {
        HttpUrl url = getBaseUrlBuilder("messages.getConversationsById")
                .addQueryParameter("peer_ids", String.valueOf(conversationId))
                .build();
        Request request = new Request.Builder().url(url).build();

        return new RestAction<>(client.newCall(request), response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    return json.has("response") ? Conversation.fromJSON(json.getJSONObject("response")
                            .getJSONArray("items").getJSONObject(0)) : null;
                }
                return null;
            }
        });
    }

    /**
     * Returns the User by its ID.
     *
     * @param  userId
     *         The ID of the User you want to receive.
     *
     * @return The User with this ID. May return null.
     */
    public RestAction<User> getUserById(String userId) {
        HttpUrl url = getBaseUrlBuilder("users.get")
                .addQueryParameter("user_ids", String.valueOf(userId))
                .build();
        Request request = new Request.Builder().url(url).build();

        return new RestAction<>(client.newCall(request), response -> {
            try (response) {
                if (response.isSuccessful()) {
                    JSONObject json = new JSONObject(response.body().string());
                    return json.has("response") ? User.fromJSON(json.getJSONArray("response").getJSONObject(0)) : null;
                }
                return null;
            }
        });
    }
}
