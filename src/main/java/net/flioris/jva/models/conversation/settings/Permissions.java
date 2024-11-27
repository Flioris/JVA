package net.flioris.jva.models.conversation.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONObject;

@Getter
@AllArgsConstructor
@ToString
public class Permissions {
    private final String changeInfo;
    private final String call;
    private final String useMassMentions;
    private final String changePin;
    private final String changeAdmins;
    private final String invite;
    private final String seeInviteLink;
    private final String changeStyle;

    public static Permissions fromJSON(JSONObject rawPermissions) {
        return new Permissions(rawPermissions.getString("change_info"), rawPermissions.getString("call"),
                rawPermissions.getString("use_mass_mentions"), rawPermissions.getString("change_pin"),
                rawPermissions.getString("change_admins"), rawPermissions.getString("invite"),
                rawPermissions.getString("see_invite_link"), rawPermissions.getString("change_style"));
    }
}
