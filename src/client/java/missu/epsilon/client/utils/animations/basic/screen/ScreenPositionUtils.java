package missu.epsilon.client.utils.animations.basic.screen;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import static missu.epsilon.client.utils.Wrapper.mc;

public class ScreenPositionUtils {

    public static final Matrix4f positionMatrix = new Matrix4f();

    public static final Matrix4f projectMatrix = new Matrix4f();

    public static final Matrix4f modelMatrix = new Matrix4f();

    public static ScreenPosition createScreenPosition(Entity entity) {
        ScreenPosition screenPosition = new ScreenPosition(0.0, 0.0, 0.0, false);

        // 统一使用插值计算，确保缓动效果
        Vec3d positionVector = getScreenPosition(
                new Vec3d(entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true),
                        entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true) + (entity.getHeight() + 0.1),
                        entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)));

        if (positionVector.z > 0.0 && positionVector.z < 1.0) {
            Vector4d position = new Vector4d(positionVector.x, positionVector.y, positionVector.z, 0.0);
            position.x = Math.min(positionVector.x, position.x);
            position.y = Math.min(positionVector.y, position.y);
            position.z = Math.max(positionVector.x, position.z);

            screenPosition.screenX = position.x;
            screenPosition.screenY = position.y;
            screenPosition.screenZ = position.z;

            screenPosition.viewable = true;
        }

        return screenPosition;
    }

    public static ScreenPosition createScreenRightBottomPosition(Entity entity) {
        ScreenPosition screenPosition = new ScreenPosition(0.0f, 0.0f, 0.0f, false);

        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;
        float maxZ = 0.0f;
        boolean viewable = false;

        Vec3d[] corners = {
                new Vec3d(-0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), -0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(-0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), 0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(-0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.getHeight() + 0.1 + entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), -0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(-0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.getHeight() + 0.1 + entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), 0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), -0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), 0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.getHeight() + 0.1 + entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), -0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.getHeight() + 0.1 + entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), 0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true))};
        for (Vec3d corner : corners) {
            Vec3d interpolatedCorner = new Vec3d(entity.prevX + (corner.x - entity.prevX),
                    entity.prevY + (corner.y - entity.prevY),
                    entity.prevZ + (corner.z - entity.prevZ));

            Vec3d screenPos = getScreenPosition(interpolatedCorner);

            if (screenPos.z > 0.0f && screenPos.z < 1.0f) {
                viewable = true;
                if (screenPos.x > maxX) maxX = (float) screenPos.x;
                if (screenPos.y > maxY) maxY = (float) screenPos.y;
                if (screenPos.z > maxZ) maxZ = (float) screenPos.z;
            }
        }

        if (viewable) {
            screenPosition.screenX = maxX;
            screenPosition.screenY = maxY;
            screenPosition.screenZ = maxZ;
            screenPosition.viewable = true;
        }

        return screenPosition;
    }

    public static ScreenPosition createScreenLeftTopPosition(Entity entity) {
        ScreenPosition screenPosition = new ScreenPosition(0.0f, 0.0f, 0.0f, false);

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxZ = 0.0f;
        boolean viewable = false;

        Vec3d[] corners = {
                new Vec3d(-0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), -0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(-0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), 0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(-0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.getHeight() + entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), -0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(-0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.getHeight() + entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), 0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), -0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), 0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.getHeight() + entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), -0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true)),
                new Vec3d(0.5 + entity.prevX + (entity.getPos().x - entity.prevX) * mc.getRenderTickCounter().getTickDelta(true), entity.getHeight() + entity.prevY + (entity.getPos().y - entity.prevY) * mc.getRenderTickCounter().getTickDelta(true), 0.5 + entity.prevZ + (entity.getPos().z - entity.prevZ) * mc.getRenderTickCounter().getTickDelta(true))};

        for (Vec3d corner : corners) {
            Vec3d interpolatedCorner = new Vec3d(entity.prevX + (corner.x - entity.prevX),
                    entity.prevY + (corner.y - entity.prevY),
                    entity.prevZ + (corner.z - entity.prevZ));

            Vec3d screenPos = getScreenPosition(interpolatedCorner);

            if (screenPos.z > 0.0f && screenPos.z < 1.0f) {
                viewable = true;
                if (screenPos.x < minX) minX = (float) screenPos.x;
                if (screenPos.y < minY) minY = (float) screenPos.y;
                if (screenPos.z > maxZ) maxZ = (float) screenPos.z;
            }
        }

        if (viewable) {
            screenPosition.screenX = minX;
            screenPosition.screenY = minY;
            screenPosition.screenZ = maxZ;
            screenPosition.viewable = true;
        }

        return screenPosition;
    }

    public static Vec3d getScreenPosition(Vec3d position) {
        Camera camera = mc.getEntityRenderDispatcher().camera;

        int displayHeight = mc.getWindow().getHeight();

        int[] viewport = new int[4];

        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        Vector3f target = new Vector3f();

        double deltaX = position.x - camera.getPos().x;
        double deltaY = position.y - camera.getPos().y;
        double deltaZ = position.z - camera.getPos().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.0f).mul(positionMatrix);

        Matrix4f matrixProj = new Matrix4f(projectMatrix);
        Matrix4f matrixModel = new Matrix4f(modelMatrix);

        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);

        return new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);
    }

    public static ScreenPosition getScreenPositionbyVec3d(Vec3d position) {
        ScreenPosition screenPosition = new ScreenPosition(0.0, 0.0, 0.0, false);
        Camera camera = mc.getEntityRenderDispatcher().camera;

        int displayHeight = mc.getWindow().getHeight();

        int[] viewport = new int[4];

        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        Vector3f target = new Vector3f();

        double deltaX = position.x - camera.getPos().x;
        double deltaY = position.y - camera.getPos().y;
        double deltaZ = position.z - camera.getPos().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.0f).mul(positionMatrix);

        Matrix4f matrixProj = new Matrix4f(projectMatrix);
        Matrix4f matrixModel = new Matrix4f(modelMatrix);

        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);

        Vec3d positionVector = new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);

        if (positionVector.z > 0.0 && positionVector.z < 1.0) {
            Vector4d vector4d = new Vector4d(positionVector.x, positionVector.y, positionVector.z, 0.0);
            vector4d.x = Math.min(positionVector.x, vector4d.x);
            vector4d.y = Math.min(positionVector.y, vector4d.y);
            vector4d.z = Math.max(positionVector.x, vector4d.z);

            screenPosition.screenX = vector4d.x;
            screenPosition.screenY = vector4d.y;
            screenPosition.screenZ = vector4d.z;

            screenPosition.viewable = true;
        }

        return screenPosition;
    }

}
