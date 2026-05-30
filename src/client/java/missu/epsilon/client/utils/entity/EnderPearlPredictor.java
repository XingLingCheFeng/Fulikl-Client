package missu.epsilon.client.utils.entity;

import missu.epsilon.client.utils.Wrapper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;

import java.util.function.BiPredicate;

public class EnderPearlPredictor {
    private Vec3d pos;
    private Vec3d motion;

    private Vec3d lastPos;
    private Vec3d lastMotion;

    public EnderPearlPredictor(Vec3d pos, Vec3d motion) {
        this.pos = pos;
        this.motion = motion;
    }

    public EnderPearlPredictor(Vec3d eyePos, float yaw, float pitch) {
        pos = eyePos.add(0, -0.1, 0);

        float x = -MathHelper.sin(yaw * ((float) Math.PI / 180)) * MathHelper.cos(pitch * ((float) Math.PI / 180));
        float y = -MathHelper.sin((pitch) * ((float) Math.PI / 180));
        float z = MathHelper.cos(yaw * ((float) Math.PI / 180)) * MathHelper.cos(pitch * ((float) Math.PI / 180));
        float uncertainty = 1;
        float power = 1.5f;

        Random random = Random.create();
        motion = new Vec3d(x, y, z).normalize().add(random.nextTriangular(0.0, 0.0172275 * (double) uncertainty), random.nextTriangular(0.0, 0.0172275 * (double) uncertainty), random.nextTriangular(0.0, 0.0172275 * (double) uncertainty)).multiply(power);
        Vec3d vec3d = Wrapper.mc.player.getMovement();
        motion = motion.add(vec3d.x, Wrapper.mc.player.isOnGround() ? 0.0 : vec3d.y, vec3d.z);
    }

    public Vec3d predictCustom(boolean useLast, BiPredicate<Vec3d, Vec3d> predicate) {
        Vec3d pMotion = useLast && lastMotion != null ? lastMotion : motion;
        Vec3d pPos = useLast && lastPos != null ? lastPos : pos;
        while (true) {
            pMotion = pMotion.add(0, -0.03, 0).multiply(0.99);
            pPos = pPos.multiply(pMotion);
            if (predicate.test(pPos, pMotion)) {
                lastMotion = pMotion;
                lastPos = pPos;
                return pPos;
            }
        }
    }

    public Vec3d predictOnlyPos(int tick, boolean useLast) {
        Vec3d pMotion = useLast && lastMotion != null ? lastMotion : motion;
        Vec3d pPos = useLast && lastPos != null ? lastPos : pos;
        for (int i = 0; i < tick; i++) {
            pMotion = pMotion.add(0, -0.03, 0).multiply(0.99);
            pPos = pPos.add(pMotion);
        }

        lastPos = pPos;
        lastMotion = pMotion;
        return pPos;
    }

    public BlockHitResult checkLastPredict() {
        if (lastMotion == null || lastPos == null)
            return null;

        return Wrapper.mc.world.raycast(new RaycastContext(lastPos, lastMotion, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, new EnderPearlEntity(EntityType.ENDER_PEARL, Wrapper.mc.world)));
    }

    public BlockHitResult predict() {
        Vec3d pMotion = motion;
        Vec3d pPos = pos;
        do {
            pMotion = pMotion.add(0, -0.03, 0).multiply(0.99);
            BlockHitResult blockHitResult = Wrapper.mc.world.raycast(new RaycastContext(pPos, (pPos = pPos.add(pMotion)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, new EnderPearlEntity(EntityType.ENDER_PEARL, Wrapper.mc.world)));

            if (blockHitResult != null && !blockHitResult.missed) {
                return blockHitResult;
            }

        } while (!(pPos.getY() < -128));


        return null;
    }

    public BlockHitResult predict(int tick, boolean useLast) {
        Vec3d pMotion = useLast && lastMotion != null ? lastMotion : motion;
        Vec3d pPos = useLast && lastPos != null ? lastPos : pos;
        for (int i = 0; i < tick; i++) {
            pMotion = pMotion.add(0, -0.03, 0).multiply(0.99);
            BlockHitResult blockHitResult = Wrapper.mc.world.raycast(new RaycastContext(pPos, (pPos = pPos.add(pMotion)), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, new EnderPearlEntity(EntityType.ENDER_PEARL, Wrapper.mc.world)));

            if (blockHitResult != null && !blockHitResult.missed) {
                lastPos = pPos;
                lastMotion = pMotion;
                return blockHitResult;
            }
        }

        lastPos = pPos;
        lastMotion = pMotion;
        return null;
    }

    public Vec3d getPos() {
        return pos;
    }

    public Vec3d getMotion() {
        return motion;
    }
}
