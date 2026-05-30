package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.network.RotationAppliedEvent;
import missu.epsilon.client.event.events.network.RotationEvent;
import missu.epsilon.client.event.events.player.MoveInputEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.entity.PlayerUtils;
import missu.epsilon.client.utils.entity.RaycastUtils;
import missu.epsilon.client.utils.entity.Rotation;
import missu.epsilon.client.utils.entity.RotationUtils;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Thanks to SouthSide NextGen for the MLG
 */

@ModuleInfo(name = "NoFall", category = ModuleCategory.MOVEMENT, description = "Disable Falling effect on the server")
public class NoFall extends Module {
    public static final ListValue mode = new ListValue("Mode", new String[]{"MLG"}, "MLG");
    public static final ListValue distanceMode = new ListValue("FallDistanceMode", new String[]{"SafeDistance", "Custom"}, "SafeDistance");
    public static final NumberValue fallDistance = new NumberValue("FallDistance", 3.0, 0.0, 10.0,0.1).displayable(() -> distanceMode.is("Custom"));
    public static final NumberValue retrieveTick = new NumberValue("RetrieveTick", 0, 0, 20,1).displayable(() -> mode.is("MLG"));
    public static BoolValue stopMove = new BoolValue("StopMove",false);

    public static int ticksExisted = -1;
    public static boolean interactRequired = false;
    public static int oldSlot = -1;
    public static boolean handleStopMove = false;
    public static boolean shouldReceive = false;
    private static PlaceData lastData;

    record PlaceData(BlockPos pos, Box bb) {
    }

    @EventTarget
    public void onEnable() {
        interactRequired = false;
        shouldReceive = false;
        handleStopMove = false;
    }

