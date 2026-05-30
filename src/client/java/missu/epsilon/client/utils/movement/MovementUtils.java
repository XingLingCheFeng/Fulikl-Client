package missu.epsilon.client.utils.movement;

import missu.epsilon.client.event.events.player.MoveInputEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import static missu.epsilon.client.utils.Wrapper.mc;


public class MovementUtils {
    public static float getSpeed() {
        return (float) Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
    }

    public static void strafe() {
        strafe(getSpeed());
    }

    public static double getHorizontalSpeed() {
        return Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
    }

    public static boolean isMove() {
        return mc.player != null && (mc.player.input.movementForward != 0F || mc.player.input.movementSideways != 0F);
    }
    public static float adjustYaw(float yaw, float forward, float strafe) {
        if (forward < 0.0f) {
            yaw += 180.0f;
        }
        if (strafe != 0.0f) {
            float multiplier = forward == 0.0f ? 1.0f : 0.5f * Math.signum(forward);
            yaw += -90.0f * multiplier * Math.signum(strafe);
        }
        return MathHelper.wrapDegrees(yaw);
    }
    public static int getForwardValue() {
        int forwardValue = 0;
        if (mc.options.forwardKey.isPressed()) {
            ++forwardValue;
        }
        if (mc.options.backKey.isPressed()) {
            --forwardValue;
        }
        return forwardValue;
    }

    public static boolean isOnGround(Entity entity, double height) {
        return entity.getWorld().getBlockCollisions(entity, entity.getBoundingBox().offset(0, -height, 0)).iterator().hasNext();
    }

    public static int getLeftValue() {
        int leftValue = 0;
        if (mc.options.leftKey.isPressed()) {
            ++leftValue;
        }
        if (mc.options.rightKey.isPressed()) {
            --leftValue;
        }
        return leftValue;
    }


    public static void strafe(final double speed) {
        if (!isMove())
            return;


        final double yaw = getDirection();
        mc.player.setVelocity(-Math.sin(yaw) * speed,mc.player.getVelocity().y,Math.cos(yaw) * speed);
    }

    public static void strafe(final double speed, final float yaw) {
        if (!isMove())
            return;


        final double direction = getDirection(yaw);
        mc.player.setVelocity(-Math.sin(direction) * speed,mc.player.getVelocity().y,Math.cos(direction) * speed);
    }
    public static double getDirection() {
        return getDirection(mc.player.getYaw());
    }

    public static void addMotionX(double x) {
        if (mc.player == null) return;
        mc.player.setVelocity(mc.player.getVelocity().x + x,mc.player.getVelocity().y,mc.player.getVelocity().z);
    }

    public static void addMotionY(double y) {
        if (mc.player == null) return;
        mc.player.setVelocity(mc.player.getVelocity().x ,mc.player.getVelocity().y + y,mc.player.getVelocity().z);
    }

    public static void addMotionZ(double z) {
        if (mc.player == null) return;
        mc.player.setVelocity(mc.player.getVelocity().x,mc.player.getVelocity().y,mc.player.getVelocity().z + z);
    }

    public static double getDirection(float yaw) {
        float rotationYaw = yaw;

        if (mc.player.forwardSpeed < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if (mc.player.forwardSpeed < 0F)
            forward = -0.5F;
        else if (mc.player.forwardSpeed > 0F)
            forward = 0.5F;

        if (mc.player.sidewaysSpeed > 0F)
            rotationYaw -= 90F * forward;

        if (mc.player.sidewaysSpeed < 0F)
            rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    /**
     * Gets the players' movement yaw
     */
    public static double getDirection(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    /**
     * Fixes the players movement
     */
    public static void fixMovement(final MoveInputEvent event, final float yaw) {
        final float forward = event.forward;
        final float strafe = event.strafe;

        final double angle = MathHelper.wrapDegrees(Math.toDegrees(getDirection(mc.player.getYaw(), forward, strafe)));
        boolean limit = false;
        if (!mc.player.isSprinting())
            limit = true;

        mc.player.ticksLeftToDoubleTapSprint = -1;

        if (forward == 0 && strafe == 0) {
            return;
        }

        float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

        for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
            for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                if (predictedStrafe == 0 && predictedForward == 0) continue;

                final double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(getDirection(yaw, predictedForward, predictedStrafe)));
                final double difference = Math.abs(angle - predictedAngle);

                if (difference < closestDifference) {
                    closestDifference = (float) difference;
                    closestForward = predictedForward;
                    closestStrafe = predictedStrafe;
                }
            }
        }

        event.forward = /*limit ? Math.max( Math.min(closestForward,0.79f),-0.79f) :*/ (closestForward);
        event.strafe = (closestStrafe);
    }
}
