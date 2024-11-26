package net.flioris.jva.event.chat;

import net.flioris.jva.models.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageEvent {
    private final Message message;
}
