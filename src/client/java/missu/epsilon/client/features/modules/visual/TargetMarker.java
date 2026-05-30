package missu.epsilon.client.features.modules.visual;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import missu.epsilon.client.event.events.render.Render3DEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.combat.KillAura;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.animations.ContinualAnimation;
import missu.epsilon.client.utils.render.ColorUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.awt.*;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "TargetMarker", category = ModuleCategory.VISUAL, defaultOn = true)
public class TargetMarker extends Module {
    private final ListValue style = new ListValue("Style", new String[]{"Box", "Circle", "Fire Fly", "Nurik Zapen"}, "Fire Fly");
    private final ListValue color = (ListValue) new ListValue("Color Mode", new String[]{"Sync","Rainbow","White"}, "Sync").displayable(() -> style.is("Nurik Zapen"));
    private final NumberValue length = (NumberValue) new NumberValue("Length", 14, 1, 40, 1).displayable(() -> this.style.is("Fire Fly"));
    private final NumberValue factor = (NumberValue) new NumberValue("Factor", 8, 1, 20, 1).displayable(() -> this.style.is("Fire Fly"));
    private final NumberValue shaking = (NumberValue) new NumberValue("Shaking", 1.8, 1.5, 10, 0.1).displayable(() -> this.style.is("Fire Fly"));
    private final NumberValue amplitude = (NumberValue) new NumberValue("Amplitude", 3, 0.1, 8, 0.1).displayable(() -> this.style.is("Fire Fly"));

    private final Identifier nurikZapen = Identifier.of("epsilon", "icons/nurik_zapen.png");
    private final Identifier fireFly = Identifier.of("epsilon", "icons/fire_fly.png");
    private float circleStep, prevCircleStep;
    private float rotation = 0f;
    private final ContinualAnimation continualAnimation = new ContinualAnimation();

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        var matrices = event.getMatrixStack();

        if (this.style.is("circle")) {
            this.prevCircleStep = this.circleStep;
            this.circleStep += 0.05f;
        }

        LivingEntity target = KillAura.currentTarget;

