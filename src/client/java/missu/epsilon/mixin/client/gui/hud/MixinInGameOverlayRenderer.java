package missu.epsilon.mixin.client.gui.hud;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.render.AntiBlind;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin(InGameOverlayRenderer.class)
public abstract class MixinInGameOverlayRenderer {

    @Redirect(method = "renderFireOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;color(FFFF)Lnet/minecraft/client/render/VertexConsumer;"))
    private static VertexConsumer injectFireOpacity(VertexConsumer vertexConsumer, float red, float green, float blue, float alpha) {
        return vertexConsumer.color(red, green, blue, ((Client.moduleManager.getModule(AntiBlind.class).getState() && AntiBlind.fireAspect.get()) ? AntiBlind.opacity.get().floatValue() / 100F : 1F) * alpha);
    }

}
