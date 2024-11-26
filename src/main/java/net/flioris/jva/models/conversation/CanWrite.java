package net.flioris.jva.models.conversation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

@Getter
@AllArgsConstructor
@ToString
public class CanWrite {
    private final boolean allowed;
    @Nullable
    private final Integer reason;

    public static CanWrite fromJSON(JSONObject rawCanWrite) {
        return new CanWrite(rawCanWrite.getBoolean("allowed"),
                rawCanWrite.has("reason") ? rawCanWrite.getInt("reason") : null);
    }
}
