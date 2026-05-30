package missu.epsilon.client.event.events.network;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.CancellableEvent;
import net.minecraft.network.packet.Packet;

@Getter
@Setter
public class SyncSendPacketEvent extends CancellableEvent {

    private Packet<?> packet;

    public SyncSendPacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

}
