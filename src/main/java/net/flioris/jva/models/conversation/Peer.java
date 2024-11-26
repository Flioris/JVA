package net.flioris.jva.models.conversation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONObject;

@Getter
@AllArgsConstructor
@ToString
public class Peer {
    private final int id;
    private final String type;
    private final int localId;

    public static Peer fromJSON(JSONObject rawPeer) {
        return new Peer(rawPeer.getInt("id"), rawPeer.getString("type"), rawPeer.getInt("local_id"));
    }
}
