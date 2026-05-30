package missu.epsilon.client.event.events.player;


import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.CancellableEvent;

public class StrafeEvent extends CancellableEvent {

    @Setter @Getter public float forward, strafe, friction, yaw;

    public StrafeEvent(float forward, float strafe, float friction, float yaw) {
        this.forward = forward;
        this.strafe = strafe;
        this.friction = friction;
        this.yaw = yaw;
    }

}