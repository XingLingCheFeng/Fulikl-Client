package missu.epsilon.client.event.events.network;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.CancellableEvent;
import net.minecraft.network.packet.Packet;

@Getter
@Setter
public class PacketEvent extends CancellableEvent {

    public CancellableEvent.PacketState packetState;
    public Packet<?> packet;

    public PacketEvent(Packet<?> packet, CancellableEvent.PacketState packetState) {
        this.packet = packet;
        this.packetState = packetState;
    }

}
