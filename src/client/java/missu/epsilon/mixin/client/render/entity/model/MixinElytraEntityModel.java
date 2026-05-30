package missu.epsilon.mixin.client.render.entity.model;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import missu.epsilon.client.features.modules.render.OldHitting;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ElytraEntityModel.class)
public class MixinElytraEntityModel {
    @ModifyExpressionValue(method = "setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At(value = "CONSTANT", args = "floatValue=3.0"))
    private float animatium$fixSneakTranslationWhileFlying(float original) {
        if (OldHitting.bodyOffset.get()) {
            return 0.0F;
        } else {
            return original;
        }
    }
}
