package missu.epsilon.client.event.events.player;

import missu.epsilon.client.event.impl.CancellableEvent;

public class SlowdownEvent extends CancellableEvent {

    public float forward,sideways;

    public SlowdownEvent(float forward,float sideways) {
        this.forward = forward;
        this.sideways = sideways;
    }

}
