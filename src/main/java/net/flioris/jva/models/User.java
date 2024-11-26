package net.flioris.jva.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;

@Getter
@AllArgsConstructor
public class User {
    private final int id;
    private final String firstName;
    private final String lastName;
    private final boolean canAccessClosed;
    private final boolean isClosed;

    public static User fromJSON(JSONObject rawUser) {
        return new User(rawUser.getInt("id"), rawUser.getString("first_name"), rawUser.getString("last_name"),
                rawUser.getBoolean("can_access_closed"), rawUser.getBoolean("is_closed"));
    }
}
