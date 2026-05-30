package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.event.events.game.WorldEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import missu.epsilon.client.utils.packets.PacketUtils;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import static missu.epsilon.client.utils.Wrapper.mc;

/**
 * By Daniel
 * 251122
 */

@ModuleInfo(name = "Blink",description = "Suspends all client packets.",category = ModuleCategory.PLAYER)
public class Blink extends Module {
    public static ListValue blinkMode = new ListValue("BlinkMode",new String[]{"Instant Release","Slow Release","Delay Release"},"Instant Release");
    public static NumberValue releaseInterval = (NumberValue) new NumberValue("ReleaseInterval(ms)",350,0,1000,10).displayable(() -> blinkMode.is("Slow Release"));
    public static NumberValue delayTime = (NumberValue) new NumberValue("DelayTime(ms)",1000,0,8000,10).displayable(() -> blinkMode.is("Delay Release"));
    public static BoolValue fakePlayer = new BoolValue("FakePlayer",true);

    public static TimerUtils releaseTimer = new TimerUtils();
    public static TimerUtils delayTimer = new TimerUtils();
    public static OtherClientPlayerEntity clonePlayer = null;
    public static final LinkedBlockingDeque<Packet<?>> blinkPackets = new LinkedBlockingDeque<>();

    @Override
    public void onEnable() {
        releaseTimer.reset();
        delayTimer.reset();
        if (mc.isInSingleplayer()) {
            this.setState(false);
            ClientUtils.displayChat("You can't use blink in singleplayer!");
            return;
        }
        if (mc.world == null || mc.player == null) return;
        if (fakePlayer.get()) {
            OtherClientPlayerEntity clone = new OtherClientPlayerEntity(mc.world,mc.player.getGameProfile());
            clone.headYaw = mc.player.headYaw;
            clone.copyPositionAndRotation(mc.player);
            clone.setUuid(UUID.randomUUID());
            mc.world.addEntity(clone);
            clonePlayer = clone;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.isCancelled()) return;
        if (ClientUtils.isNull() || mc.getNetworkHandler() == null || mc.player.isDead()) return;

        Packet<?> packet = event.getPacket();

        if (packet instanceof HandshakeC2SPacket
                || packet instanceof QueryRequestC2SPacket
                || packet instanceof QueryPingC2SPacket
                || packet instanceof GameMessageS2CPacket
                || packet instanceof DisconnectS2CPacket) {
            return;
        }

        if (event.packetState == CancellableEvent.PacketState.SEND) {
            event.cancelEvent();
            synchronized (blinkPackets) {
                blinkPackets.add(packet);
            }

        }
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        if (mc.world == null) {
            blinkPackets.clear();
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.eventState == CancellableEvent.EventState.POST) {
            if (mc.player == null) return;
            if (mc.player.isDead() || mc.player.age <= 10) {
                sendBlinkPacket();
            }
            if (delayTimer.hasTimeElapsed(delayTime.get()) && blinkMode.is("Delay Release")) {
                releaseTickPacket();
            }
            if (releaseTimer.hasTimeElapsed(releaseInterval.get()) && blinkMode.is("Slow Release")) {
                releaseTickPacket();
                releaseTimer.reset();
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        sendBlinkPacket();
    }

    private void sendBlinkPacket() {
        synchronized (blinkPackets) {
            while (!blinkPackets.isEmpty()) {
                Packet<?> packet = blinkPackets.poll();
                PacketUtils.sendPacketNoEvent(packet);
            }
        }

        if (clonePlayer != null) {
            deleteFakePlayer();
            clonePlayer = null;
        }
    }

    private void releaseTickPacket() {
        synchronized (blinkPackets) {
            while (!blinkPackets.isEmpty()) {
                Packet<?> packet = blinkPackets.poll();
                PacketUtils.sendPacketNoEvent(packet);
                if (packet instanceof PlayerMoveC2SPacket packet1) {
                    double x = packet1.getX(clonePlayer.getX());
                    double y = packet1.getY(clonePlayer.getY());
                    double z = packet1.getZ(clonePlayer.getZ());

                    float yaw = packet1.getYaw(clonePlayer.getYaw());
                    float pitch = packet1.getPitch(clonePlayer.getPitch());

                    clonePlayer.updatePositionAndAngles(x, y, z, yaw, pitch);

                    if (packet1.changesLook()) {
                        clonePlayer.setYaw(yaw);
                        clonePlayer.setHeadYaw(yaw);
                        clonePlayer.setPitch(pitch);
                    }
                    break;
                }
            }
        }
    }

    private void deleteFakePlayer() {
        if (clonePlayer == null || mc.world == null) return;
        OtherClientPlayerEntity clone = clonePlayer;

        mc.world.removeEntity(clone.getId(), Entity.RemovalReason.DISCARDED);
        clonePlayer = null;
    }
}