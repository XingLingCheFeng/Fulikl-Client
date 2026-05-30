package missu.epsilon.client.utils.entity;

import lombok.Getter;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.utils.Wrapper;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Simple buffer handler example ported to 1.21.4 APIs.
 */
public class BufferHandler implements Wrapper {
    @Getter
    private DelayModules delayModule = DelayModules.NONE;
    private long delayTicks = 0L;
    private final Deque<Packet<?>> delayedPackets = new ConcurrentLinkedDeque<>();

    /**
     * Returns true if the packet should be buffered instead of processed immediately.
     */
    public boolean shouldDelay(Packet<?> packet) {
        if (this.delayModule == DelayModules.NONE) {
            return false;
        }
        if (packet instanceof KeepAliveS2CPacket) {
            return false;
        }
        if (!(packet instanceof GameJoinS2CPacket) && !(packet instanceof PlayerRespawnS2CPacket)) {
            if (packet instanceof EntityStatusS2CPacket statusPacket && mc.world != null) {
                Entity entity = statusPacket.getEntity(mc.world);
                if (entity != null && mc.player != null && (!entity.equals(mc.player) || statusPacket.getStatus() != 2)) {
                    return false;
                }
            }
            this.delayedPackets.offer(packet);
            return true;
        }
        this.stopDelay(false, this.delayModule);
        return false;
    }

    /**
     * Stops delaying packets. When {@code set} is false, queued packets are flushed to the play handler.
     */
    public void stopDelay(boolean set, DelayModules module) {
        if (set) {
            this.delayModule = module;
            this.delayTicks = 0L;
            return;
        }

        this.delayModule = DelayModules.NONE;
        this.delayTicks = 0L;

        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null || this.delayedPackets.isEmpty()) {
            this.delayedPackets.clear();
            return;
        }

        Packet<?> packet;
        while ((packet = this.delayedPackets.poll()) != null) {
            this.applyPacket(packet, handler);
        }
    }

    private void applyPacket(Packet<?> packet, ClientPlayNetworkHandler handler) {
        try {
            @SuppressWarnings("unchecked")
            Packet<ClientPlayPacketListener> clientPacket = (Packet<ClientPlayPacketListener>) packet;
            clientPacket.apply(handler);
        } catch (ClassCastException ignored) {
        }
    }

    public void delay(DelayModules module) {
        this.delayModule = module;
    }

    public long getDelayTicks() {
        return this.delayTicks;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (event.getPacketState() == CancellableEvent.PacketState.SEND) {
            if (packet instanceof HandshakeC2SPacket
                    || packet instanceof LoginHelloC2SPacket
                    || packet instanceof LoginKeyC2SPacket
                    || packet instanceof QueryRequestC2SPacket
                    || packet instanceof QueryPingC2SPacket) {
                this.stopDelay(false, this.delayModule);
            }
            return;
        }

        if (event.getPacketState() == CancellableEvent.PacketState.RECEIVE && this.shouldDelay(packet)) {
            event.cancelEvent();
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.player.isDead()) {
            this.stopDelay(false, this.delayModule);
            return;
        }

        if (this.delayModule != DelayModules.NONE) {
            this.delayTicks++;
        }
    }
}
