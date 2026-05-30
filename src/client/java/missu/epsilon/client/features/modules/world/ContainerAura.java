package missu.epsilon.client.features.modules.world;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.EnumAutoDisableType;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.combat.KillAura;
import missu.epsilon.client.features.modules.player.Blink;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.client.VecCalculation;
import missu.epsilon.client.utils.entity.InventoryUtils;
import missu.epsilon.client.utils.entity.RaycastUtils;
import missu.epsilon.client.utils.entity.Rotation;
import missu.epsilon.client.utils.entity.RotationUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import missu.epsilon.client.utils.network.ServerUtils;
import net.minecraft.block.entity.*;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static missu.epsilon.client.utils.Wrapper.mc;

/**
 * Author Daniel
 * Date IDK
 */

@ModuleInfo(name = "ContainerAura",category = ModuleCategory.WORLD,description = "Automatically steal containers", autoDisable = EnumAutoDisableType.GAME_END)
public class ContainerAura extends Module {
    public static MultiBoolValue container = new MultiBoolValue("Interactive Container",new BoolValue[]{
            new BoolValue("Chest",true),
            new BoolValue("Furnace",false),
            new BoolValue("BlastFurnace",false),
            new BoolValue("SmokerFurnace", false),
            new BoolValue("BrewingStand", false)
    });
    public static BoolValue swing = new BoolValue("Swing",true);
    public static BoolValue workOnStealerEnabled = new BoolValue("WorkOnStealerEnabled",true);
    public static BoolValue ignoreOtherChestOpen = new BoolValue("IgnoreEnemyOpenChest",false);
    public static NumberValue range = new NumberValue("Range",4.5,0,7,0.1);
    public static NumberValue throughRange = new NumberValue("ThroughWallRange",4.5,0,7,0.1);
    public static NumberValue cancelRange = new NumberValue("CancelRange",0,0,20,0.1);
    public static NumberValue delay = new NumberValue("Delay",400,0,1000,1);
    public static NumberValue turnSpeed = new NumberValue("TurnSpeed",180,0,180,0.1);
    public static BoolValue disableInLobby = new BoolValue("DisableInLobby",false);

    public static boolean opened = false;
    public static TimerUtils timer = new TimerUtils();
    public static BlockEntity targetBlock = null;

    @Override
    public void onEnable() {
        targetBlock = null;
        opened = false;
        timer.reset();
    }

    @Override
    public void onDisable() {
        targetBlock = null;
        opened = false;
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onMotion(MotionEvent event) {
        if (disableInLobby.get() && ServerUtils.isInLobby()) return;
        if (mc.world == null || mc.player == null || event.eventState == CancellableEvent.EventState.PRE || mc.interactionManager == null || mc.getNetworkHandler() == null
                || Client.moduleManager.getModule(Blink.class).getState()
                || KillAura.currentTarget != null
                || Client.moduleManager.getModule(Scaffold.class).getState()
                || workOnStealerEnabled.get() && !Client.moduleManager.getModule(ContainerStealer.class).getState()
                || !timer.hasTimeElapsed(delay.get())
        || opened || InventoryUtils.serverOpenInventory)
            return;

        for (PlayerEntity entity: mc.world.getPlayers()) {
            if (entity == mc.player) continue;
            if (VecCalculation.getDistanceToEntityBox(mc.player,entity) <= cancelRange.get()) {
                if (mc.currentScreen instanceof GenericContainerScreen || mc.currentScreen instanceof FurnaceScreen || mc.currentScreen instanceof BrewingStandScreen || mc.currentScreen instanceof SmokerScreen || mc.currentScreen instanceof BlastFurnaceScreen || mc.currentScreen instanceof InventoryScreen)
                    mc.player.closeHandledScreen();
                return;
            }
        }

        if (mc.currentScreen instanceof GenericContainerScreen || mc.currentScreen instanceof FurnaceScreen || mc.currentScreen instanceof BrewingStandScreen || mc.currentScreen instanceof SmokerScreen || mc.currentScreen instanceof BlastFurnaceScreen || mc.currentScreen instanceof InventoryScreen) return;

        RotationUtils.updateCurrentCrosshairTarget();

        List<BlockEntity> nearbyContainers = getNearbyContainers(mc.world, mc.player);
        if (nearbyContainers.isEmpty())
            return;

        BlockEntity target = nearbyContainers.getFirst();
        BlockPos targetPos = target.getPos();

        Vec3d eyes = mc.player.getEyePos();
        Vec3d targetCenter = Vec3d.ofCenter(targetPos);

        double distance = eyes.distanceTo(targetCenter);
        boolean throughWalls = distance <= throughRange.get();

        double dx = targetCenter.x - eyes.x;
        double dy = targetCenter.y - eyes.y;
        double dz = targetCenter.z - eyes.z;

        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, distXZ)));

        BlockHitResult predictHit = RaycastUtils.rayCastContainer(new Rotation(yaw, pitch), mc.player, mc.world, range.get(), throughWalls);

        if (predictHit == null || !predictHit.getBlockPos().equals(targetPos)) return;

        Client.rotationManager.setRotations(new Rotation(yaw, pitch), 1, MovementFix.SILENT, turnSpeed.get().intValue());

        BlockHitResult hit = RaycastUtils.rayCastContainer(RotationManager.serverRotation, mc.player, mc.world, range.get(), throughWalls);
        if (hit == null) return;

        if (hit.getBlockPos().equals(targetPos)) {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
            opened = true;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (opened && (event.packet instanceof CloseScreenS2CPacket || event.packet instanceof CloseHandledScreenC2SPacket)) {
            timer.reset();
            opened = false;
        }
    }

    /// 找你旁边的 containers
    private static List<BlockEntity> getNearbyContainers(ClientWorld world, ClientPlayerEntity player) {
        List<BlockEntity> list = new ArrayList<>();
        BlockPos playerPos = player.getBlockPos();
        int radius = (int) Math.ceil(range.get());

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be == null) continue;

                    if ((be instanceof ChestBlockEntity && container.get("Chest")
                            || be instanceof FurnaceBlockEntity && container.get("Furnace")
                            || be instanceof BlastFurnaceBlockEntity && container.get("BlastFurnace")
                            || be instanceof SmokerBlockEntity && container.get("SmokerFurnace")
                            || be instanceof BrewingStandBlockEntity && container.get("BrewingStand"))
                            && (!ClientData.clickedContainers.contains(Objects.requireNonNull(mc.world).getBlockEntity(pos)) || ignoreOtherChestOpen.get() && !ClientData.playerClickedContainers.contains(Objects.requireNonNull(mc.world).getBlockEntity(pos)))) {

                        double dist = player.getEyePos().distanceTo(Vec3d.ofCenter(pos));
                        if (dist <= range.get()) {
                            list.add(be);
                        }
                    }
                }
            }
        }

        list.sort(Comparator.comparingDouble(be ->
                player.getEyePos().distanceTo(Vec3d.ofCenter(be.getPos()))
        ));

        return list;
    }
}
