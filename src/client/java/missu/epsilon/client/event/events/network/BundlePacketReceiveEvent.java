package missu.epsilon.client.event.events.network;

import lombok.Getter;
import missu.epsilon.client.event.impl.CancellableEvent;
import net.minecraft.network.packet.Packet;

public class BundlePacketReceiveEvent extends CancellableEvent {

    @Getter
    public Packet<?> packet;

    public BundlePacketReceiveEvent(Packet<?> packet) {
        this.packet = packet;
    }

}
