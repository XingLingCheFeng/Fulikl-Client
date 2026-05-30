package missu.epsilon.mixin.client.render.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import missu.epsilon.client.Client;
import missu.epsilon.client.addons.EntityRendererAddon;
import missu.epsilon.client.event.events.render.RenderNameTagsEvent;
import missu.epsilon.client.features.modules.render.OldHitting;
import missu.epsilon.client.utils.render.LegacyEntityUtils;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.skidonion.obfuscator.annotations.Renamer;

@Mixin(EntityRenderer.class)
@Renamer(obfuscated = false)
public class MixinEntityRenderer<T extends Entity, S extends EntityRenderState> implements EntityRendererAddon {

    @Unique public Entity entity;

    @Inject(method = "getDisplayName", at = @At("HEAD")) public void getDisplayName(T entity, CallbackInfoReturnable<Text> callbackInfoReturnable) {
        this.entity = entity;
    }

    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    public void onRenderNameTags(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo callbackInfo) {
        RenderNameTagsEvent renderNameTagsEvent = new RenderNameTagsEvent(entity);
        Client.getInstance().getEventManager().call(renderNameTagsEvent);

        if (entity != null) {
            if (renderNameTagsEvent.isCancelled()) {
                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private <T extends Entity, S extends EntityRenderState> void animatium$saveEntityByState(T entity, S state, float tickDelta, CallbackInfo ci) {
        LegacyEntityUtils.setEntityByState(state, entity);
    }

    @WrapOperation(method = "renderLabelIfPresent", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/state/EntityRenderState;sneaking:Z"))
    private boolean animatium$sneakAnimationWhileFlying(EntityRenderState instance, Operation<Boolean> original) {
        if (OldHitting.sneakAnimationWhileFlying.get() && instance instanceof LivingEntityRenderState livingEntityRenderState) {
            return livingEntityRenderState.sneaking || livingEntityRenderState.isInPose(EntityPose.CROUCHING);
        } else {
            return original.call(instance);
        }
    }

    @SuppressWarnings("AddedMixinMembersNamePattern") @Override public Entity getEntity() {
        return entity;
    }

}
