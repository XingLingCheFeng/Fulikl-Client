package missu.epsilon.client.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.render.font.NanoVGImpl;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static missu.epsilon.client.utils.Wrapper.mc;
import static org.lwjgl.nanovg.NanoVG.*;

public class RenderUtils extends NanoVGImpl {
    private static final float BezierCurve = 0.25f;

    public static Pair<Vec3d, Boolean> project(Matrix4f modelView, Matrix4f projection, Vec3d vector) {
        Vec3d camPos = vector.subtract(mc.gameRenderer.getCamera().getPos());
        Vector4f vec = new Vector4f((float) camPos.x, (float) camPos.y, (float) camPos.z, 1F);

        vec.mul(modelView);
        vec.mul(projection);

        boolean isVisible = vec.w() > 0.0;

        if (vec.w() != 0) {
            vec.x /= vec.w();
            vec.y /= vec.w();
            vec.z /= vec.w();
        }

        double screenX = (vec.x() * 0.5 + 0.5) * mc.getWindow().getScaledWidth();
        double screenY = (0.5 - vec.y() * 0.5) * mc.getWindow().getScaledHeight();

        Vec3d position = new Vec3d(screenX, screenY, vec.z());

        return new Pair<>(position, isVisible);
    }

    public static void drawAppleRoundedRect(float x, float y, float width, float height, ColorPanel color, float radius) {
        radius = Math.max(0f, Math.min(radius, height / 2f));

        float c = radius * BezierCurve;

        NanoVG.nvgBeginPath(context);

        NanoVG.nvgMoveTo(context, x + radius, y);

        NanoVG.nvgLineTo(context, x + width - radius, y);
        NanoVG.nvgBezierTo(context, x + width - c, y, x + width, y + c, x + width, y + radius);

        NanoVG.nvgLineTo(context, x + width, y + height - radius);
        NanoVG.nvgBezierTo(context, x + width, y + height - c, x + width - c, y + height, x + width - radius, y + height);

        NanoVG.nvgLineTo(context, x + radius, y + height);
        NanoVG.nvgBezierTo(context, x + c, y + height, x, y + height - c, x, y + height - radius);

        NanoVG.nvgLineTo(context, x, y + radius);
        NanoVG.nvgBezierTo(context, x, y + c, x + c, y, x + radius, y);

        RenderHelper.fillColor(color);
        NanoVG.nvgFill(context);
    }

    public static void endBuilding(BufferBuilder bb) {
        BuiltBuffer builtBuffer = bb.endNullable();
        if (builtBuffer != null) BufferRenderer.drawWithGlobalProgram(builtBuffer);
    }

