package missu.epsilon.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.MoveEvent;
import missu.epsilon.client.event.events.player.*;
import missu.epsilon.client.features.modules.render.FreeLook;
import missu.epsilon.client.utils.IMixinCameraEntity;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.render.ViewBobbingStorage;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

import java.util.UUID;

import static missu.epsilon.client.utils.Wrapper.mc;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Renamer(obfuscated = false)
@Mixin(Entity.class)
public abstract class MixinEntity implements IMixinCameraEntity, ViewBobbingStorage {

    @Shadow public abstract int getId();
    @Unique private float cameraPitch;
    @Unique private float cameraYaw;
    @Shadow public abstract float getPitch();
    @Shadow public abstract float getYaw();
    @Shadow public abstract Vec3d getVelocity();
    @Shadow public abstract double getX();
    @Shadow public abstract float getYaw(float tickDelta);
    @Shadow public abstract float getPitch(float tickDelta);
    @Shadow public abstract Vec3d getRotationVector(float var1, float var2);
    @Shadow public abstract boolean isOnGround();
    @Shadow public abstract void onLanding();
    @Shadow public Vec3d movementMultiplier;
    @Shadow public abstract UUID getUuid();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();
    @Shadow public abstract World getWorld();
    @Shadow public abstract Vec3d getPos();
    @Override public float getCameraPitch() {
        return cameraPitch;
    }
    @Override public float getCameraYaw() {
        return cameraYaw;
    }
    @Override public void setCameraPitch(float pitch) {
        this.cameraPitch = pitch;
    }
    @Override public void setCameraYaw(float yaw) {
        this.cameraYaw = yaw;
    }


    @Inject(method = {"slowMovement"}, at = {@At("RETURN")})
    private void makeStuckInBlock(BlockState state, Vec3d multiplier, CallbackInfo ci) {
        Entity thisEntity = (Entity) (Object) this;
        if (mc.player == thisEntity) {
            SlowMovementEvent event = new SlowMovementEvent(state, multiplier);
            Client.getInstance().getEventManager().call(event);
            if (event.isCancelled()) {
                this.movementMultiplier = Vec3d.ZERO;
                return;
            }
            this.movementMultiplier = event.movementMultiplier;
        }
    }

    /**
     * @author 性压抑大王
     * @reason missu
     */
    @Overwrite
    public final Vec3d getRotationVec(float p_20253_) {
        float pitch = this.getPitch(p_20253_);
        float yaw = this.getYaw(p_20253_);
        Entity thisEntity = (Entity) (Object) this;

        if (thisEntity == MinecraftClient.getInstance().player) {
            LookEvent lookEvent = new LookEvent(thisEntity, yaw, pitch);
            Client.getInstance().getEventManager().call(lookEvent);
            yaw = lookEvent.yaw;
            pitch = lookEvent.pitch;
        }

        return this.getRotationVector(pitch, yaw);
    }

    /**
     * StrafeEvent
     */
    @Redirect(method = "updateVelocity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d onMovementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        return movementInputToVelocity(movementInput, speed, yaw);
    }

