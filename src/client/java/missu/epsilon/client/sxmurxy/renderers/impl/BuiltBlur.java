package missu.epsilon.client.sxmurxy.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import missu.epsilon.client.Client;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.SimpleFrameBufferInstance;
import missu.epsilon.client.sxmurxy.renderers.IRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

import java.util.Objects;

public record BuiltBlur(Matrix4f matrix4f, PositionState positionState, SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float blurRadius) implements IRenderer {

    @SuppressWarnings("resource")
    @Override
    public void render(Matrix4f matrix, float x, float y, float z) {
        if (!Client.moduleManager.getModule(missu.epsilon.client.features.modules.visual.PostProcessing.class).isEnabled()) {
            return;
        }

        float width = this.size.width(), height = this.size.height();
        ShaderProgram shader = RenderSystem.setShader(SimpleFrameBufferInstance.BLUR_SHADER_KEY);
        assert shader != null;
        Objects.requireNonNull(shader.getUniform("Size")).set(width, height);
        Objects.requireNonNull(shader.getUniform("Radius")).set(this.radius.radius1(), this.radius.radius2(), this.radius.radius3(), this.radius.radius4());
        Objects.requireNonNull(shader.getUniform("Smoothness")).set(this.smoothness);
        Objects.requireNonNull(shader.getUniform("BlurRadius")).set(this.blurRadius);

        BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix, x, y, z).color(this.color.color1());
        builder.vertex(matrix, x, y + height, z).color(this.color.color2());
        builder.vertex(matrix, x + width, y + height, z).color(this.color.color3());
        builder.vertex(matrix, x + width, y, z).color(this.color.color4());

        BufferRenderer.drawWithGlobalProgram(builder.end());
    }
}