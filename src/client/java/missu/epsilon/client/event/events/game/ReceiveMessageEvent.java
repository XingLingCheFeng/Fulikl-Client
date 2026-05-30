package missu.epsilon.client.event.events.game;

import lombok.Getter;
import missu.epsilon.client.event.impl.CancellableEvent;
import net.minecraft.text.Text;

@Getter
public class ReceiveMessageEvent extends CancellableEvent {

    private final Text message;

    public ReceiveMessageEvent(Text message) {
        this.message = message;
    }

}
