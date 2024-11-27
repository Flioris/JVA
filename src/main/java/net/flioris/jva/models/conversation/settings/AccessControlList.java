package net.flioris.jva.models.conversation.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONObject;

@Getter
@AllArgsConstructor
@ToString
public class AccessControlList {
    private boolean canChangeInviteLink;
    private boolean canInvite;
    private boolean canSeeInviteLink;
    private boolean canCall;
    private boolean canSendReactions;
    private boolean canChangeInfo;
    private boolean canCopyChat;
    private boolean canChangeStyle;
    private boolean canChangePin;
    private boolean canModerate;
    private boolean canUseMassMentions;
    private boolean canPromoteUsers;

    public static AccessControlList fromJSON(JSONObject rawAcl) {
        return new AccessControlList(rawAcl.getBoolean("can_change_invite_link"), rawAcl.getBoolean("can_invite"),
                rawAcl.getBoolean("can_see_invite_link"), rawAcl.getBoolean("can_call"),
                rawAcl.getBoolean("can_send_reactions"), rawAcl.getBoolean("can_change_info"),
                rawAcl.getBoolean("can_copy_chat"), rawAcl.getBoolean("can_change_style"),
                rawAcl.getBoolean("can_change_pin"), rawAcl.getBoolean("can_moderate"),
                rawAcl.getBoolean("can_use_mass_mentions"), rawAcl.getBoolean("can_promote_users"));
    }
}
