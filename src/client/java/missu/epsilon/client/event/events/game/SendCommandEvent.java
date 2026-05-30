package missu.epsilon.client.event.events.game;

import missu.epsilon.client.event.impl.Event;

public class SendCommandEvent implements Event {

    public String command;
    public SendCommandEvent(String command) {
        this.command = command;
    }

}
