package net.flioris.jva.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONObject;

@Getter
@AllArgsConstructor
@ToString
public class User {
    private final int id;
    private final String firstName;
    private final String lastName;
    private final boolean canAccessClosed;
    private final boolean isClosed;
    private final JSONObject raw;

    public static User fromJSON(JSONObject rawUser) {
        return new User(rawUser.getInt("id"), rawUser.getString("first_name"), rawUser.getString("last_name"),
                rawUser.getBoolean("can_access_closed"), rawUser.getBoolean("is_closed"), rawUser);
    }
}
