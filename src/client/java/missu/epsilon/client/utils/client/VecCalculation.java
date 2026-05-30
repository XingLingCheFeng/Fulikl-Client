package missu.epsilon.client.utils.client;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class VecCalculation {

    public static Vec3d getBestAimVec(Entity player, Entity target, float prediction, boolean alwaysHavePoint) {
        Vec3d eyePos = player.getEyePos();
        Box targetBox = target.getBoundingBox();

        if (prediction != 0) {
            eyePos = eyePos.add(player.getVelocity().multiply(prediction));
            targetBox = targetBox.offset(target.getVelocity().multiply(prediction));
        }

        List<Vec3d> possiblePoints = new ArrayList<>();

        Vec3d targetCenter = new Vec3d(
                (targetBox.minX + targetBox.maxX) * 0.5,
                (targetBox.minY + targetBox.maxY) * 0.5,
                (targetBox.minZ + targetBox.maxZ) * 0.5
        );

        for (double i = 0.2; i <= 0.9; i += 0.2) {
            possiblePoints.add(new Vec3d(targetCenter.x, targetBox.minY + target.getHeight() * i, targetCenter.z));
        }

        possiblePoints.add(new Vec3d(targetBox.minX, targetCenter.y, targetCenter.z));
        possiblePoints.add(new Vec3d(targetBox.maxX, targetCenter.y, targetCenter.z));
        possiblePoints.add(new Vec3d(targetCenter.x, targetBox.minY, targetCenter.z));
        possiblePoints.add(new Vec3d(targetCenter.x, targetBox.maxY, targetCenter.z));
        possiblePoints.add(new Vec3d(targetCenter.x, targetCenter.y, targetBox.minZ));
        possiblePoints.add(new Vec3d(targetCenter.x, targetCenter.y, targetBox.maxZ));

        Vec3d bestPoint = null;
        double bestScore = Double.MAX_VALUE;

        for (Vec3d point : possiblePoints) {
            if (!isNotBlocked(player.getWorld(), eyePos, point, player))
                continue;

            double distToEye = eyePos.squaredDistanceTo(point);
            double distToCenter = point.squaredDistanceTo(targetCenter);

            double eyeWeight = 0.7;
            double centerWeight = 0.3;

            double score = distToEye * eyeWeight + distToCenter * centerWeight;

            if (score < bestScore) {
                bestScore = score;
                bestPoint = point;
            }
        }

        if (bestPoint == null && alwaysHavePoint) {
            double x = MathHelper.clamp(eyePos.x, targetBox.minX, targetBox.maxX);
            double y = MathHelper.clamp(eyePos.y, targetBox.minY, targetBox.maxY);
            double z = MathHelper.clamp(eyePos.z, targetBox.minZ, targetBox.maxZ);
            bestPoint = new Vec3d(x, y, z);
        }

        return bestPoint;
    }


    public static Box getHitBox(Entity entity) {
        double borderSize = entity.getTargetingMargin();
        Box box = entity.getBoundingBox();
        return box.expand(borderSize);
    }

    public static Vec3d newGetPointToEntityBoxSafe(
            Entity from,
            Entity to,
            double throughwallRange
    ) {
        Vec3d eye = from.getEyePos();
        World world = from.getWorld();
        Box box = to.getBoundingBox();

        double best = Double.MAX_VALUE;
        Vec3d bestPoint = null;

        Vec3d nearest = getNearestPointBB(eye, box);
        double d = eye.distanceTo(nearest);
        if (d <= throughwallRange) return nearest;

        if (isNotBlocked(world, eye, nearest, from)) {
            return nearest;
        }

        Vec3d[] samples = new Vec3d[] {
                box.getCenter(),
                new Vec3d(box.getCenter().x, box.maxY - 0.1, box.getCenter().z), // head
                to.getEyePos(),
                new Vec3d(box.getCenter().x, (box.minY + box.maxY) * 0.5, box.getCenter().z), // chest
                new Vec3d(box.getCenter().x, box.minY + 0.2, box.getCenter().z) // legs
        };

        for (Vec3d p : samples) {
            double dist = eye.distanceTo(p);
            if (dist >= best) continue;

            if (dist <= throughwallRange || isNotBlocked(world, eye, nearest, from)) {
                best = dist;
                bestPoint = p;
            }
        }

        return bestPoint;
    }


    public static double newGetDistanceToEntityBoxSafe(
            Entity from,
            Entity to,
            double throughwallRange
    ) {
        Vec3d eye = from.getEyePos();
        World world = from.getWorld();
        Box box = to.getBoundingBox();

        double best = Double.MAX_VALUE;

        Vec3d nearest = getNearestPointBB(eye, box);
        double d = eye.distanceTo(nearest);
        if (d <= throughwallRange) return d;
        if (isNotBlocked(world, eye, nearest, from)) {
            best = d;
        }

        Vec3d[] samples = new Vec3d[] {
                box.getCenter(),
                new Vec3d(box.getCenter().x, box.maxY - 0.1, box.getCenter().z), // head
                to.getEyePos(),
                new Vec3d(box.getCenter().x, (box.minY + box.maxY) * 0.5, box.getCenter().z), // chest
                new Vec3d(box.getCenter().x, box.minY + 0.2, box.getCenter().z) // legs
        };

        for (Vec3d p : samples) {
            double dist = eye.distanceTo(p);
            if (dist >= best) continue;

            if (dist <= throughwallRange || isNotBlocked(world, eye, nearest, from)) {
                best = dist;
            }
        }

        return best;
    }

    public static Vec3d getNearestPointBB(Vec3d eye, Box box) {
        double x = eye.x;
        double y = eye.y;
        double z = eye.z;

        if (x > box.maxX) x = box.maxX;
        else if (x < box.minX) x = box.minX;

        if (y > box.maxY) y = box.maxY;
        else if (y < box.minY) y = box.minY;

        if (z > box.maxZ) z = box.maxZ;
        else if (z < box.minZ) z = box.minZ;

        return new Vec3d(x, y, z);
    }

    public static double getDistanceToEntityBox(Entity from, Entity to) {
        Vec3d eyes = from.getEyePos();
        Vec3d nearestPoint = getNearestPointBB(eyes, getHitBox(to));
        return eyes.distanceTo(nearestPoint);
    }

    private static boolean isNotBlocked(World world, Vec3d start, Vec3d end, Entity entity) {
        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity);
        BlockHitResult hitResult = world.raycast(context);
        return hitResult.getType() == HitResult.Type.MISS;
    }

}