    public static Color injectAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
    }

    public static Vec3d interpolatePos(float prevposX, float prevposY, float prevposZ, float posX, float posY, float posZ) {
        double x = prevposX + ((posX - prevposX) * getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = prevposY + ((posY - prevposY) * getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = prevposZ + ((posZ - prevposZ) * getTickDelta()) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        return new Vec3d(x, y, z);
    }

    public static float getTickDelta() {
        return mc.getRenderTickCounter().getTickDelta(true);
    }

    public static void drawImageNative(MatrixStack matrices, float x, float y, float width, float height, Identifier texture) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        RenderSystem.setShaderTexture(0, texture);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        bufferBuilder.vertex(matrix, x, y + height, 0).texture(0, 1);
        bufferBuilder.vertex(matrix, x + width, y + height, 0).texture(1, 1);
        bufferBuilder.vertex(matrix, x + width, y, 0).texture(1, 0);
        bufferBuilder.vertex(matrix, x, y, 0).texture(0, 0);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }


    public static void drawTextureOnEntity(MatrixStack matrices, int xPos, int yPos, int width, int height, float textureWidth, float textureHeight, Entity entity, Identifier texture, boolean rotate, Color c0, Color c1, Color c2, Color c3, float additionalRotation) {
        Vec3d entPos = entity.getLerpedPos(getTickDelta()).add(0, 1, 0);
        Camera camera = mc.getEntityRenderDispatcher().camera;
        Vec3d camPos = camera.getPos();

        double x = entPos.x - camPos.x;
        double y = entPos.y - camPos.y;
        double z = entPos.z - camPos.z;

        Quaternionf cameraRotation = camera.getRotation();

        matrices.push();
        matrices.translate(x, y, z);
        matrices.multiply(cameraRotation);

        if (rotate) {
            matrices.multiply(new Quaternionf().rotationZ((float) Math.toRadians(Math.sin(System.currentTimeMillis() / 800.0) * 360)));
        }

        if (additionalRotation != 0f) {
            matrices.multiply(new Quaternionf().rotationZ((float) Math.toRadians(additionalRotation)));
        }

        matrices.scale(0.03f, 0.03f, 0.03f);

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        float uMin = 0f / textureWidth;
        float vMin = 0f / textureHeight;
        float uMax = width / textureWidth;
        float vMax = height / textureHeight;

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buf.vertex(matrix4f, xPos, yPos, 0).texture(uMin, vMin).color(c0.getRed(), c0.getGreen(), c0.getBlue(), c0.getAlpha());
        buf.vertex(matrix4f, xPos + width, yPos, 0).texture(uMax, vMin).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha());
        buf.vertex(matrix4f, xPos + width, yPos + height, 0).texture(uMax, vMax).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha());
        buf.vertex(matrix4f, xPos, yPos + height, 0).texture(uMin, vMax).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        matrices.pop();
    }


    public static void renderScaledItem(DrawContext context, ItemStack stack, float x, float y, float scale) {
        if (stack.isEmpty()) return;

        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, scale);

        float scaledX = x / scale + 0.65f;
        float scaledY = y / scale;

        RenderUtils.renderItemAtFloatPos(context, stack, scaledX, scaledY);
        context.getMatrices().pop();
    }

    public static void drawTextureOnEntity(MatrixStack matrices, int xPos, int yPos, int width, int height, float textureWidth, float textureHeight, Entity entity, Identifier texture, boolean rotate, Color c0, Color c1, Color c2, Color c3) {
        Vec3d entPos = entity.getLerpedPos(getTickDelta()).add(0, 1, 0);
        Camera camera = mc.getEntityRenderDispatcher().camera;
        Vec3d camPos = camera.getPos();

        double x = entPos.x - camPos.x;
        double y = entPos.y - camPos.y;
        double z = entPos.z - camPos.z;

        Quaternionf cameraRotation = camera.getRotation();

        matrices.push();
        matrices.translate(x, y, z);
        matrices.multiply(cameraRotation);

        if (rotate) {
            matrices.multiply(new Quaternionf().rotationZ((float) Math.toRadians(Math.sin(System.currentTimeMillis() / 800.0) * 360)));
        }

        matrices.scale(0.03f, 0.03f, 0.03f);

        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        float uMin = 0f / textureWidth;
        float vMin = 0f / textureHeight;
        float uMax = width / textureWidth;
        float vMax = height / textureHeight;

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buf.vertex(matrix4f, xPos, yPos, 0).texture(uMin, vMin).color(c0.getRed(), c0.getGreen(), c0.getBlue(), c0.getAlpha());
        buf.vertex(matrix4f, xPos + width, yPos, 0).texture(uMax, vMin).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha());
        buf.vertex(matrix4f, xPos + width, yPos + height, 0).texture(uMax, vMax).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha());
        buf.vertex(matrix4f, xPos, yPos + height, 0).texture(uMin, vMax).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha());
        BufferRenderer.drawWithGlobalProgram(buf.end());
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        matrices.pop();
    }

    public static void drawCircleOutline(BlockPos pos, double radius, int color) {
        if (mc.world == null || mc.player == null) return;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        double x = pos.getX() + 0.5 - cameraPos.x;
        double y = pos.getY() + 0.01 - cameraPos.y;
        double z = pos.getZ() + 0.5 - cameraPos.z;

        float a = (color >> 24 & 255) / 255.0f;
        float r = (color >> 16 & 255) / 255.0f;
        float g = (color >> 8 & 255) / 255.0f;
        float b = (color & 255) / 255.0f;

        MatrixStack matrices = new MatrixStack();
        matrices.push();
        matrices.translate(x, y, z);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.POSITION_COLOR);

        int points = 100;
        for (int i = 0; i <= points; i++) {
            double angle = 2 * Math.PI * i / points;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            buffer.vertex(matrix, (float) dx, 0f, (float) dz).color(r, g, b, a);
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        matrices.pop();
    }

    public static void drawBox(BufferBuilder bufferBuilder, Box box, Matrix4f m, Color c, boolean fix) {
        float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
        float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPos().getX());
        float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPos().getY());
        float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());

        if (!fix) {
            minX = (float) box.minX;
            minY = (float) box.minY;
            minZ = (float) box.minZ;
            maxX = (float) box.maxX;
            maxY = (float) box.maxY;
            maxZ = (float) box.maxZ;
        }

        bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB());

        bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB());

        bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB());

        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB());

        bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB());

        bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB());
        bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB());
        bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB());
    }

    public static void drawBox(MatrixStack matrixStack, Box box, Color color, boolean outline, @Nullable Color outlineColor, boolean cameraTranslate) {
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        double camX = cameraTranslate ? dispatcher.camera.getPos().x : 0;
        double camY = cameraTranslate ? dispatcher.camera.getPos().y : 0;
        double camZ = cameraTranslate ? dispatcher.camera.getPos().z : 0;

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tessellator = Tessellator.getInstance();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float minX = (float) (box.minX - camX);
        float minY = (float) (box.minY - camY);
        float minZ = (float) (box.minZ - camZ);
        float maxX = (float) (box.maxX - camX);
        float maxZ = (float) (box.maxZ - camZ);
        float maxY = (float) (box.maxY - camY);
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();

        if (outline && outlineColor != null) {
            drawBoxLine(matrixStack, box, outlineColor, cameraTranslate);
        }
    }

    public static void drawBox(MatrixStack matrixStack, double x, double y, double z, double width, double height, double depth, Color color, boolean outline, @Nullable Color outlineColor, boolean cameraTranslate) {
        Box box = new Box(x, y, z, x + width, y + height, z + depth);
        drawBox(matrixStack, box, color, outline, outlineColor, cameraTranslate);
    }

    public static void drawBoxLine(MatrixStack matrixStack, Box box, Color color, boolean cameraTranslate) {
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        double camX = cameraTranslate ? dispatcher.camera.getPos().x : 0;
        double camY = cameraTranslate ? dispatcher.camera.getPos().y : 0;
        double camZ = cameraTranslate ? dispatcher.camera.getPos().z : 0;

        float minX = (float) (box.minX - camX);
        float minY = (float) (box.minY - camY);
        float minZ = (float) (box.minZ - camZ);
        float maxX = (float) (box.maxX - camX);
        float maxZ = (float) (box.maxZ - camZ);
        float maxY = (float) (box.maxY - camY);
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float a = color.getAlpha() / 255f;

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, maxX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, minZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, minY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, maxZ).color(r, g, b, a);
        buffer.vertex(matrix, minX, maxY, minZ).color(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }


    public static void drawGradientAppleRoundedRectUD(float x, float y, float width, float height, ColorPanel colorLeft, ColorPanel colorRight, float radius) {
        radius = Math.max(0f, Math.min(radius, height / 2f));

        float c = radius * BezierCurve;

        NanoVG.nvgBeginPath(context);

        NanoVG.nvgMoveTo(context, x + radius, y);

        NanoVG.nvgLineTo(context, x + width - radius, y);
        NanoVG.nvgBezierTo(context, x + width - c, y, x + width, y + c, x + width, y + radius);

        NanoVG.nvgLineTo(context, x + width, y + height - radius);
        NanoVG.nvgBezierTo(context, x + width, y + height - c, x + width - c, y + height, x + width - radius, y + height);

        NanoVG.nvgLineTo(context, x + radius, y + height);
        NanoVG.nvgBezierTo(context, x + c, y + height, x, y + height - c, x, y + height - radius);

        NanoVG.nvgLineTo(context, x, y + radius);
        NanoVG.nvgBezierTo(context, x, y + c, x + c, y, x + radius, y);

        NVGPaint gradient = RenderHelper.createGradient(x, y, x, y + height, colorLeft, colorRight);

        nvgFillPaint(context, gradient);
        NanoVG.nvgFill(context);
    }

    public static void drawGradientAppleRoundedRectLR(float x, float y, float width, float height, ColorPanel colorLeft, ColorPanel colorRight, float radius) {
        radius = Math.max(0f, Math.min(radius, height / 2f));

        float c = radius * BezierCurve;

        NanoVG.nvgBeginPath(context);

        NanoVG.nvgMoveTo(context, x + radius, y);

        NanoVG.nvgLineTo(context, x + width - radius, y);
        NanoVG.nvgBezierTo(context, x + width - c, y, x + width, y + c, x + width, y + radius);

        NanoVG.nvgLineTo(context, x + width, y + height - radius);
        NanoVG.nvgBezierTo(context, x + width, y + height - c, x + width - c, y + height, x + width - radius, y + height);

        NanoVG.nvgLineTo(context, x + radius, y + height);
        NanoVG.nvgBezierTo(context, x + c, y + height, x, y + height - c, x, y + height - radius);

        NanoVG.nvgLineTo(context, x, y + radius);
        NanoVG.nvgBezierTo(context, x, y + c, x + c, y, x + radius, y);

        NVGPaint gradient = RenderHelper.createGradient(x, y, x + width, y + height, colorLeft, colorRight);

        nvgFillPaint(context, gradient);
        NanoVG.nvgFill(context);
    }

    public static void drawRect(float x, float y, float width, float height, ColorPanel color) {
        nvgBeginPath(context);
        nvgRect(context, x, y, width, height);
        RenderHelper.fillColor(color);
        nvgFill(context);
    }

    public static void drawOutlineRect(float x, float y, float width, float height, ColorPanel color, float lineWidth) {
        nvgBeginPath(context);
        nvgRect(context, x, y, width, height);
        nvgStrokeColor(context, colorPanelToNVG(color));
        nvgStrokeWidth(context, lineWidth);
        nvgStroke(context);
    }

    public static NVGColor colorPanelToNVG(ColorPanel c) {
        NVGColor col = NVGColor.calloc();
        // 假设 ColorPanel 的通道是 0-255
        col.r(c.red);
        col.g(c.green);
        col.b(c.blue);
        col.a(c.alpha);
        return col;
    }

    public static void drawGradientRect(float x, float y, float width, float height, ColorPanel colorLeft, ColorPanel colorRight) {
        NVGPaint gradient = RenderHelper.createGradient(x, y, x + width, y + height, colorLeft, colorRight);
        nvgBeginPath(context);
        nvgRect(context, x, y, width, height);
        nvgFillPaint(context, gradient);
        nvgFill(context);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, ColorPanel color, float radius) {
        nvgBeginPath(context);
        nvgRoundedRect(context, x, y, width, height, radius);
        RenderHelper.fillColor(color);
        nvgFill(context);
    }

    public static void drawGradientRoundedRectLR(float x, float y, float width, float height, ColorPanel colorLeft, ColorPanel colorRight, float radius) {
        NVGPaint gradient = RenderHelper.createGradient(x, y, x + width, y + height, colorLeft, colorRight);
        nvgBeginPath(context);
        nvgRoundedRect(context, x, y, width, height, radius);
        nvgFillPaint(context, gradient);
        nvgFill(context);
    }

    public static void drawGradientRoundedRectUD(float x, float y, float width, float height, ColorPanel colorLeft, ColorPanel colorRight, float radius) {
        NVGPaint gradient = RenderHelper.createGradient(x, y, x, y + height, colorLeft, colorRight);
        nvgBeginPath(context);
        nvgRoundedRect(context, x, y, width, height, radius);
        nvgFillPaint(context, gradient);
        nvgFill(context);
    }

    public static void drawRoundedRectCornerLR(float x, float y, float width, float height, ColorPanel color, float a, float b, float c, float d) {
        nvgBeginPath(context);
        nvgRoundedRectVarying(context, x, y, width, height, a, b, c, d);
        RenderHelper.fillColor(color);
        nvgFill(context);
    }

    public static void drawGradientRoundedRectCornerLR(float x, float y, float width, float height, ColorPanel colorLeft, ColorPanel colorRight, float a, float b, float c, float d) {
        NVGPaint gradient = RenderHelper.createGradient(x, y, x + width, y + height, colorLeft, colorRight);
        nvgBeginPath(context);
        nvgRoundedRectVarying(context, x, y, width, height, a, b, c, d);
        nvgFillPaint(context, gradient);
        nvgFill(context);
    }

    public static void drawGradientRoundedRectCornerUD(float x, float y, float width, float height, ColorPanel colorLeft, ColorPanel colorRight, float a, float b, float c, float d) {
        NVGPaint gradient = RenderHelper.createGradient(x, y, x, y + height, colorLeft, colorRight);
        nvgBeginPath(context);
        nvgRoundedRectVarying(context, x, y, width, height, a, b, c, d);
        nvgFillPaint(context, gradient);
        nvgFill(context);
    }

    public static void drawShadow(float x, float y, float width, float height, ColorPanel color, float strength, float cornerRadius) {
        NVGPaint shadowPaint = NVGPaint.calloc();
        NVGColor nvgColorFirst = NVGColor.calloc();
        NVGColor nvgColorSecond = NVGColor.calloc();
        nvgColorFirst.r(color.red).g(color.green).b(color.blue).a(color.alpha);
        nvgColorSecond.r(color.red).g(color.green).b(color.blue).a(0f);
        nvgBoxGradient(context, x, y, width, height, cornerRadius, strength, nvgColorFirst, nvgColorSecond, shadowPaint);
        nvgBeginPath(context);
        nvgRoundedRect(context, x - strength, y - strength, width + 2f * strength, height + 2f * strength, cornerRadius);
        nvgRoundedRect(context, x - strength, y - strength, width + 2f * strength, height + 2f * strength, cornerRadius);
        nvgPathWinding(context, NVG_HOLE);
        nvgFillPaint(context, shadowPaint);
        nvgFill(context);
        shadowPaint.free();
    }

    public static void drawCircle(float x, float y, float radius, ColorPanel color) {
        nvgBeginPath(context);
        nvgCircle(context, x, y, radius);
        RenderHelper.fillColor(color);
        nvgFill(context);
    }

    public static void drawCircleProgressRing(float x, float y, float radius, float strokeWidth, ColorPanel color, float progress) {
        progress = Math.max(0f, Math.min(1f, progress));

        nvgShapeAntiAlias(context, true);

        float startAngle = -90f * (float) Math.PI / 180f;
        float endAngle = startAngle - 2f * (float) Math.PI * progress;

        nvgBeginPath(context);
        nvgArc(context, x, y, radius, startAngle, endAngle, NVG_CCW);
        RenderHelper.strokeColor(color);
        nvgStrokeWidth(context, strokeWidth);
        nvgStroke(context);
    }

    public static void drawCircleOutline(float x, float y, float radius, ColorPanel color, float strokeWidth) {
        nvgBeginPath(context);
        nvgCircle(context, x, y, radius);
        RenderHelper.strokeColor(color);
        nvgStrokeWidth(context, strokeWidth);
        nvgStroke(context);
    }

    public static void drawGradientCircle(float x, float y, float radius, ColorPanel colorInner, ColorPanel colorOuter) {
        NVGPaint gradient = RenderHelper.createRadialGradient(x, y, 0f, radius, colorInner, colorOuter);
        nvgBeginPath(context);
        nvgCircle(context, x, y, radius);
        nvgFillPaint(context, gradient);
        nvgFill(context);
    }

    public static void drawEllipse(float x, float y, float radiusX, float radiusY, ColorPanel color) {
        nvgBeginPath(context);
        nvgEllipse(context, x, y, radiusX, radiusY);
        RenderHelper.fillColor(color);
        nvgFill(context);
    }

    public static void drawEllipseOutline(float x, float y, float radiusX, float radiusY, ColorPanel color, float strokeWidth) {
        nvgBeginPath(context);
        nvgEllipse(context, x, y, radiusX, radiusY);
        RenderHelper.strokeColor(color);
        nvgStrokeWidth(context, strokeWidth);
        nvgStroke(context);
    }

    public static void drawArc(float x, float y, float radius, float startAngle, float endAngle, ColorPanel color, int direction) {
        nvgBeginPath(context);
        nvgArc(context, x, y, radius, startAngle, endAngle, direction);
        RenderHelper.fillColor(color);
        nvgFill(context);
    }

    public static void drawArcOutline(float x, float y, float radius, float startAngle, float endAngle, ColorPanel color, float strokeWidth, int direction) {
        nvgBeginPath(context);
        nvgArc(context, x, y, radius, startAngle, endAngle, direction);
        RenderHelper.strokeColor(color);
        nvgStrokeWidth(context, strokeWidth);
        nvgStroke(context);
    }

    public static void renderItemAtFloatPos(DrawContext context, ItemStack stack, float x, float y) {
        if (stack.isEmpty()) return;

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableDepthTest();
        context.drawItem(stack, 0, 0);
        RenderSystem.enableDepthTest();
        context.getMatrices().pop();
    }
}
