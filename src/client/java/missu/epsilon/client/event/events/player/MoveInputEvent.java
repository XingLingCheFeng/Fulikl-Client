package missu.epsilon.client.event.events.player;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.CancellableEvent;

@Getter
@Setter
public class MoveInputEvent extends CancellableEvent {

    public float forward;
    public float strafe;
    public boolean jump;
    public boolean sneak;

    public MoveInputEvent(float forward, float strafe, boolean jump, boolean sneak) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneak = sneak;
    }

}
