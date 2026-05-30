package missu.epsilon.client.event.events.command;

import lombok.Getter;
import missu.epsilon.client.event.impl.CancellableEvent;

@Getter
public class MessageEvent extends CancellableEvent {

    private final String message;

    public MessageEvent(String message) {
        this.message = message;
    }

}
