package missu.epsilon.client.event.events.game;

import missu.epsilon.client.event.impl.Event;

public class SendMessageEvent implements Event {

    public String message;
    public SendMessageEvent(String message) {
        this.message = message;
    }

}