        if (target == null) return;
        switch (this.style.get()) {
            case "Box" ->
                    RenderUtils.drawBox(matrices, target.getBoundingBox().expand(0.1), ColorUtils.reAlpha(ClientSettings.color(0), 120), false, null, true);
            case "Circle" -> this.drawCircleTargetESP(matrices, target);
            case "Fire Fly" ->
                    this.renderFireFlyTargetESP(this.length.get().intValue(), this.factor.get().intValue(), this.shaking.get().floatValue(), this.amplitude.get().floatValue(), target);
            case "Nurik Zapen" -> {
                this.rotation += 0.05f;
                continualAnimation.animate(this.rotation, 50);

                Color color1, color2, color3, color4;

                if (color.is("Rainbow")) {
                    float hueStep = 0.35f;
                    float baseHue = (System.currentTimeMillis() * 0.001f) % 1.0f;

                    color1 = ColorUtils.reAlpha(ColorUtils.hsvToRgb(baseHue + 0 * hueStep, 1f, 1f), 200);
                    color2 = ColorUtils.reAlpha(ColorUtils.hsvToRgb(baseHue + 1 * hueStep, 1f, 1f), 200);
                    color3 = ColorUtils.reAlpha(ColorUtils.hsvToRgb(baseHue + 2 * hueStep, 1f, 1f), 200);
                    color4 = ColorUtils.reAlpha(ColorUtils.hsvToRgb(baseHue + 3 * hueStep, 1f, 1f), 200);
                } else {
                    if (color.is("Sync")) {
                        color1 = color2 = color3 = color4 = ColorUtils.reAlpha(ClientSettings.color(0), 120);
                    } else {
                        color1 = color2 = color3 = color4 = ColorUtils.reAlpha(Color.white, 120);
                    }
                }

                RenderUtils.drawTextureOnEntity(
                        matrices, -24, -24, 48, 48, 48, 48,
                        target, this.nurikZapen, true,
                        color1, color2, color3, color4, rotation
                );
            }
        }
    }

    private void drawCircleTargetESP(MatrixStack matrixStack, Entity target) {
        if (target == null) return;
        double cs = this.prevCircleStep + (this.circleStep - this.prevCircleStep) * RenderUtils.getTickDelta();
        double prevSinAnim = Math.abs(1 + Math.sin(cs - 0.45f)) / 2;
        double sinAnim = Math.abs(1 + Math.sin(cs)) / 2;
        double x = target.prevX + (target.getX() - target.prevX) * RenderUtils.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = target.prevY + (target.getY() - target.prevY) * RenderUtils.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + prevSinAnim * target.getHeight();
        double z = target.prevZ + (target.getZ() - target.prevZ) * RenderUtils.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double nextY = target.prevY + (target.getY() - target.prevY) * RenderUtils.getTickDelta() - mc.getEntityRenderDispatcher().camera.getPos().getY() + sinAnim * target.getHeight();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        float cos;
        float sin;

        for (int i = 0; i <= 60; i++) {
            cos = (float) (x + Math.cos(i * 6.28 / 60) * target.getWidth() * 0.8);
            sin = (float) (z + Math.sin(i * 6.28 / 60) * target.getWidth() * 0.8);

            bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), cos, (float) nextY, sin).color(ColorUtils.reAlpha(ClientSettings.color(0), 170).getRGB());
            bufferBuilder.vertex(matrixStack.peek().getPositionMatrix(), cos, (float) y, sin).color(ColorUtils.reAlpha(ClientSettings.color(10), 0).getRGB());
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    private void renderFireFlyTargetESP(int espLength, int factor, float shaking, float amplitude, Entity target) {
        if (mc.player != null) {
            Camera camera = mc.gameRenderer.getCamera();

            double tPosX = MathHelper.lerp(RenderUtils.getTickDelta(), target.prevX, target.getX()) - camera.getPos().x;
            double tPosY = MathHelper.lerp(RenderUtils.getTickDelta(), target.prevY, target.getY()) - camera.getPos().y;
            double tPosZ = MathHelper.lerp(RenderUtils.getTickDelta(), target.prevZ, target.getZ()) - camera.getPos().z;
            float iAge = MathHelper.lerp(RenderUtils.getTickDelta(), target.age - 1, target.age);

            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.setShaderTexture(0, this.fireFly);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            boolean canSee = mc.player.canSee(target);

            if (canSee) {
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false);
            } else {
                RenderSystem.disableDepthTest();
            }

            for (int j = 0; j < 3; j++) {
                for (int i = 0; i <= espLength; i++) {
                    double radians = Math.toRadians((((float) i / 1.5f + iAge) * factor + (j * 120)) % (factor * 360));
                    double sinQuad = Math.sin(Math.toRadians(iAge * 2.5f + i * (j + 1)) * amplitude) / shaking;
                    float offset = ((float) i / espLength);

                    MatrixStack matrices = new MatrixStack();
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                    matrices.translate(tPosX + Math.cos(radians) * target.getWidth(), (tPosY + 1 + sinQuad), tPosZ + Math.sin(radians) * target.getWidth());
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                    Matrix4f matrix = matrices.peek().getPositionMatrix();

                    int color = ColorUtils.applyOpacity(ClientSettings.color(0), offset).getRGB();
                    float scale = Math.max(0.24f * (offset), 0.2f);

                    buffer.vertex(matrix, -scale, scale, 0).texture(0f, 1f).color(color);
                    buffer.vertex(matrix, scale, scale, 0).texture(1f, 1f).color(color);
                    buffer.vertex(matrix, scale, -scale, 0).texture(1f, 0).color(color);
                    buffer.vertex(matrix, -scale, -scale, 0).texture(0, 0).color(color);
                }
            }

            BufferRenderer.drawWithGlobalProgram(buffer.end());

            if (canSee) {
                RenderSystem.depthMask(true);
                RenderSystem.disableDepthTest();
            } else {
                RenderSystem.enableDepthTest();
            }

            RenderSystem.disableBlend();
        }
    }
}
