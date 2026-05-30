package missu.epsilon.client.event.events.player;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.Event;

@Getter
@Setter
public class FallFlyingEvent implements Event {

    private float rotationPitch;

    public FallFlyingEvent(float rotationPitch) {
        this.rotationPitch = rotationPitch;
    }

}
