package missu.epsilon.client.event.events.network;

import lombok.Getter;
import missu.epsilon.client.event.impl.CancellableEvent;
import net.minecraft.network.packet.Packet;

@Getter
public class SyncApplyPacketEvent extends CancellableEvent {

    private final Packet<?> packet;

    public SyncApplyPacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

}
