package missu.epsilon.client.utils.entity;

import missu.epsilon.client.Client;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.management.rotation.Priority;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.management.rotation.SmoothMode;
import missu.epsilon.client.utils.Wrapper;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.math.MathUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Vector2f;

import java.util.Random;

import static missu.epsilon.client.utils.Wrapper.mc;
import static missu.epsilon.client.utils.entity.RaycastUtils.getVectorForRotation;

public class RotationUtils {
    private static final double RAD_TO_DEG = 180.0 / Math.PI;

//    public static void register() {
//       // Client.getInstance().getEventManager().registerEvent(new RotationUtils());
//    }

    @Deprecated(forRemoval = true)
    public static void setRotation(Rotation rotation) {
        setRotation(rotation, 0, true, 180, 90);
    }

    @Deprecated(forRemoval = true)
    public static void setRotation(Rotation rotation, int keepLength, boolean silentFix, float rotateSpeed) {
        setRotation(rotation, keepLength, silentFix, (int) (rotateSpeed * 180), (int) (rotateSpeed * 90));
    }

    //pitchSpeed was dropped
    //This is okay cuz no module actually use pitch speed
    @Deprecated(forRemoval = true)
    public static void setRotation(Rotation b, int k, boolean s, int y, int p) {
        MovementFix correction = s ? MovementFix.SILENT : MovementFix.STRICT;
        Client.rotationManager.setRotations(b,correction,true, SmoothMode.ADVANCED,y,k,0, Priority.MEDIUM);
    }

    @Deprecated(forRemoval = true)
    public static void setRotation(Rotation r, int k, boolean s) {
        MovementFix correction = s ? MovementFix.SILENT : MovementFix.STRICT;
        Client.rotationManager.setRotations(r,correction,true, SmoothMode.ADVANCED,180,k,0, Priority.MEDIUM);
    }

    @Deprecated(forRemoval = true)
    public static void setRotationNoSmooth(Rotation r, int k, boolean s) {
        MovementFix correction = s ? MovementFix.SILENT : MovementFix.STRICT;
        Client.rotationManager.setRotations(r,correction,false, SmoothMode.LINEAR,180,k,0, Priority.MEDIUM);
    }

    public static BlockHitResult performRaytrace(BlockPos blockPos, Rotation rotation, double reach) {
        if (ClientUtils.isNull()) return null;

        Vec3d eyes = mc.player.getEyePos();
        Vec3d endPos = eyes.add(getVectorForRotation(rotation.yaw, rotation.pitch).multiply(reach));

        BlockState state = mc.world.getBlockState(blockPos);
        VoxelShape shape = state.getCollisionShape(mc.world, blockPos);

        // Fabric 1.21.4 中直接返回 BlockHitResult
        return shape.raycast(eyes, endPos, blockPos);
    }

    public static Rotation vecToRotation(Vec3d target, boolean predict) {
        if (mc.player == null) {
            return new Rotation(0f, 0f);
        }

        Vec3d eyesPos = mc.player.getEyePos();

        if (predict) {
            eyesPos = eyesPos.add(mc.player.getVelocity());
        }

        double diffX = target.x - eyesPos.x;
        double diffY = target.y - eyesPos.y;
        double diffZ = target.z - eyesPos.z;

        final double distXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * RAD_TO_DEG) - 90.0F;
        float pitch = (float) (-(Math.atan2(diffY, distXZ) * RAD_TO_DEG));

