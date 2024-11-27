package net.flioris.jva.event.chat;

import net.flioris.jva.models.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;

@Getter
@AllArgsConstructor
public class MessageEvent {
    private final Message message;
    private final JSONObject raw;
}
