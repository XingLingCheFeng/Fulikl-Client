package missu.epsilon.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.player.JumpEvent;
import missu.epsilon.client.features.modules.render.Animations;
import missu.epsilon.client.features.modules.render.AntiBlind;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.utils.render.ViewBobbingStorage;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements ViewBobbingStorage {

    @Shadow public abstract void setSprinting(boolean sprinting);
    @Unique private float preYaw = 0f;
    @Unique private JumpEvent jumpEvent;
    @Shadow public abstract ItemStack getMainHandStack();

    @Shadow
    public float bodyYaw;


    @Unique
    private float animatium$bobbingTilt = 0.0F;
    @Unique
    private float animatium$previousBobbingTilt = 0.0F;

    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    public void getHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
        if (Client.moduleManager.getModule(Animations.class).isEnabled()) {
            cir.setReturnValue(Animations.swingSpeed.get().intValue());
        }
    }

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void hookAntiNausea(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect == StatusEffects.NAUSEA && !(AntiBlind.nausea.get() && Client.moduleManager.getModule(AntiBlind.class).isEnabled())) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "jump",at = @At("HEAD"),cancellable = true)
    public void jumpPre(CallbackInfo callbackInfo) {
        if((LivingEntity)(Object)this instanceof ClientPlayerEntity playerEntity) {
            jumpEvent = new JumpEvent(playerEntity.getYaw());
            Client.getInstance().getEventManager().call(jumpEvent);
            if (jumpEvent.isCancelled()) {
                callbackInfo.cancel();
                return;
            }
            preYaw = playerEntity.getYaw();
            playerEntity.setYaw(jumpEvent.yaw);
        }
    }

    @Inject(method = "jump",at = @At("TAIL"))
    public void jumpPost(CallbackInfo ci){
        if((LivingEntity)(Object)this instanceof ClientPlayerEntity playerEntity && jumpEvent != null) {
            jumpEvent = null;
            playerEntity.setYaw(preYaw);
        }
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tickStatusEffects()V", shift = At.Shift.BEFORE))
    private void updatePreviousBobbingTiltValue(CallbackInfo ci) {
        this.animatium$previousBobbingTilt = this.animatium$bobbingTilt;
    }

    @Inject(method = "turnHead", at = @At("HEAD"), cancellable = true)
    private void overrideTurnHead(float bodyRotation, float headRotation, CallbackInfoReturnable<Float> cir) {
        Entity entity = (Entity) (Object) this;

        if (!(entity instanceof ClientPlayerEntity)) return;
        if (RotationManager.serverRotation == null) return;

        float targetYaw = RotationManager.serverRotation.getYaw();

        float f = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);
        this.bodyYaw += f * 0.3F;
        float g = MathHelper.wrapDegrees(targetYaw - this.bodyYaw);

        g = MathHelper.clamp(g, -75.0F, 75.0F);
        this.bodyYaw = targetYaw - g;
        if (Math.abs(g) > 50.0F) {
            this.bodyYaw += g * 0.2F;
        }

        boolean bl = g < -90.0F || g >= 90.0F;
        cir.setReturnValue(bl ? -headRotation : headRotation);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;abs(F)F"))
    private float animatium$rotateBackwardsWalking(float value, Operation<Float> original) {
        return 0F;
    }


  /*  @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;abs(F)F"))
    private float animatium$rotateBackwardsWalking(float value, Operation<Float> original) {
        if (AnimatiumClient.isEnabled() && AnimatiumConfig.instance().rotateBackwardsWalking) {
            return 0F;
        } else {
            return original.call(value);
        }
    }*/

    @Override
    public void setBobbingTilt(float bobbingTilt) {
        this.animatium$bobbingTilt = bobbingTilt;
    }

    @Override
    public float getBobbingTilt() {
        return this.animatium$bobbingTilt;
    }

    @Override
    public float getPreviousBobbingTilt() {
        return this.animatium$previousBobbingTilt;
    }



}
