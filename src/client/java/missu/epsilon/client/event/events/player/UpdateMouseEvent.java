package missu.epsilon.client.event.events.player;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.CancellableEvent;

@Getter
@Setter
public class UpdateMouseEvent extends CancellableEvent {

    private double yaw;
    private double pitch;
    private double smoother;

    public UpdateMouseEvent(double yaw, double pitch, double smoother) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.smoother = smoother;
    }

}
