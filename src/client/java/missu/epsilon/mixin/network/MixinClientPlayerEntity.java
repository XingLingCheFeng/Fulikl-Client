package missu.epsilon.mixin.network;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.GameProfile;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.PostUpdateEvent;
import missu.epsilon.client.event.events.game.TickUpdateEvent;
import missu.epsilon.client.event.events.player.*;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.features.modules.combat.KillAura;
import missu.epsilon.client.features.modules.render.OldHitting;
import missu.epsilon.client.features.modules.movement.NoSlow;
import missu.epsilon.client.utils.entity.ItemUtils;
import net.minecraft.block.Portal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

import java.util.Objects;

@Renamer(obfuscated = false)
@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientPlayerEntity extends PlayerEntity {

    public MixinClientPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow public double lastX;
    @Shadow public double lastBaseY;
    @Shadow public double lastZ;
    @Shadow public float lastYaw;
    @Shadow public float lastPitch;
    @Shadow private boolean lastOnGround;
    @Final @Shadow public ClientPlayNetworkHandler networkHandler;
    @Shadow protected abstract boolean isCamera();
    @Shadow public int ticksSinceLastPositionPacketSent;
    @Shadow private boolean autoJumpEnabled;
    @Shadow private boolean lastHorizontalCollision;
    @Shadow @Final protected MinecraftClient client;
    @Shadow public Input input;
    @Shadow protected abstract boolean canSprint();
    @Shadow protected abstract boolean isBlind();
    @Shadow protected abstract boolean canVehicleSprint(Entity vehicle);
    @Shadow public abstract boolean shouldSlowDown();
    @Shadow protected abstract boolean isRidingCamel();
    @Shadow public int ticksLeftToDoubleTapSprint;
    @Shadow protected abstract void tickNausea(boolean fromPortalEffect);
    @Shadow public abstract Portal.Effect getCurrentPortalEffect();
    @Shadow protected abstract void pushOutOfBlocks(double x, double z);
    @Shadow public int ticksToNextAutoJump;
    @Shadow private boolean falling;
    @Shadow private int underwaterVisibilityTicks;
    @Shadow @Nullable public abstract JumpingMount getJumpingMount();
    @Shadow private int field_3938;
    @Shadow private float mountJumpStrength;
    @Shadow public abstract float getMountJumpStrength();
    @Shadow protected abstract void startRidingJump();
    @Shadow private boolean inSneakingPose;
    @Shadow protected abstract void sendSneakingPacket();
    @Shadow private boolean lastSprinting;
    @Shadow protected abstract boolean isWalking();

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callbackInfo) {
        TickUpdateEvent updateEvent = new TickUpdateEvent();
        Client.getInstance().getEventManager().call(updateEvent);
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendSneakingPacket()V"))
    private boolean sendSneakingAfterSprinting(ClientPlayerEntity instance) {
        return !(Client.moduleManager.getModule(OldHitting.class).getState() && OldHitting.sneakTiming.get());
    }

    @Unique
    private void sendSprintingPacket() {
        boolean bl = this.isSprinting();
        if (bl != this.lastSprinting) {
            ClientCommandC2SPacket.Mode mode = bl ? ClientCommandC2SPacket.Mode.START_SPRINTING : ClientCommandC2SPacket.Mode.STOP_SPRINTING;
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, mode));
            this.lastSprinting = bl;
        }
    }

    /**
     * @author Daniel
     */
    @Unique
    private boolean shouldStopSprinting() {
        return this.isGliding() || this.isBlind() || this.shouldSlowDown() || this.hasVehicle() && !this.isRidingCamel() || (this.isUsingItem() && !(Objects.requireNonNull(Client.moduleManager.getModule(NoSlow.class)).getState() && !NoSlow.slowByWaitingServer && ((this.getMainHandStack().getItem() instanceof SwordItem && (this.isUsingItem() || KillAura.realBlock) && NoSlow.sword.get()) || (ItemUtils.isConsumable(this.getMainHandStack()) && this.isUsingItem() && NoSlow.consume.get()) || (this.getMainHandStack().getItem() instanceof BowItem || this.getMainHandStack().getItem() instanceof CrossbowItem) && this.isUsingItem() && NoSlow.bow.get()))) && !this.hasVehicle() && !this.isSubmergedInWater();
    }

    /**
     * @author Daniel
     */
    @Unique
    private boolean canStartSprinting() {
        return !this.isSprinting() && this.isWalking() && this.canSprint() && (!this.isUsingItem() || (Objects.requireNonNull(Client.moduleManager.getModule(NoSlow.class)).getState() && !NoSlow.slowByWaitingServer && ((this.getMainHandStack().getItem() instanceof SwordItem && (this.isUsingItem() || KillAura.realBlock) && NoSlow.sword.get()) || (ItemUtils.isConsumable(this.getMainHandStack()) && this.isUsingItem() && NoSlow.consume.get()) || (this.getMainHandStack().getItem() instanceof BowItem || this.getMainHandStack().getItem() instanceof CrossbowItem) && this.isUsingItem() && NoSlow.bow.get()))) && !this.isBlind() && (!this.hasVehicle() || this.canVehicleSprint(this.getVehicle())) && !this.isGliding() && (!this.shouldSlowDown() || this.isSubmergedInWater() && !(OldHitting.cancelSwimming.get() && Client.moduleManager.getModule(OldHitting.class).isEnabled()));
    }


    /**
     * @author 226
     * @reason FixMovement
     */
    @Inject(cancellable = true,method = "tickMovement", at = @At("HEAD"))
    public void tickMovement(CallbackInfo ci) {
        ci.cancel();
        UpdateEvent event = new UpdateEvent();
        Client.getInstance().getEventManager().call(event);
        Client.getInstance().getEventManager().call(new TickMovementEvent());
        if (this.ticksLeftToDoubleTapSprint> 0) {
            --this.ticksLeftToDoubleTapSprint;
        }
        if (!(this.client.currentScreen instanceof DownloadingTerrainScreen)) {
            this.tickNausea(this.getCurrentPortalEffect() == Portal.Effect.CONFUSION);
            this.tickPortalCooldown();
        }
        boolean bl = this.input.playerInput.jump();
        boolean bl2 = this.input.playerInput.sneak();
        boolean bl3 = this.isWalking();
        PlayerAbilities playerAbilities = this.getAbilities();
        this.inSneakingPose = !playerAbilities.flying && !this.isSwimming() && !this.hasVehicle() && this.canChangeIntoPose(EntityPose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.canChangeIntoPose(EntityPose.STANDING));
        this.input.tick();
        this.client.getTutorialManager().onMovement(this.input);
        if (shouldStopSprinting()) {
            this.setSprinting(false);
        }
        if ((this.isUsingItem() || KillAura.realBlock) && !this.hasVehicle()) {
            final SlowdownEvent slowDownEvent = new SlowdownEvent(0.2F,0.2F);
            Client.getInstance().getEventManager().call(slowDownEvent);
            Input var10000 = this.input;
            var10000.movementSideways *= slowDownEvent.sideways;
            var10000.movementForward *= slowDownEvent.forward;
            this.ticksLeftToDoubleTapSprint = 0;
        }
        if (this.shouldSlowDown()) {
            float f = (float)this.getAttributeValue(EntityAttributes.SNEAKING_SPEED);
            Input var17 = this.input;
            var17.movementSideways *= f;
            var17.movementForward *= f;
        }
        boolean bl4 = false;
        if (this.ticksToNextAutoJump > 0) {
            --this.ticksToNextAutoJump;
            bl4 = true;
            this.input.jump();
        }
        if (!this.noClip) {
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() - (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() - (double)this.getWidth() * 0.35);
            this.pushOutOfBlocks(this.getX() + (double)this.getWidth() * 0.35, this.getZ() + (double)this.getWidth() * 0.35);
        }
        if (bl2) {
            this.ticksLeftToDoubleTapSprint = 0;
        }
        boolean bl5 = canStartSprinting();
        boolean bl6 = this.hasVehicle() ? Objects.requireNonNull(this.getVehicle()).isOnGround() : this.isOnGround();
        boolean bl7 = !bl2 && !bl3;
        if ((bl6 || this.isSubmergedInWater()) && bl7 && bl5) {
            if (this.ticksLeftToDoubleTapSprint <= 0 && !this.client.options.sprintKey.isPressed()) {
                this.ticksLeftToDoubleTapSprint = 7;
            } else {
                this.setSprinting(true);
            }
        }
        if ((!this.isTouchingWater() || this.isSubmergedInWater() && !(OldHitting.cancelSwimming.get() && Client.moduleManager.getModule(OldHitting.class).isEnabled())) && bl5 && this.client.options.sprintKey.isPressed()) {
            this.setSprinting(true);
        }
        if (this.isSprinting()) {
            boolean bl8 = !this.input.hasForwardMovement() || !this.canSprint();
            boolean bl9 = bl8 || this.horizontalCollision && !this.collidedSoftly || this.isTouchingWater() && !this.isSubmergedInWater() || this.isSubmergedInWater && (OldHitting.cancelSwimming.get() && Client.moduleManager.getModule(OldHitting.class).isEnabled());
            if (this.isSwimming()) {
                if (!this.isOnGround() && !this.input.playerInput.sneak() && bl8 || !this.isTouchingWater()) {
                    this.setSprinting(false);
                }
            } else if (bl9) {
                this.setSprinting(false);
            }
        }
        boolean bl8 = false;
        if (playerAbilities.allowFlying) {
            if (Objects.requireNonNull(this.client.interactionManager).isFlyingLocked()) {
                if (!playerAbilities.flying) {
                    playerAbilities.flying = true;
                    bl8 = true;
                    this.sendAbilitiesUpdate();
                }
            } else if (!bl && this.input.playerInput.jump() && !bl4) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7;
                } else if (!this.isSwimming()) {
                    playerAbilities.flying = !playerAbilities.flying;
                    if (playerAbilities.flying && this.isOnGround()) {
                        this.jump();
                    }

                    bl8 = true;
                    this.sendAbilitiesUpdate();
                    this.abilityResyncCountdown = 0;
                }
            }
        }
        if (this.input.playerInput.jump() && !bl8 && !bl && !this.isClimbing() && this.checkGliding()) {
            this.networkHandler.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
        this.falling = this.isGliding();
        if (this.isTouchingWater() && this.input.playerInput.sneak() && this.shouldSwimInFluids()) {
            this.knockDownwards();
        }
        if (this.isSubmergedIn(FluidTags.WATER)) {
            int i = this.isSpectator() ? 10 : 1;
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks + i, 0, 600);
        } else if (this.underwaterVisibilityTicks > 0) {
            this.isSubmergedIn(FluidTags.WATER);
            this.underwaterVisibilityTicks = MathHelper.clamp(this.underwaterVisibilityTicks - 10, 0, 600);
        }
        if (playerAbilities.flying && this.isCamera()) {
            int i = 0;
            if (this.input.playerInput.sneak()) {
                --i;
            }
            if (this.input.playerInput.jump()) {
                ++i;
            }
            if (i != 0) {
                this.setVelocity(this.getVelocity().add(0.0F, (float)i * playerAbilities.getFlySpeed() * 3.0F, 0.0F));
            }
        }
        JumpingMount jumpingMount = this.getJumpingMount();
        if (jumpingMount != null && jumpingMount.getJumpCooldown() == 0) {
            if (this.field_3938 < 0) {
                ++this.field_3938;
                if (this.field_3938 == 0) {
                    this.mountJumpStrength = 0.0F;
                }
            }
            if (bl && !this.input.playerInput.jump()) {
                this.field_3938 = -10;
                jumpingMount.setJumpStrength(MathHelper.floor(this.getMountJumpStrength() * 100.0F));
                this.startRidingJump();
            } else if (!bl && this.input.playerInput.jump()) {
                this.field_3938 = 0;
                this.mountJumpStrength = 0.0F;
            } else if (bl) {
                ++this.field_3938;
                if (this.field_3938 < 10) {
                    this.mountJumpStrength = (float)this.field_3938 * 0.1F;
                } else {
                    this.mountJumpStrength = 0.8F + 2.0F / (float)(this.field_3938 - 9) * 0.1F;
                }
            }
        } else {
            this.mountJumpStrength = 0.0F;
        }
        Client.getInstance().getEventManager().call(new PostUpdateEvent());
        super.tickMovement();
        if (this.isOnGround() && playerAbilities.flying && !Objects.requireNonNull(this.client.interactionManager).isFlyingLocked()) {
            playerAbilities.flying = false;
            this.sendAbilitiesUpdate();
        }
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "HEAD"), cancellable = true)
    public void onSendMovementPackets(CallbackInfo callbackInfo) {
        MotionEvent movementUpdateEvent = new MotionEvent(CancellableEvent.EventState.PRE, this.getYaw(), this.getPitch(), this.getX(), this.getY(), this.getZ(), this.isOnGround());
        Client.getInstance().getEventManager().call(movementUpdateEvent);
        if (movementUpdateEvent.isCancelled()) return;
        sendSprintingPacket();
        /// ViaFabricFix
        if (Client.moduleManager.getModule(OldHitting.class).getState() && OldHitting.sneakTiming.get()) {
            this.sendSneakingPacket();
        }
        if (this.isCamera()) {
            double d = movementUpdateEvent.x - this.lastX;
            double e = movementUpdateEvent.y - this.lastBaseY;
            double f = movementUpdateEvent.z - this.lastZ;
            double g = movementUpdateEvent.yaw - this.lastYaw;
            double h = movementUpdateEvent.pitch - this.lastPitch;
            this.ticksSinceLastPositionPacketSent++;
            boolean bl = MathHelper.squaredMagnitude(d, e, f) > MathHelper.square(2.0E-4) || this.ticksSinceLastPositionPacketSent >= 20;
            boolean bl2 = g != 0.0 || h != 0.0;
            if (bl && bl2) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(movementUpdateEvent.x, movementUpdateEvent.y, movementUpdateEvent.z, movementUpdateEvent.yaw, movementUpdateEvent.pitch, movementUpdateEvent.onGround, this.horizontalCollision));
            } else if (bl) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(movementUpdateEvent.x, movementUpdateEvent.y, movementUpdateEvent.z, movementUpdateEvent.onGround, this.horizontalCollision));
            } else if (bl2) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(movementUpdateEvent.yaw, movementUpdateEvent.pitch, movementUpdateEvent.onGround, this.horizontalCollision));
            } else if (this.lastOnGround != movementUpdateEvent.onGround || this.lastHorizontalCollision != this.horizontalCollision) {
                this.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(movementUpdateEvent.onGround, this.horizontalCollision));
            }
            if (bl) {
                this.lastX = movementUpdateEvent.x;
                this.lastBaseY = movementUpdateEvent.y;
                this.lastZ = movementUpdateEvent.z;
                this.ticksSinceLastPositionPacketSent = 0;
            }
            if (bl2) {
                this.lastYaw = movementUpdateEvent.yaw;
                this.lastPitch = movementUpdateEvent.pitch;
            }
            this.lastOnGround = this.isOnGround();
            this.lastHorizontalCollision = this.horizontalCollision;
            this.autoJumpEnabled = this.client.options.getAutoJump().getValue();
        }
        movementUpdateEvent.eventState = CancellableEvent.EventState.POST;
        Client.getInstance().getEventManager().call(movementUpdateEvent);
        callbackInfo.cancel();
    }

}