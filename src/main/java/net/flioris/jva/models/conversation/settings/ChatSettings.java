package net.flioris.jva.models.conversation.settings;

import net.flioris.jva.models.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class ChatSettings {
    @Nullable
    private final Boolean isService;
    @Nullable
    private final Boolean isDisappearing;
    @Nullable
    private final Integer ownerId;
    @Nullable
    private final String description;
    @Nullable
    private final Integer pinnedMessagesCount;
    private final List<Integer> adminIds;
    private final int membersCount;
    private final String title;
    private final Message pinnedMessage;
    private final String state;
    private final List<Integer> activeIds;
    private final boolean isGroupChannel;
    @Nullable
    private final Photo photo;
    @Nullable
    private final Permissions permissions;
    private final AccessControlList accessControlList;

    public static ChatSettings fromJSON(JSONObject rawChatSettings) {
        JSONArray activeArray = rawChatSettings.getJSONArray("active_ids");
        List<Integer> activeIds = new ArrayList<>();

        for (int i = 0; i < activeArray.length(); i++) {
            activeIds.add(activeArray.getInt(i));
        }

        JSONArray adminArray = rawChatSettings.getJSONArray("admin_ids");
        List<Integer> adminIds = new ArrayList<>();

        for (int i = 0; i < adminArray.length(); i++) {
            adminIds.add(adminArray.getInt(i));
        }

        return new ChatSettings(rawChatSettings.has("is_service") ? rawChatSettings.getBoolean("is_service") : null,
                rawChatSettings.has("is_disappearing") ? rawChatSettings.getBoolean("is_disappearing") : null,
                rawChatSettings.has("owner_id") ? rawChatSettings.getInt("owner_id") : null,
                rawChatSettings.has("description") ? rawChatSettings.getString("description") : null,
                rawChatSettings.has("pinned_messages_count") ? rawChatSettings.getInt("pinned_messages_count") : null,
                adminIds, rawChatSettings.getInt("members_count"), rawChatSettings.getString("title"),
                rawChatSettings.has("pinned_message") ? Message.fromJSON(rawChatSettings.getJSONObject("pinned_message")) : null,
                rawChatSettings.getString("state"),
                activeIds,
                rawChatSettings.getBoolean("is_group_channel"),
                rawChatSettings.has("photo") ? Photo.fromJSON(rawChatSettings.getJSONObject("photo")) : null,
                rawChatSettings.has("permissions") ? Permissions.fromJSON(rawChatSettings.getJSONObject("permissions")) : null,
                AccessControlList.fromJSON(rawChatSettings.getJSONObject("acl")));
    }
}
