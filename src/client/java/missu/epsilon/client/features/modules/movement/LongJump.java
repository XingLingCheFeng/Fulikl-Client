package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.entity.PlayerUtils;
import missu.epsilon.client.utils.entity.Rotation;
import missu.epsilon.client.utils.entity.RotationUtils;
import missu.epsilon.client.utils.movement.MovementUtils;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

import java.util.concurrent.LinkedBlockingDeque;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "LongJump",description = "Use fireball to jump",category = ModuleCategory.MOVEMENT)
public class LongJump extends Module {
    public static LinkedBlockingDeque<Packet<ClientPlayPacketListener>> serverPackets = new LinkedBlockingDeque<>();
    public static double lastMotionY;
    public static boolean delay;
    public static int delayTick;
    public static boolean setTrue;
    public static boolean used;

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        if (PlayerUtils.findSlot(Items.FIRE_CHARGE) == -1) {
            ClientUtils.displayChat("You don't have fire charge in your slot");
            setState(false);
            return;
        }

        ClientUtils.displayChat("LongJump enabled,please disable other modules that need rotations");
        if (mc.player.getMainHandStack().getItem() != Items.FIRE_CHARGE) {
            mc.player.getInventory().selectedSlot = PlayerUtils.findSlot(Items.FIRE_CHARGE);
        }
        used = false;
    }

    @Override
    public void onDisable() {
        while (!serverPackets.isEmpty()) {
            Packet<ClientPlayPacketListener> packet = serverPackets.poll();
            packet.apply(mc.getNetworkHandler());
        }
        delay = false;
        mc.options.jumpKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode()));
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null) return;
        double motionY = mc.player.getVelocity().y;

        if (delayTick > 0) delayTick--;

        if (!used) {
            if (!MovementUtils.isMove())
                Client.rotationManager.setRotations(new Rotation(mc.player.getYaw(),90F),1, MovementFix.SILENT);
            else
                Client.rotationManager.setRotations(new Rotation(mc.player.getYaw() - 180F,90F),1,MovementFix.SILENT);
            setTrue = true;
        }

        if (delay) {
            mc.options.jumpKey.setPressed(true);
        }
        if (!mc.player.onGround && (mc.player.getVelocity().y < 0 && lastMotionY >= 0 || delayTick == 0) && used && delay) {
            while (!serverPackets.isEmpty()) {
                Packet<ClientPlayPacketListener> packet = serverPackets.poll();
                packet.apply(mc.getNetworkHandler());
            }
            delay = false;
            delayTick = 0;
            mc.options.jumpKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode()));
            setState(false);
        }
        lastMotionY = motionY;
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        if (event.packetState == CancellableEvent.PacketState.RECEIVE) {
            if (event.packet instanceof EntityVelocityUpdateS2CPacket packet) {
                if (packet.getEntityId() == mc.player.getId()) {
                    if (setTrue) {
                        delay = true;
                        delayTick = 15;
                        used = true;
                    }
                }
            }
            if (delay) {
                event.cancelEvent();
                @SuppressWarnings("unchecked")
                Packet<ClientPlayPacketListener> typedPacket = (Packet<ClientPlayPacketListener>) event.packet;
                serverPackets.add(typedPacket);
            }
        }
    }
}