    @Override
    public void onDisable() {
        handleStopMove = false;
        interactRequired = false;
        shouldReceive = false;
        lastData = null;
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onRotation(RotationEvent event) {
        if (mc.player == null || PlayerUtils.ticksSinceTeleport < 3) return;
        if (mc.player.isCreative()) return;
        if (mc.player.isUsingItem() || mc.currentScreen != null) return;

        if (mode.is("MLG")) {
            boolean shouldMLG = (ClientData.getFallDistance() > mc.player.getAttributeValue(EntityAttributes.SAFE_FALL_DISTANCE) && distanceMode.is("SafeDistance")
                    || ClientData.getFallDistance() > fallDistance.get() && distanceMode.is("Custom")) && !mc.player.isInsideWaterOrBubbleColumn() && nextTickWillLanding();

            if (ticksExisted >= 0) {
                ticksExisted--;
            }

            if (shouldMLG) {
                int waterBucketSlot = PlayerUtils.findSlot(Items.WATER_BUCKET);
                if (waterBucketSlot == -1) {
                    shouldMLG = false;
                }
                ticksExisted = retrieveTick.get().intValue();
                handleStopMove = true;
                placeWaterBucket(waterBucketSlot);
            }
            if (!shouldMLG && hasEmptyBucket() && shouldReceive && ticksExisted <= 0) {
                retrieveWaterBlock();
            }

            if (handleStopMove) {
                if (!mc.player.isInsideWaterOrBubbleColumn() && ClientData.clientOnGround()) {
                    handleStopMove = false;
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onRotationApplied(RotationAppliedEvent event) {
        if (RotationManager.targetRotation == null || mc.player == null || mc.interactionManager == null) return;

        if (mode.is("MLG")) {
            if (interactRequired) {
                BlockHitResult result = RaycastUtils.rayCast(new Vector2f(RotationManager.targetRotation.getYaw(), RotationManager.targetRotation.getPitch()), 4.5);
                if (result == null) {
                    if (mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot).getItem().equals(Items.BUCKET)) {
                        shouldReceive = true;
                    }
                    if (oldSlot != -1) {
                        mc.player.getInventory().selectedSlot = oldSlot;
                        oldSlot = -1;
                    }
                    return;
                }

                interactRequired = false;
                mc.crosshairTarget = result;
                float currentYaw = mc.player.getYaw();
                float currentPitch = mc.player.getPitch();
                mc.player.setYaw(RotationManager.targetRotation.getYaw());
                mc.player.setPitch(RotationManager.targetRotation.getPitch());

                var res = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.setYaw(currentYaw);
                mc.player.setPitch(currentPitch);
                if (res.isAccepted()) {
                    mc.player.swingHand(Hand.MAIN_HAND);
                }

                if (mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot).getItem().equals(Items.BUCKET)) {
                    shouldReceive = true;
                }
                if (oldSlot != -1) {
                    mc.player.getInventory().selectedSlot = oldSlot;
                    oldSlot = -1;
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onMovementInput(MoveInputEvent event) {
        if (lastData != null && mc.player != null && mode.is("MLG")) {
            mc.player.setSprinting(false);
        }
        if (handleStopMove && stopMove.get() && mc.player != null && mode.is("MLG")) {
            event.cancelEvent();
        }
    }

    private static void retrieveWaterBlock() {
        if (mc.player == null) return;

        oldSlot = mc.player.getInventory().selectedSlot;
        int emptyBucketSlot = PlayerUtils.findSlot(Items.BUCKET);
        if (emptyBucketSlot == -1) return;
        BlockPos pos = findScoopableWaterBlock();
        if (pos == null) {
            return;
        }
        var r = getLookAtWaterBlock(pos);

        if (mc.player.getMainHandStack().getItem() != Items.BUCKET)
            mc.player.getInventory().selectedSlot = emptyBucketSlot;
        interactItem(r);
        shouldReceive = false;
    }

    public static BlockPos findScoopableWaterBlock() {
        if (mc.player == null) return null;

        World world = mc.player.getWorld();
        BlockPos playerBlockPos = mc.player.getBlockPos();
        double maxReachDistance = mc.player.getBlockInteractionRange();
        int reach = (int) Math.ceil(maxReachDistance);
        List<BlockPos> possible = new ArrayList<>();
        for (int x = -reach; x <= reach; x++) {
            for (int y = -reach; y <= reach; y++) {
                for (int z = -reach; z <= reach; z++) {

                    BlockPos currentPos = playerBlockPos.add(x, y, z);

                    FluidState fluidState = world.getFluidState(currentPos);

                    if (fluidState.isStill() && fluidState.getFluid() == Fluids.WATER) {
                        possible.add(currentPos);
                    }
                }
            }
        }
        possible.removeIf(blockPos -> {
            Rotation rotation = getLookAtWaterBlock(blockPos);
            return RaycastUtils.rayCast(new Vector2f(rotation.getYaw(),rotation.getPitch()),4.5).getType() != HitResult.Type.BLOCK;
        });
        if (possible.isEmpty()) return null;
        return possible.getFirst();
    }

    private static boolean nextTickWillLanding() {
        return !isAirBlocksBelow(getYMotion());
    }

    public static boolean isAirBlocksBelow(int high) {
        if (mc.player == null || mc.world == null) return false;

        List<PlaceData> possibleBoxes = new ArrayList<>();

        for (int x = -getXMotion(); x <= getXMotion(); x++) {
            for (int z = -getZMotion(); z <= getZMotion(); z++) {
                for (int y = 0; y <= high; y++) {
                    var pos = mc.player.getBlockPos().add(x, 0, z).down(y);
                    var state = mc.world.getBlockState(pos);

                    var bb = new Box(pos).withMaxY(pos.getY() + 3);
                    if (bb.intersects(mc.player.getBoundingBox()) && !state.isAir()) {
                        possibleBoxes.add(new PlaceData(pos, bb));
                    }
                }
            }
        }

        return possibleBoxes.isEmpty();
    }
    private static int getXMotion() {
        if (mc.player == null) return -1;
        return (int) Math.ceil(mc.player.getVelocity().x * mc.player.getVelocity().x);
    }
    private static int getYMotion() {
        if (mc.player == null) return -1;
        return (int) Math.ceil(mc.player.getVelocity().y * mc.player.getVelocity().y);
    }
    private static int getZMotion() {
        if (mc.player == null) return -1;
        return (int) Math.ceil(mc.player.getVelocity().z * mc.player.getVelocity().z);
    }

    private static boolean hasEmptyBucket() {
        if (mc.player == null) return false;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack != null && stack.getItem() == Items.BUCKET) {
                return true;
            }
        }
        return false;
    }

    public static void interactItem(Rotation rotation) {
        Client.rotationManager.setRotations(rotation, 1, MovementFix.SILENT);
        interactRequired = true;
    }

    private static PlaceData findBestPlacePos() {
        if (mc.player == null || mc.world == null) return null;

        List<PlaceData> possibleBoxes = new ArrayList<>();

        var playerBox = mc.player.getBoundingBox();
        playerBox.offset(mc.player.getVelocity());

        for (int x = -getXMotion(); x <= getXMotion(); x++) {
            for (int z = -getZMotion(); z <= getZMotion(); z++) {
                for (int y = 0; y <= getYMotion(); y++) {
                    var pos = mc.player.getBlockPos().add(x, 0, z).down(y);
                    var state = mc.world.getBlockState(pos);

                    var bb = new Box(pos).withMaxY(pos.getY() + 3);
                    if (bb.intersects(playerBox) && !state.isAir()) {
                        possibleBoxes.add(new PlaceData(pos, bb));
                    }
                }
            }
        }

        possibleBoxes.sort(Comparator.comparingDouble(p -> p.pos.getY()));
        Collections.reverse(possibleBoxes);
        var best = possibleBoxes.getFirst();

        possibleBoxes.removeIf(p -> p.pos.getY() != best.pos().getY());

        possibleBoxes.sort(Comparator.comparingDouble(p -> Vec3d.of(p.pos()).squaredDistanceTo(
                playerBox.getCenter().withAxis(Direction.Axis.Y, playerBox.minY)
        )));
        possibleBoxes.sort(Comparator.comparing(p -> !p.bb.contains(mc.player.getPos())));
        return possibleBoxes.getFirst();
    }

    private static Rotation getLookAtWaterBlock(BlockPos targetPos) {
        if (mc.player == null || targetPos == null) {
            return new Rotation(0, 0);
        }

        return RotationUtils.toRotation(Vec3d.of(targetPos),false);
    }

    public static void placeWaterBucket(int waterBucketSlot) {
        if (waterBucketSlot == -1 || mc.player == null) {
            return;
        }
        oldSlot = mc.player.getInventory().selectedSlot;

        var best = findBestPlacePos();
        lastData = best;

        if (best == null) return;

        var r = getLookAtWaterBlock(best.pos());

        if (mc.player.getMainHandStack().getItem() != Items.WATER_BUCKET)
            mc.player.getInventory().selectedSlot = waterBucketSlot;
        interactItem(r);
        shouldReceive = true;
    }
}
