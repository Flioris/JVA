package net.flioris.jva.models.conversation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.flioris.jva.models.conversation.settings.ChatSettings;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

@Getter
@AllArgsConstructor
@ToString
public class Conversation {
    @Nullable
    private final Integer peerFlags;
    private final Peer peer;
    @Nullable
    private final Integer inReadCmid;
    @Nullable
    private final Integer inRead;
    @Nullable
    private final Integer outReadCmid;
    @Nullable
    private final Integer outRead;
    @Nullable
    private final Boolean isMarkedUnread;
    @Nullable
    private final Boolean important;
    @Nullable
    private final Boolean unanswered;
    private final CanWrite canWrite;
    private final ChatSettings chatSettings;
    @Nullable
    private final SortId sortId;
    @Nullable
    private final Integer lastMessageId;
    @Nullable
    private final Integer lastConversationMessageId;
    @Nullable
    private final Integer version;
    private final JSONObject raw;

    public static Conversation fromJSON(JSONObject rawConversation) {
        return new Conversation(rawConversation.has("peer_flags") ? rawConversation.getInt("peer_flags") : null,
                Peer.fromJSON(rawConversation.getJSONObject("peer")),
                rawConversation.has("in_read_cmid") ? rawConversation.getInt("in_read_cmid") : null,
                rawConversation.has("in_read") ? rawConversation.getInt("in_read") : null,
                rawConversation.has("out_read_cmid") ? rawConversation.getInt("out_read_cmid") : null,
                rawConversation.has("out_read") ? rawConversation.getInt("out_read") : null,
                rawConversation.has("is_marked_unread") ? rawConversation.getBoolean("is_marked_unread") : null,
                rawConversation.has("important") ? rawConversation.getBoolean("important") : null,
                rawConversation.has("unanswered") ? rawConversation.getBoolean("unanswered") : null,
                CanWrite.fromJSON(rawConversation.getJSONObject("can_write")),
                ChatSettings.fromJSON(rawConversation.getJSONObject("chat_settings")),
                rawConversation.has("sort_id") ? SortId.fromJSON(rawConversation.getJSONObject("sort_id")) : null,
                rawConversation.has("last_message_id") ? rawConversation.getInt("last_message_id") : null,
                rawConversation.has("last_conversation_message_id") ? rawConversation.getInt("last_conversation_message_id") : null,
                rawConversation.has("version") ? rawConversation.getInt("version") : null,
                rawConversation);
    }
}
