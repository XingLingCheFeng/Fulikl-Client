package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.events.game.WorldEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.MoveInputEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.entity.Rotation;
import missu.epsilon.client.utils.entity.RotationUtils;
import missu.epsilon.client.utils.movement.MovementUtils;
import missu.epsilon.client.utils.packets.PacketUtils;
import missu.epsilon.client.utils.scaffold.PlaceInfo;
import missu.epsilon.client.utils.scaffold.ScaffoldUtils;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static missu.epsilon.client.utils.Wrapper.mc;

/**
 * 不 要 抄 袭
 * 大杂烩，所以看着会有点乱
 * @author Jon_awa && Daniel
 */
@ModuleInfo(name = "AntiVoid",category = ModuleCategory.MOVEMENT,description = "Automatically stuck to avoid you falling to the void")
public class AntiVoid extends Module {
    private final NumberValue maxFallDistValue = new NumberValue("MaxFallDistance", 3, 1, 20,1);
    private final BoolValue pickPearl = new BoolValue("FindPearl", true);
    private final BoolValue onlyPearl = new BoolValue("OnlyWhenHavePearl", false);
    public static BoolValue checkClutch = new BoolValue("CheckClutch",false);
    private final BoolValue ticksLimited = new BoolValue("Ticks Limited", false);
    private final NumberValue maxTicks = (NumberValue) new NumberValue("Max Ticks", 100, 0, 1200, 10).displayable(ticksLimited::get);
    private final BoolValue continueStuck = (BoolValue) new BoolValue("Continue Stuck", false).displayable(ticksLimited::get);
    private final NumberValue coolDownTicks = (NumberValue) new NumberValue("Cool Down Ticks", 20, 0, 40, 1).displayable(ticksLimited::get);
    private final MultiBoolValue grimFixSettings = new MultiBoolValue("Grim Fix Settings", new BoolValue[] {
            new BoolValue("Fix TransactionOrder", true),
            new BoolValue("Fix BadPacketR", true),
            new BoolValue("Fix BadPacketO (1.21 Check)", true)
    });

    private final Queue<CommonPongC2SPacket> pongPackets = new ConcurrentLinkedQueue<>();
    private final LinkedList<Runnable> delayToNextTick = new LinkedList<>();
    private boolean cantStuck;
    private Rotation lastRotation;
    private boolean hasRotated;
    private boolean freeze;
    private int stuckTicks;
    private boolean switchedToPearl;

    @Override
    public void onEnable() {
        reset();
    }

    @Override
    public void onDisable() {
        switchedToPearl = false;
        cantStuck = false;
        if (grimFixSettings.get("Fix BadPacketO (1.21 Check)")) {
            while (!delayToNextTick.isEmpty()) {
                delayToNextTick.poll().run();
            }
        }

        if (grimFixSettings.get("Fix TransactionOrder")) {
            while (!pongPackets.isEmpty()) {
                PacketUtils.sendPacketNoEvent(pongPackets.poll());
            }
        }
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }
        if (!cantStuck && isInVoid() && ClientData.getFallDistance() > maxFallDistValue.get() &&
                (!onlyPearl.get() || (onlyPearl.get() && hasPearl())) && (!checkClutch.get() || (canPlaceBlock() || hasPearl()))) {
            if (!delayToNextTick.isEmpty()) {
                for (var runnable : delayToNextTick) {
                    runnable.run();
                }
                delayToNextTick.clear();
            }

            hasRotated = false;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null) return;

        if (mc.player.onGround) cantStuck = false;

