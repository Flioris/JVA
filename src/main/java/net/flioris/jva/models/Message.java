package net.flioris.jva.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class Message {
    private final int date;
    private final int authorId;
    private final List<String> attachments;
    @Nullable
    private final Boolean hidden;
    @Nullable
    private final Integer version;
    @Nullable
    private final Boolean unavailable;
    @Nullable
    private final Integer out;
    private final int conversationMessageId;
    private final int peerId;
    @Nullable
    private final Boolean important;
    private final List<Message> forwardedMessages;
    private final int id;
    private final String text;
    @Nullable
    private final Integer randomId;
    @Nullable
    private final Message replyMessage;

    public static Message fromJSON(JSONObject rawMessage) {
        List<String> attachmentURLs = new ArrayList<>();

        if (rawMessage.has("attachments")) {
            JSONArray attachmentArray = rawMessage.getJSONArray("attachments");
            for (int i = 0; i < attachmentArray.length(); i++) {
                JSONObject attachment = attachmentArray.getJSONObject(i);
                if (attachment.getString("type").equals("photo")) {
                    JSONObject photo = attachment.getJSONObject("photo");
                    JSONArray sizes = photo.getJSONArray("sizes");
                    String largestUrl = null;
                    int maxWidth = 0;
                    int maxHeight = 0;
                    for (int j = 0; j < sizes.length(); j++) {
                        JSONObject size = sizes.getJSONObject(j);
                        int width = size.getInt("width");
                        int height = size.getInt("height");
                        String url = size.getString("url");
                        if (width > maxWidth && height > maxHeight) {
                            maxWidth = width;
                            maxHeight = height;
                            largestUrl = url;
                        }
                    }
                    if (largestUrl != null) {
                        attachmentURLs.add(largestUrl);
                    }
                }
            }
        }

        List<Message> forwardedMessages = new ArrayList<>();

        if (rawMessage.has("fwd_messages")) {
            JSONArray forwardedMessageArray = rawMessage.getJSONArray("fwd_messages");
            for (int i = 0; i < forwardedMessageArray.length(); i++) {
                JSONObject forwardedMessage = forwardedMessageArray.getJSONObject(i);
                forwardedMessages.add(fromJSON(forwardedMessage));
            }
        }

        return new Message(rawMessage.getInt("date"), rawMessage.getInt("from_id"), attachmentURLs,
                rawMessage.has("is_hidden") ? rawMessage.getBoolean("is_hidden") : null,
                rawMessage.has("version") ? rawMessage.getInt("version") : null,
                rawMessage.has("is_unavailable") ? rawMessage.getBoolean("is_unavailable") : null,
                rawMessage.has("out") ? rawMessage.getInt("out") : null, rawMessage.getInt("conversation_message_id"),
                rawMessage.getInt("peer_id"), rawMessage.has("important") ? rawMessage.getBoolean("important") : null,
                forwardedMessages, rawMessage.getInt("id"), rawMessage.getString("text"),
                rawMessage.has("random_id") ? rawMessage.getInt("random_id") : null,
                rawMessage.has("reply_message") ? fromJSON(rawMessage.getJSONObject("reply_message")) : null);
    }
}