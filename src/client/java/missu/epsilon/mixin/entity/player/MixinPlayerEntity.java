package missu.epsilon.mixin.entity.player;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.AttackVelocityEvent;
import missu.epsilon.client.features.modules.render.OldHitting;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.utils.render.RenderRotationAccessor;
import missu.epsilon.client.utils.render.ViewBobbingStorage;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements RenderRotationAccessor {

    @Unique public float renderPitchHead;
    @Unique public float prevRenderPitchHead;

    @Unique public float renderYawHead;
    @Unique public float prevRenderYawHead;

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World level) {
        super(entityType, level);
    }

    @Unique AttackVelocityEvent attackVelocityEvent;

    @Redirect(method = {"attack"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V"))
    private void attack(PlayerEntity instance, boolean sprinting) {
        instance.setSprinting(attackVelocityEvent.sprinting);
    }

    @SuppressWarnings("ParameterCanBeLocal")
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void setVelocity(PlayerEntity instance, Vec3d vec3d){
        attackVelocityEvent = new AttackVelocityEvent();
        Client.getInstance().getEventManager().call(attackVelocityEvent);
        vec3d = instance.getVelocity().multiply(attackVelocityEvent.motion, 1.0, attackVelocityEvent.motion);
        instance.setVelocity(vec3d);
    }

    @Inject(method = "getAttackCooldownProgress", at = @At("HEAD"), cancellable = true)
    private void removeCooldown(float baseTime, CallbackInfoReturnable<Float> cir) {
        if (OldHitting.noCooldown.get()) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setMovementSpeed(F)V", shift = At.Shift.AFTER))
    private void updateBobbingTiltValues(CallbackInfo ci) {
        ViewBobbingStorage bobbingAccessor = (ViewBobbingStorage) this;
        float g = this.isOnGround() || this.getHealth() <= 0.0F ? 0.0F : (float) (Math.atan(-this.getVelocity().y * (double) 0.2F) * 15.0F);
        bobbingAccessor.setBobbingTilt(MathHelper.lerp(0.8F, bobbingAccessor.getBobbingTilt(), g));
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void tickRenderHead(CallbackInfo ci) {
        prevRenderPitchHead = renderPitchHead;
        renderPitchHead = this.getPitch();

        prevRenderYawHead = renderYawHead;
        renderYawHead = this.getYaw();

        if (RotationManager.serverRotation != null){
            renderPitchHead = RotationManager.serverRotation.getPitch();
            renderYawHead = RotationManager.serverRotation.getYaw();
        }
    }

    @Override
    public float getRenderPitchHead() {
        return renderPitchHead;
    }

    @Override
    public float getPrevRenderPitchHead() {
        return prevRenderPitchHead;
    }

    @Override
    public float getRenderYawHead() {
        return renderYawHead;
    }

    @Override
    public float getPrevRenderYawHead() {
        return prevRenderYawHead;
    }




}
