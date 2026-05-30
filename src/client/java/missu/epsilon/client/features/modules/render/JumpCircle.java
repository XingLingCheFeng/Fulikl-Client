package missu.epsilon.client.features.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.events.render.Render3DEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.miscs.TimerUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static missu.epsilon.client.utils.Wrapper.mc;
import static missu.epsilon.client.utils.render.ColorUtils.applyOpacity;

@ModuleInfo(name = "JumpCircle", category = ModuleCategory.RENDER)
public class JumpCircle extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Default","Portal"}, "Default");
    private final BoolValue easeOut = new BoolValue("EaseOut", true);
    private final NumberValue rotateSpeed = new NumberValue("RotateSpeed", 2f, 0.5f, 5f,0.5f);
    private final NumberValue circleScale = new NumberValue("CircleScale", 1f, 0.5f, 5f,0.5f);
    private final BoolValue onlySelf = new BoolValue("OnlySelf", false);
    private final List<Circle> circles = new ArrayList<>();
    private final List<PlayerEntity> cache = new CopyOnWriteArrayList<>();
    public static final Identifier hitbubble = Identifier.of("epsilon", "particles/hitbubble.png");
    public static final Identifier default_circle = Identifier.of("epsilon", "particles/circle.png");

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        for (PlayerEntity pl : mc.world.getPlayers())
            if (!cache.contains(pl) && pl.isOnGround() && (mc.player == pl || !onlySelf.get()))
                cache.add(pl);

        cache.forEach(pl -> {
            if (pl != null && !pl.isOnGround()) {
                circles.add(new Circle(new Vec3d(pl.getX(), (int) Math.floor(pl.getY()) + 0.001f, pl.getZ()), new TimerUtils()));
                cache.remove(pl);
            }
        });

        circles.removeIf(c -> c.timer.hasTimeElapsed(easeOut.get() ? 5000 : 6000));
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        MatrixStack stack = event.getMatrixStack();
        Collections.reverse(circles);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);

        switch (mode.get()) {
            case "Portal" -> RenderSystem.setShaderTexture(0, hitbubble);
            case "Default" -> RenderSystem.setShaderTexture(0, default_circle);
        }

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (Circle c : circles) {
            float colorAnim = (float) (c.timer.getTime()) / 6000f;
            float sizeAnim = circleScale.getValue().floatValue() - (float) Math.pow(1 - ((c.timer.getTime() * (easeOut.get() ? 2f : 1f)) / 5000f), 4);

            stack.push();
            stack.translate(c.pos().x - mc.getEntityRenderDispatcher().camera.getPos().getX(), c.pos().y - mc.getEntityRenderDispatcher().camera.getPos().getY(), c.pos().z - mc.getEntityRenderDispatcher().camera.getPos().getZ());
            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sizeAnim * rotateSpeed.getValue().floatValue() * 1000f));
            float scale = sizeAnim * 2f;
            Matrix4f matrix = stack.peek().getPositionMatrix();

            buffer.vertex(matrix, -sizeAnim, -sizeAnim + scale, 0).texture(0, 1).color(applyOpacity(new Color(255,255,255), 1f - colorAnim).getRGB());
            buffer.vertex(matrix, -sizeAnim + scale, -sizeAnim + scale, 0).texture(1, 1).color(applyOpacity(new Color(255,255,255), 1f - colorAnim).getRGB());
            buffer.vertex(matrix, -sizeAnim + scale, -sizeAnim, 0).texture(1, 0).color(applyOpacity(new Color(255,255,255), 1f - colorAnim).getRGB());
            buffer.vertex(matrix, -sizeAnim, -sizeAnim, 0).texture(0, 0).color(applyOpacity(new Color(255,255,255), 1f - colorAnim).getRGB());

            stack.pop();
        }

        RenderUtils.endBuilding(buffer);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();
        Collections.reverse(circles);
    }


    public record Circle(Vec3d pos, TimerUtils timer) {
    }
}