package net.flioris.jva.event.chat;

import net.flioris.jva.models.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommandEvent {
    private final Message message;
    private final String commandName;
    private final String[] args;
}
