package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.events.player.MoveInputEvent;
import missu.epsilon.client.event.events.player.SlowMovementEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.movement.MovementUtils;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@ModuleInfo(name = "FastWeb",category = ModuleCategory.MOVEMENT,description = "Reduce or cancel the slow effect on the web")
public class FastWeb extends Module {
    public static ListValue mode = new ListValue("Mode",  new String[]{"Cancel", "GrimStrafe", "Legit", "FastFall"}, "Grim Strafe");
    public static NumberValue speed = new NumberValue("Speed", 0.5, 0.1, 1,0.01);

    @SuppressWarnings("unused")
    @EventTarget
    public void onSlowMovement(SlowMovementEvent event) {
        if (ClientUtils.isNull() || mc.player == null) {
            return;
        }
        if (event.state.getBlock() == Blocks.COBWEB) {
            switch (mode.get()) {
                case "Cancel":
                    event.cancelEvent();
                    break;
                case "GrimStrafe":
                    if (MovementUtils.isMove()) {
                        if (!mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed() || mc.options.jumpKey.isPressed() && mc.options.sneakKey.isPressed()) {
                            strafe(0.64);
                        } else {
                            strafe(0.61);
                        }
                    }
                    if (mc.options.jumpKey.isPressed() && mc.options.sneakKey.isPressed()) {
                        return;
                    }
                    if (mc.options.jumpKey.isPressed()) {
                        mc.player.setVelocity(mc.player.getVelocity().x, 1.4F, mc.player.getVelocity().z);
                    }

                    if (mc.options.sneakKey.isPressed()) {
                        mc.player.setVelocity(mc.player.getVelocity().x, -3.77F, mc.player.getVelocity().z);
                    }
                    break;
            }
        }
    }

    @EventTarget
    public void onUpdate(MotionEvent event) {
        if (mc.player == null && event.eventState != CancellableEvent.EventState.POST) {
            return;
        }

        if (mode.get().equals("FastFall") && isPlayerInWeb()) {
            double deltaY = mc.player.getY() - mc.player.prevY;

            if (deltaY > 0) {
                Vec3d vel = mc.player.getVelocity();

                mc.player.setVelocity(
                        vel.x,
                        -Math.abs(deltaY * 20.0),
                        vel.z
                );
            }
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (mc.player == null) return;

        if (isPlayerInWeb()) {
            switch (mode.get()) {
                case "Legit":
                    if (MovementUtils.isMove()) {
                        event.jump = true;
                    }
                    break;
                case "FastFall":
                    event.jump = true;
            }
        }
    }

    private boolean isPlayerInWeb() {
        if (mc.player == null || mc.world == null) return false;

        Box box = mc.player.getBoundingBox();

        int minX = MathHelper.floor(box.minX);
        int maxX = MathHelper.floor(box.maxX);
        int minY = MathHelper.floor(box.minY);
        int maxY = MathHelper.floor(box.maxY);
        int minZ = MathHelper.floor(box.minZ);
        int maxZ = MathHelper.floor(box.maxZ);

        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.set(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void strafe(double speed) {
        if (mc.player == null) return;

        double yaw = getDirection();

        mc.player.setVelocity(-Math.sin(yaw) * speed, mc.player.getVelocity().y, Math.cos(yaw) * speed);
    }
    private double getDirection() {
        if (mc.player == null) return 0;

        var rotationYaw = RotationManager.serverRotation.getYaw();

        if (mc.player.input.movementForward < 0F) {
            rotationYaw += 180F;
        }

        var forward = 1F;

        if (mc.player.input.movementForward < 0F) {
            forward = -0.5F;
        } else if (mc.player.input.movementForward > 0F) {
            forward = 0.5F;
        }

        if (mc.player.input.movementSideways > 0F) {
            rotationYaw -= 90F * forward;
        }

        if (mc.player.input.movementSideways < 0F) {
            rotationYaw += 90F * forward;
        }

        return Math.toRadians(rotationYaw);
    }
}