    @Unique
    private Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
        /// movementInput.z : forward
        /// movementInput.x : strafe
        /// speed : friction
        /// yaw : yaw
        StrafeEvent strafeEvent = new StrafeEvent((float) movementInput.z, (float) movementInput.x, speed, yaw);
        Vec3d vec3d = movementInput;
        if (((Object) this) instanceof ClientPlayerEntity) {
            Client.getInstance().getEventManager().call(strafeEvent);
            if (strafeEvent.isCancelled()) {
                return vec3d;
            }
            vec3d = new Vec3d(strafeEvent.strafe, movementInput.y, strafeEvent.forward);
            speed = strafeEvent.friction;
        }
        double d = vec3d.lengthSquared();
        if (d < 1.0E-7) {
            return Vec3d.ZERO;
        } else {
            Vec3d newVec3d = (d > 1.0 ? vec3d.normalize() : vec3d).multiply(speed);
            float f = MathHelper.sin(strafeEvent.yaw * (float) (Math.PI / 180.0));
            float g = MathHelper.cos(strafeEvent.yaw * (float) (Math.PI / 180.0));
            return new Vec3d(newVec3d.x * (double) g - newVec3d.z * (double) f, newVec3d.y, newVec3d.z * (double) g + newVec3d.x * (double) f);
        }
    }

    @ModifyVariable(method = "move", at = @At("HEAD"), argsOnly = true)
    private Vec3d onModifyMovement(Vec3d movement) {
        MoveEvent event = new MoveEvent(movement.x, movement.y, movement.z);
        Client.getInstance().getEventManager().call(event);
        if (event.isCancelled()) {
            return movement;
        }
        return new Vec3d(event.x, event.y, event.z);
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", shift = At.Shift.AFTER))
    private void fuckMojangFallDistance(MovementType type, Vec3d movement, CallbackInfo ci, @Local(ordinal = 1) Vec3d vec3d) {
        Entity entity = (Entity) (Object) this;
        if (entity == mc.player) {
            if (this.isOnGround()) {
                this.onLanding();
            } else if (vec3d.y < 0.0) {
                ClientData.setFallDistance((float) (ClientData.getFallDistance() - vec3d.y));
            }
        }
    }

    @Inject(method = "onLanding", at = @At("HEAD"))
    private void onLanding(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity == mc.player) {
            ClientData.setFallDistance(0);
        }
    }

    @Inject(method = "limitFallDistance", at = @At("HEAD"))
    private void limitFallDistance(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity == mc.player) {
            if (this.getVelocity().getY() > -0.5 && ClientData.getFallDistance() > 1.0F) {
                ClientData.setFallDistance(1.0F);
            }
        }
    }

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getFloat(Ljava/lang/String;)F"))
    private void readNbt(NbtCompound nbt, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity == mc.player) {
            ClientData.setFallDistance(nbt.getFloat("FallDistance"));
        }
    }

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setOnFireFromLava()V", shift = At.Shift.AFTER))
    private void baseTick(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity == mc.player) {
            ClientData.setFallDistance(ClientData.getFallDistance() * 0.5F);
        }
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"))
    private void onPlayerDirectionChange(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        float prevPitch = getPitch();
        float prevYaw = getYaw();
        float pitch = prevPitch + (float) (cursorDeltaY * .15);
        float yaw = prevYaw + (float) (cursorDeltaX * .15);
        pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
        Client.getInstance().getEventManager().call(new PlayerDirectionChangeEvent(prevPitch, prevYaw, pitch, yaw));
    }

    @Inject(method = "updateVelocity",at = @At("TAIL"))
    private void updateVelocity(float speed, Vec3d movementInput, CallbackInfo ci) {
        if (mc.player != null && this.getUuid() == mc.player.getUuid()) {
            MovePostEvent event = new MovePostEvent();
            Client.getInstance().getEventManager().call(event);
        }
    }

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    public void changeCameraLookDirection(double xDelta, double yDelta, CallbackInfo ci) {
        Entity thisEntity = (Entity) (Object) this;
        if (thisEntity instanceof ClientPlayerEntity) {
            FreeLook freeLook = FreeLook.getInstance();
            if (freeLook != null && freeLook.isEnabled() && freeLook.isActive()) {
                float pitchDelta = (float) (yDelta * 0.15);
                float yawDelta = (float) (xDelta * 0.15);
                this.cameraPitch = MathHelper.clamp(this.cameraPitch + pitchDelta, -90.0f, 90.0f);
                this.cameraYaw += yawDelta;
                ci.cancel();
            }
        }
    }


    @Unique
    private float animatium$horizontalSpeed = 0.0F;
    @Unique
    private float animatium$previousHorizontalSpeed = 0.0F;

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tickPortalTeleportation()V", shift = At.Shift.AFTER))
    private void animatium$storePreviousHorizontalSpeed(CallbackInfo ci) {
        this.animatium$previousHorizontalSpeed = this.animatium$horizontalSpeed;
    }

    @Inject(method = "applyMoveEffect", at = @At("HEAD"))
    private void animatium$storeHorizontalSpeed(Entity.MoveEffect movementEmission, Vec3d vec3d, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        this.animatium$horizontalSpeed = this.animatium$horizontalSpeed + (float) vec3d.horizontalLength() * 0.6F;
    }

    @Override
    public float getHorizontalSpeed() {
        return this.animatium$horizontalSpeed;
    }

    @Override
    public float getPreviousHorizontalSpeed() {
        return this.animatium$previousHorizontalSpeed;
    }



}