package missu.epsilon.client.event.events.player;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.CancellableEvent;

@Setter
@Getter
public class JumpEvent extends CancellableEvent {

    public float yaw;

    public JumpEvent(float yaw) {
        this.yaw = yaw;
    }

}
