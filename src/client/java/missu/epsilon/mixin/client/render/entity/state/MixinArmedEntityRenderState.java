package missu.epsilon.mixin.client.render.entity.state;

import missu.epsilon.client.utils.entity.ItemSpoofUtils;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

import static missu.epsilon.client.utils.Wrapper.mc;

@Renamer(obfuscated = false)
@Mixin(ArmedEntityRenderState.class)
public class MixinArmedEntityRenderState {

    /**
     * @author Jon_awa
     * @reason For item spoof
     */
    @Inject(method = "updateRenderState", at = @At("HEAD"), cancellable = true)
    private static void updateRenderState(LivingEntity entity, ArmedEntityRenderState state, ItemModelManager itemModelManager, CallbackInfo ci) {
        if (entity == mc.player) {
            state.mainArm = entity.getMainArm();
            itemModelManager.updateForLivingEntity(state.rightHandItemState, getStackInArm(Arm.RIGHT), ModelTransformationMode.THIRD_PERSON_RIGHT_HAND, false, entity);
            itemModelManager.updateForLivingEntity(state.leftHandItemState, getStackInArm(Arm.LEFT), ModelTransformationMode.THIRD_PERSON_LEFT_HAND, true, entity);
            ci.cancel();
        }
    }

    @Unique
    private static ItemStack getStackInArm(Arm arm) {
        if (mc.player != null) {
            return mc.player.getMainArm() == arm ? ItemSpoofUtils.getSpoofedStack() : mc.player.getOffHandStack();
        }
        return null;
    }

}