        return new Rotation(yaw, pitch);
    }

    public static Rotation toRotation(Vec3d vec, boolean predict) {
        if (Wrapper.mc.player == null) {
            return new Rotation(0f, 0f);
        }

        Vec3d eyesPos = Wrapper.mc.player.getEyePos();

        if (predict) {
            eyesPos = eyesPos.add(Wrapper.mc.player.getVelocity());
        }

        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;

        float yaw = MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f);
        float pitch = MathHelper.wrapDegrees(-(float) Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))));

        return new Rotation(yaw, pitch);
    }

    public static Vec3d getEntityNearestVec(PlayerEntity player, LivingEntity entity) {
        Vec3d eyePos = player.getEyePos();
        Box targetBox = entity.getBoundingBox();

        double x = MathHelper.clamp(eyePos.x, targetBox.minX, targetBox.maxX);
        double y = MathHelper.clamp(eyePos.y, targetBox.minY, targetBox.maxY);
        double z = MathHelper.clamp(eyePos.z, targetBox.minZ, targetBox.maxZ);

        return new Vec3d(x, y, z);
    }

    public static double getRotationDifference(Rotation a, Rotation b) {
        return Math.hypot(getAngleDifference(a.getYaw(), b.getYaw()), a.getPitch() - b.getPitch());
    }

    public static float getAngleDifference(float a, float b) {
        return ((((a - b) % 360F) + 540F) % 360F) - 180F;
    }

    public static Vec3d getCenter(Box bb) {
        return new Vec3d(bb.minX + (bb.maxX - bb.minX) * 0.5, bb.minY + (bb.maxY - bb.minY) * 0.5, bb.minZ + (bb.maxZ - bb.minZ) * 0.5);
    }

    public static Rotation toRotation(Vec3d eyePos, Vec3d targetPos) {
        double diffX = targetPos.x - eyePos.x;
        double diffY = targetPos.y - eyePos.y;
        double diffZ = targetPos.z - eyePos.z;

        float yaw = MathHelper.wrapDegrees((float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90));
        float pitch = MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))));

        return new Rotation(yaw, MathHelper.clamp(pitch, -90, 90));
    }

    public static Rotation getRotationOrElseMC() {

        if (mc.player != null) {
            return RotationManager.targetRotation == null ? new Rotation(mc.player.getYaw(), mc.player.getPitch()) : RotationManager.targetRotation;
        } else {
            return new Rotation(0, 0);
        }

    }

    public static void updateCurrentCrosshairTarget() {
        mc.gameRenderer.updateCrosshairTarget(1f);
    }

    public static float clampYaw(float yaw, float maxYaw) {
        maxYaw = MathUtils.clamp(maxYaw, 0, 180);

        if (yaw > maxYaw) {
            yaw = maxYaw;
        } else if (yaw < -maxYaw) {
            yaw = -maxYaw;
        }

        return yaw;
    }

    public static float quantize(float angle) {
        return angle - angle % 0.0096f;
    }


    public static Vector2f rotationToPos(Vec3d eyesPos, BlockPos targetPos, Vec3d helpVector) {
        VoxelShape voxelShape = mc.world.getBlockState(targetPos).getSidesShape(mc.world, targetPos);
        Box bb;
        if (voxelShape.isEmpty()) {
            /*      bb = new Box();*/
            bb = new Box(0, 0, 0, 1, 1, 1);
        } else {
            bb = voxelShape.getBoundingBox();
        }
        double height = bb.maxY - bb.minY;
        double xWidth = bb.maxX - bb.minX;
        double zWidth = bb.maxZ - bb.minZ;
        Vec3d hitVec = new Vec3d(targetPos.getX() + bb.minX, targetPos.getY() + bb.minY, targetPos.getZ() + bb.minZ).add(xWidth / 2f, height / 2f, zWidth / 2f);
        Vec3i faceVec = new Vec3i(0, 0, 0);
        Vec3d directionVec = new Vec3d(faceVec.getX() * (xWidth / 2f), faceVec.getY() * (height / 2f), faceVec.getZ() * (zWidth / 2f));
        hitVec = hitVec.add(directionVec);
        double max = 0.4;
        double fixX = 0.0;
        double fixZ = 0.0;
        double fixY = 0.0;
        if (helpVector != null) {
            if (directionVec.getX() == 0) {
                fixX += Math.min(-xWidth / 2f * max, Math.max(xWidth / 2f * max, helpVector.getX() - hitVec.getX()));
            }
            if (directionVec.getY() == 0) {
                fixY += Math.min(-height / 2f * max, Math.max(height / 2f * max, helpVector.getY() - hitVec.getY()));
            }
            if (directionVec.getZ() == 0) {
                fixZ += Math.min(-zWidth / 2f * max, Math.max(zWidth / 2f * max, helpVector.getZ() - hitVec.getZ()));
            }
        } else {
            java.util.Random random = new Random();
            if (directionVec.getX() == 0) {
                fixX += Math.min(-xWidth / 2f * max, Math.max(xWidth / 2f * max, random.nextDouble() * (max + max) - max));
            }
            if (directionVec.getY() == 0) {
                fixY += Math.min(-height / 2f * max, Math.max(height / 2f * max, random.nextDouble() * (max + max) - max));
            }
            if (directionVec.getZ() == 0) {
                fixZ += Math.min(-zWidth / 2f * max, Math.max(zWidth / 2f * max, random.nextDouble() * (max + max) - max));
            }
        }
        hitVec = hitVec.add(fixX, fixY, fixZ);
        return toRotation(hitVec, eyesPos, false);
    }

    public static Vector2f toRotation(final Vec3d vec, final Vec3d eyesPos, final boolean predict) {
        if (predict)
            eyesPos.add(mc.player.getVelocity());

        final double diffX = vec.x - eyesPos.x;
        final double diffY = vec.y - eyesPos.y;
        final double diffZ = vec.z - eyesPos.z;

        return new Vector2f(MathHelper.wrapDegrees(
                (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F
        ), MathHelper.wrapDegrees(
                (float) (-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))
        ));
    }
}