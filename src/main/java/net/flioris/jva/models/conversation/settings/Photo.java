package net.flioris.jva.models.conversation.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONObject;

@Getter
@AllArgsConstructor
@ToString
public class Photo {
    private final String photo50;
    private final String photo100;
    private final String photo200;
    private final boolean defaultPhoto;
    private final boolean defaultCallPhoto;

    public static Photo fromJSON(JSONObject rawPhoto) {
        return new Photo(rawPhoto.getString("photo_50"), rawPhoto.getString("photo_100"),
                rawPhoto.getString("photo_200"), rawPhoto.getBoolean("is_default_photo"),
                rawPhoto.getBoolean("is_default_call_photo"));
    }
}