        if (!cantStuck && isInVoid() && ClientData.getFallDistance() > maxFallDistValue.get() &&
                (!onlyPearl.get() || (onlyPearl.get() && hasPearl())) && (!checkClutch.get() || (canPlaceBlock() || hasPearl()))) {

            if (pickPearl.get() && hasPearl() && !switchedToPearl) {
                ItemStack currentItem = mc.player.getMainHandStack();
                if (currentItem == null || !(currentItem.getItem() instanceof EnderPearlItem)) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = mc.player.getInventory().getStack(i);
                        if (stack != null && stack.getItem() instanceof EnderPearlItem) {
                            mc.player.getInventory().selectedSlot = i;
                            switchedToPearl = true;
                            break;
                        }
                    }
                }
            }

            if (freeze) {
                mc.player.setVelocity(new Vec3d(0, 0, 0));
            }

            if (freeze || stuckTicks < 0) {
                ++stuckTicks;
            }

            if (ticksLimited.getValue() && (stuckTicks < 0 || stuckTicks >= maxTicks.getValue())) {
                freeze = false;

                if (continueStuck.getValue()) {
                    if (stuckTicks > 0) {
                        stuckTicks = -coolDownTicks.getValue().intValue();
                    }
                } else {
                    cantStuck = true;
                }
            }
        } else {
            freeze = false;
            reset();
            if (grimFixSettings.get("Fix BadPacketO (1.21 Check)")) {
                while (!delayToNextTick.isEmpty()) {
                    delayToNextTick.poll().run();
                }
            }

            if (grimFixSettings.get("Fix TransactionOrder")) {
                while (!pongPackets.isEmpty()) {
                    PacketUtils.sendPacketNoEvent(pongPackets.poll());
                }
            }
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (freeze) {
            event.strafe = 0;
            event.forward = 0;
            event.sneak = false;
            event.jump = false;
        }
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        reset();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null) return;

        if (!cantStuck && isInVoid() && ClientData.getFallDistance() > maxFallDistValue.get() &&
                (!onlyPearl.get() || (onlyPearl.get() && hasPearl())) && (!checkClutch.get() || (canPlaceBlock() || hasPearl()))) {
            if (event.packetState == CancellableEvent.PacketState.SEND) {
                if (!freeze && stuckTicks == 0) {
                    if (event.getPacket() instanceof PlayerMoveC2SPacket packet) {
                        if (packet.changesPosition()) {
                            freeze = true;
                        }

                        if (packet.changesLook()) {
                            lastRotation = new Rotation(packet.yaw, packet.pitch);
                        }
                    }
                } else {
                    if (event.getPacket() instanceof PlayerMoveC2SPacket) {
                        event.cancelEvent();
                    } else if (event.getPacket() instanceof CommonPongC2SPacket packet && grimFixSettings.get("Fix BadPacketR")) {
                        pongPackets.offer(packet);
                        event.cancelEvent();
                    } else if ((event.getPacket() instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM && (mc.player.getActiveItem().getItem() instanceof BowItem || mc.player.getActiveItem().getItem() instanceof CrossbowItem)) || event.getPacket() instanceof PlayerInteractItemC2SPacket || event.getPacket() instanceof PlayerInteractBlockC2SPacket) {
                        if (lastRotation == null || !lastRotation.equals(RotationUtils.getRotationOrElseMC())) {
                            var rotation = RotationUtils.getRotationOrElseMC();
                            PacketUtils.sendPacketNoEvent(new PlayerMoveC2SPacket.LookAndOnGround(rotation.getYaw(), rotation.getPitch(), mc.player.isOnGround(), mc.player.horizontalCollision));

                            lastRotation = rotation;
                            hasRotated = true;
                            stuckTicks = 0;

                            if (grimFixSettings.get("Fix TransactionOrder")) {
                                if (grimFixSettings.get("Fix BadPacketO (1.21 Check)")) {
                                    delayToNextTick.add(() -> {
                                        while (!pongPackets.isEmpty()) {
                                            PacketUtils.sendPacketNoEvent(pongPackets.poll());
                                        }
                                    });
                                } else {
                                    while (!pongPackets.isEmpty()) {
                                        PacketUtils.sendPacketNoEvent(pongPackets.poll());
                                    }
                                }
                            }
                        }

                        if (grimFixSettings.get("Fix BadPacketO (1.21 Check)") && hasRotated) {
                            delayToNextTick.add(() -> PacketUtils.sendPacketNoEvent(event.getPacket()));
                        } else {
                            PacketUtils.sendPacketNoEvent(event.getPacket());
                        }

                        event.cancelEvent();
                    } else if (event.getPacket() instanceof HandSwingC2SPacket) {
                        if (grimFixSettings.get("Fix BadPacketO (1.21 Check)") && hasRotated) {
                            delayToNextTick.add(() -> PacketUtils.sendPacketNoEvent(event.getPacket()));
                            event.cancelEvent();
                        }
                    }
                }
            } else if (freeze) {
                if (event.getPacket() instanceof EntityDamageS2CPacket packet && packet.entityId() == mc.player.getId()) {
                    if (grimFixSettings.get("Fix BadPacketO (1.21 Check)")) {
                        reset();
                        freeze = false;
                        cantStuck = true;
                        while (!delayToNextTick.isEmpty()) {
                            delayToNextTick.poll().run();
                        }
                    }

                    if (grimFixSettings.get("Fix TransactionOrder")) {
                        while (!pongPackets.isEmpty()) {
                            PacketUtils.sendPacketNoEvent(pongPackets.poll());
                        }
                    }
                } else if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet && packet.getEntityId() == mc.player.getId()) {
                    if (grimFixSettings.get("Fix BadPacketO (1.21 Check)")) {
                        reset();
                        freeze = false;
                        cantStuck = true;
                        while (!delayToNextTick.isEmpty()) {
                            delayToNextTick.poll().run();
                        }
                    }

                    if (grimFixSettings.get("Fix TransactionOrder")) {
                        while (!pongPackets.isEmpty()) {
                            PacketUtils.sendPacketNoEvent(pongPackets.poll());
                        }
                    }
                }
            }
        } else {
            freeze = false;
            if (grimFixSettings.get("Fix BadPacketO (1.21 Check)")) {
                while (!delayToNextTick.isEmpty()) {
                    delayToNextTick.poll().run();
                }
            }

            if (grimFixSettings.get("Fix TransactionOrder")) {
                while (!pongPackets.isEmpty()) {
                    PacketUtils.sendPacketNoEvent(pongPackets.poll());
                }
            }
        }
    }

    private void reset() {
        switchedToPearl = false;
        delayToNextTick.clear();
        pongPackets.clear();
        lastRotation = null;
        stuckTicks = 0;
        freeze = false;
    }

    public static boolean canPlaceBlock() {
        if (mc.player == null) return false;

        int x = MathHelper.floor(mc.player.getX());
        int z = MathHelper.floor(mc.player.getZ());
        int startY = MathHelper.floor(mc.player.getY() - 1);

        ScaffoldUtils.SearchMode mode = ScaffoldUtils.SearchMode.Hypixel;

        BlockPos pos = new BlockPos(x, startY, z);

        PlaceInfo info = ScaffoldUtils.getPlaceInfo(pos, mode);
        return info != null;
    }

    private boolean hasPearl() {
        if (mc.player == null || mc.player.getInventory() == null) {
            return false;
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() instanceof EnderPearlItem) {
                return true;
            }
        }
        return false;
    }

    private boolean isInVoid() {
        if (mc.player == null) {
            return false;
        }

        for (int i = 0; i <= 128; i++) {
            if (MovementUtils.isOnGround(mc.player,i)) {
                return false;
            }
        }
        return true;
    }
}
