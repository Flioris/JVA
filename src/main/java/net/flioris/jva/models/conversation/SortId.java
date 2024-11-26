package net.flioris.jva.models.conversation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONObject;

@Getter
@AllArgsConstructor
@ToString
public class SortId {
    private final int minorId;
    private final int majorId;

    public static SortId fromJSON(JSONObject rawSortId) {
        return new SortId(rawSortId.getInt("minor_id"), rawSortId.getInt("major_id"));
    }
}
