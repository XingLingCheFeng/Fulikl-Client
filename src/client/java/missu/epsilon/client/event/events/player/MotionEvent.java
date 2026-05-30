package missu.epsilon.client.event.events.player;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.CancellableEvent;

public class MotionEvent extends CancellableEvent {

    public EventState eventState;
    @Getter @Setter public float yaw, pitch;
    @Getter @Setter public double x, y, z;
    @Getter @Setter public boolean onGround;

    public MotionEvent(EventState eventState, float yaw, float pitch, double x, double y, double z, boolean onGround) {
        this.eventState = eventState;
        this.yaw = yaw;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
    }

}
