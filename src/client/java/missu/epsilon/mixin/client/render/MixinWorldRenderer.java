package missu.epsilon.mixin.client.render;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.render.Chams;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

import static missu.epsilon.client.utils.Wrapper.mc;
import static org.lwjgl.opengl.GL11C.*;

@Renamer(obfuscated = false)
@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    @Unique
    private boolean isRenderingChams = false;

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private void injectChamsForEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (Client.moduleManager.getModule(Chams.class).getState() && entity != mc.player) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(1f, -1000000F);

            this.isRenderingChams = true;
        }
    }

    @Inject(method = "renderEntity", at = @At("RETURN"))
    private void injectChamsForEntityPost(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (Client.moduleManager.getModule(Chams.class).getState() && this.isRenderingChams && entity != mc.player) {
            glPolygonOffset(1f, 1000000F);
            glDisable(GL_POLYGON_OFFSET_FILL);

            this.isRenderingChams = false;
        }
    }
}
