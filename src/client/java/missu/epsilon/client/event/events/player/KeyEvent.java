package missu.epsilon.client.event.events.player;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.Event;

@Getter
public class KeyEvent implements Event {

    @Setter
    private int key;
    private final boolean hasScreen;

    public KeyEvent(int key, boolean hasScreen){
        this.key = key;
        this.hasScreen = hasScreen;
    }

}