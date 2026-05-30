package missu.epsilon.client.event.events.game;

import missu.epsilon.client.event.impl.CancellableEvent;

public class MoveEvent extends CancellableEvent {

    public double x, y, z;

    public MoveEvent(Double x,Double y,Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

}
