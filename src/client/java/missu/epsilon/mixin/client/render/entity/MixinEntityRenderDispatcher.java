package missu.epsilon.mixin.client.render.entity;

import com.llamalad7.mixinextras.sugar.Local;
import missu.epsilon.client.features.modules.render.OldHitting;
import missu.epsilon.client.utils.render.LegacyEntityUtils;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

    @Shadow public Camera camera;




    @ModifyArg(method = "renderFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", ordinal = 0), index = 1)
    private float animatium$flameOffset(float original, @Local(argsOnly = true) EntityRenderState entityRenderState) {
        Entity entity = LegacyEntityUtils.getEntityByState(entityRenderState);
        if (entity instanceof PlayerEntity player) {
            final float scale = player.getScale();
            boolean shouldSyncPlayerModelWithEyeHeight = OldHitting.syncPlayerModelWithEyeHeight.get();
            if (shouldSyncPlayerModelWithEyeHeight) {
                original = (PlayerEntity.STANDING_DIMENSIONS.eyeHeight() * scale) - LegacyEntityUtils.lerpCameraPosition(camera);
            }
        }

        return original;
    }
}
