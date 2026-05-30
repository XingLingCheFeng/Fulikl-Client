package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.AttackEvent;
import missu.epsilon.client.event.events.game.ClickEvent;
import missu.epsilon.client.event.events.game.ReceiveMessageEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.events.player.MoveInputEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.movement.LongJump;
import missu.epsilon.client.features.modules.world.Scaffold;
import missu.epsilon.client.features.value.impl.*;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.management.rotation.Priority;
import missu.epsilon.client.management.rotation.SmoothMode;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.client.VecCalculation;
import missu.epsilon.client.utils.client.security.BypassProtection;
import missu.epsilon.client.utils.entity.*;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import tech.skidonion.obfuscator.annotations.NativeObfuscation;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

import static missu.epsilon.client.Client.moduleManager;
import static missu.epsilon.client.utils.entity.RotationUtils.toRotation;

/**
 * Author Daniel
 * Recode 260118
 */

@ModuleInfo(name = "AntiKnockback",description = "Reduce Server Knockback",category = ModuleCategory.COMBAT)
public class  AntiKnockback extends Module {
    public static ListValue server = new ListValue("Server",new String[]{"Vanilla", "Hypixel"},"Hypixel");
    public static MultiBoolValue hypixelAddons = (MultiBoolValue) new MultiBoolValue("Hypixel Addons",new BoolValue[]{
            new BoolValue("AttackReduce",true),
            new BoolValue("BufferCorrect",true),
            new BoolValue("RotationJumpReset",true),
            new BoolValue("Airpush",true)
    }).displayable(() -> server.is("Hypixel"));
    public static NumberValue tick = (NumberValue) new NumberValue("BufferTick",4,0,10).displayable(() -> server.is("Hypixel") && hypixelAddons.get("BufferCorrect"));
    public static BoolValue onlyPlayer = (BoolValue) new BoolValue("OnlyAttackPlayer",true).displayable(() -> server.is("Hypixel"));
    public static BoolValue riskMode = (BoolValue) new BoolValue("RiskMode(Cause Softlock)",false).displayable(() -> hypixelAddons.get("Airpush"));
    public static BoolValue debug = new BoolValue("Debug",false);

    private static final LinkedBlockingDeque<Packet<ClientPlayPacketListener>> serverPackets = new LinkedBlockingDeque<>();
    /**
     * 向前
     */
    public static boolean forward;
    public static boolean buffer = false;
    public static boolean shouldStrict;
    public static int hitCount;
    public static Rotation oppositeRotation;
    public static int ticksSinceVelocity = -1;
    public static boolean handleReset = false;
    public static boolean receiveVelocity;
    public static int bufferTick = -1;

    @SuppressWarnings("unused")
    @EventTarget
    public void onClick(ClickEvent event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        if (ticksSinceVelocity >= 0) {
            ticksSinceVelocity++;
        }
        if (bufferTick >= 0) {
            bufferTick++;
        }
        if (ticksSinceVelocity >= 10) {
            if (receiveVelocity) receiveVelocity = false;
            shouldStrict = false;
            ticksSinceVelocity = -1;
        }
        if (ticksSinceVelocity >= hitCount) {
            if (receiveVelocity) {
                receiveVelocity = false;
            }
            shouldStrict = false;
        }

        if (server.is("Hypixel") && hypixelAddons.get("RotationJumpReset")) {
            handleJumpReset();
        }

        if (buffer || handleReset) {
            shouldStrict = true;
        }

        EntityHitResult hitResult = RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), KillAura.range.get(), false);
        if (hitResult == null && KillAura.throughRange.get() > 0) {
            hitResult = RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), KillAura.throughRange.get(), true);
        }
        Entity target = (hitResult != null) ? hitResult.getEntity() : null;

        boolean validTarget = (target != null && target != mc.player && (target instanceof PlayerEntity || !onlyPlayer.get()))
                || (KillAura.currentTarget != null && (KillAura.currentTarget instanceof PlayerEntity || !onlyPlayer.get()));

        if ((validTarget || getFarthestLivingEntity(onlyPlayer.get()) != null) && receiveVelocity) {
            shouldStrict = true;
        }

        if (receiveVelocity) {
            if (mc.player.isSprinting() || mc.player.lastSprinting) {
                if (validTarget && hypixelAddons.get("AttackReduce")) {
                    if (PacketLockUtils.attackAndLock()) {
                        if (target != null && target != mc.player) {
                            Client.getInstance().getEventManager().call(new AttackEvent(target));
                            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
                        } else {
                            Client.getInstance().getEventManager().call(new AttackEvent(KillAura.currentTarget));
                            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(KillAura.currentTarget, mc.player.isSneaking()));
                        }

                            mc.player.swingHand(Hand.MAIN_HAND);
                        if (Objects.requireNonNull(mc.interactionManager).getCurrentGameMode() != GameMode.SPECTATOR) {
                            mc.player.setVelocity(mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
                            mc.player.setSprinting(false);
                        }
                    }
                } else if (getFarthestLivingEntity(onlyPlayer.get()) != null && hypixelAddons.get("Airpush")) {

                    if (!Client.bypassProtection.isTrustedEnvironment()){
                        mc.player.setVelocity(mc.player.getVelocity().multiply(0.1,1,0.1));
                    } else {
                        airPush();
                    }
                }
            }
        }
    }


    @NativeObfuscation
    public void airPush(){
        LivingEntity entity = getFarthestLivingEntity(onlyPlayer.get());
        if (entity != null && PacketLockUtils.attackAndLock()) {
            Client.getInstance().getEventManager().call(new AttackEvent(entity));
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(entity, mc.player.isSneaking()));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            if (Objects.requireNonNull(mc.interactionManager).getCurrentGameMode() != GameMode.SPECTATOR) {
                mc.player.setVelocity(mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
                mc.player.setSprinting(false);
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.eventState != CancellableEvent.EventState.POST || mc.player == null) return;

        EntityHitResult hitResult = RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), KillAura.range.get(), false);
        if (hitResult == null && KillAura.throughRange.get() > 0) {
            hitResult = RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), KillAura.throughRange.get(), true);
        }
        Entity attackTarget = (hitResult != null) ? hitResult.getEntity() : null;

        if (buffer) {
            if (bufferTick >= tick.get() || mc.player.isSprinting() || mc.player.lastSprinting || mc.player.hasStatusEffect(StatusEffects.SLOWNESS) || ClientData.clientOnGround() || KillAura.currentTarget == null && attackTarget == null && getFarthestLivingEntity(onlyPlayer.get()) == null) {
                while (!serverPackets.isEmpty()) {
                    Packet<ClientPlayPacketListener> packet = serverPackets.poll();
                    packet.apply(mc.getNetworkHandler());
                }
                if (debug.get())
                    ClientUtils.debug("Used Tick: " + bufferTick);
                receiveVelocity = true;
                ticksSinceVelocity = 0;
                if (ClientData.clientOnGround() && !moduleManager.getModule(Scaffold.class).isEnabled() && hypixelAddons.get("RotationJumpReset")) {
                    if (getClosestLivingEntity(KillAura.range.get(), KillAura.throughRange.get(), onlyPlayer.get()) != null) {
                        LivingEntity target = getClosestLivingEntity(KillAura.range.get(), KillAura.throughRange.get(), onlyPlayer.get());
                        Rotation rotation1 = RotationUtils.toRotation(VecCalculation.newGetPointToEntityBoxSafe(mc.player, Objects.requireNonNull(target), KillAura.throughRange.get()), false);
                        Client.rotationManager.setRotations(new Rotation(oppositeRotation.getYaw(), rotation1.getPitch()), MovementFix.STRICT, true, SmoothMode.ADVANCED, 180F, 3, 0, Priority.MEDIUM);
                    } else {
                        Client.rotationManager.setRotations(oppositeRotation, 3, MovementFix.STRICT);
                    }
                    shouldStrict = true;
                }
                buffer = false;
                bufferTick = -1;
            }
        }
    }

    @EventTarget
    public void onReceiveMessage(ReceiveMessageEvent event) {
        if (event.getMessage().getString().contains("failed") && event.getMessage().getString().contains("Hitboxes") && ticksSinceVelocity >= 0 && server.is("Hypixel")) {
            event.cancelEvent();
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (handleReset || buffer || shouldStrict) {
            event.forward = 1;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }

        if (event.packetState == CancellableEvent.PacketState.RECEIVE) {
            if (event.packet instanceof EntityVelocityUpdateS2CPacket packet) {
                if (packet.getEntityId() == Objects.requireNonNull(mc.player).getId() && !moduleManager.getModule(LongJump.class).isEnabled()) {
                    switch (server.get()) {
                        case "Hypixel":
                            double velocityX = packet.velocityX / 8000.0;
                            double velocityY = packet.velocityY / 8000.0;
                            double velocityZ = packet.velocityZ / 8000.0;
                            boolean falling = velocityY <= 0.0;
                            double strength = new Vec3d(velocityX,0,velocityZ).length();
                            boolean lowStrength = strength < 0.1;

                            if (falling || lowStrength || mc.player.isClimbing() || mc.player.isInFluid()) {
                                if (debug.get())
                                    ClientUtils.debug("Abnormal knockback");
                                return;
                            }

                            Vec3d knockback = new Vec3d(velocityX, 0, velocityZ).normalize();
                            knockback = new Vec3d(knockback.x * -1, 0, knockback.z * -1);

                            hitCount = computeReduceTicks(packet.velocityX,packet.velocityZ);

                            Vec3d lookAt = mc.player.getPos().add(knockback.x, 0, knockback.z);
                            float currentPitch = RotationUtils.getRotationOrElseMC().getPitch();
                            Rotation rotation = toRotation(lookAt, false);
                            rotation = new Rotation(rotation.getYaw(), currentPitch);
                            oppositeRotation = rotation;

                            if (!buffer) {
                                if (hypixelAddons.get("BufferCorrect") && tick.get() != 0) {
                                    EntityHitResult hitResult = RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), KillAura.range.get(), false);
                                    if (hitResult == null && KillAura.throughRange.get() > 0) {
                                        hitResult = RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), KillAura.throughRange.get(), true);
                                    }
                                    Entity attackTarget = (hitResult != null) ? hitResult.getEntity() : null;
                                    if ((attackTarget != null && attackTarget != mc.player && (attackTarget instanceof PlayerEntity ||
                                            !onlyPlayer.get())) || (KillAura.currentTarget != null && (KillAura.currentTarget instanceof PlayerEntity || !onlyPlayer.get())) || (getFarthestLivingEntity(onlyPlayer.get())) != null && hypixelAddons.get("Airpush")) {
                                        if (mc.player.isSprinting() || mc.player.lastSprinting) {
                                            if (ClientData.clientOnGround() && !moduleManager.getModule(Scaffold.class).isEnabled() && hypixelAddons.get("RotationJumpReset")) {
                                                if (getClosestLivingEntity(KillAura.range.get(), KillAura.throughRange.get(), onlyPlayer.get()) != null) {
                                                    LivingEntity target = getClosestLivingEntity(KillAura.range.get(), KillAura.throughRange.get(), onlyPlayer.get());
                                                    Rotation rotation1 = RotationUtils.toRotation(VecCalculation.newGetPointToEntityBoxSafe(mc.player, Objects.requireNonNull(target), KillAura.throughRange.get()), false);
                                                    Client.rotationManager.setRotations(new Rotation(oppositeRotation.getYaw(), rotation1.getPitch()), 3, MovementFix.STRICT);
                                                } else {
                                                    Client.rotationManager.setRotations(oppositeRotation, 3, MovementFix.STRICT);
                                                }
                                                shouldStrict = true;
                                            }
                                            receiveVelocity = true;
                                            ticksSinceVelocity = 0;
                                        } else {
                                            buffer = true;
                                            if (debug.get())
                                                ClientUtils.debug("Buffer");
                                            bufferTick = 0;
                                        }
                                    } else {
                                        if (ClientData.clientOnGround() && !moduleManager.getModule(Scaffold.class).isEnabled() && hypixelAddons.get("RotationJumpReset")) {
                                            if (getClosestLivingEntity(KillAura.range.get(), KillAura.throughRange.get(), onlyPlayer.get()) != null) {
                                                LivingEntity target = getClosestLivingEntity(KillAura.range.get(), KillAura.throughRange.get(), onlyPlayer.get());
                                                Rotation rotation1 = RotationUtils.toRotation(VecCalculation.newGetPointToEntityBoxSafe(mc.player, Objects.requireNonNull(target), KillAura.throughRange.get()), false);
                                                Client.rotationManager.setRotations(new Rotation(oppositeRotation.getYaw(), rotation1.getPitch()), 3, MovementFix.STRICT);
                                            } else {
                                                Client.rotationManager.setRotations(oppositeRotation, 3, MovementFix.STRICT);
                                            }
                                            shouldStrict = true;
                                        }
                                        receiveVelocity = true;
                                        ticksSinceVelocity = 0;
                                    }
                                } else {
                                    if (ClientData.clientOnGround() && !moduleManager.getModule(Scaffold.class).isEnabled() && hypixelAddons.get("RotationJumpReset")) {
                                        if (getClosestLivingEntity(KillAura.range.get(), KillAura.throughRange.get(), onlyPlayer.get()) != null) {
                                            LivingEntity target = getClosestLivingEntity(KillAura.range.get(), KillAura.throughRange.get(), onlyPlayer.get());
                                            Rotation rotation1 = RotationUtils.toRotation(VecCalculation.newGetPointToEntityBoxSafe(mc.player, Objects.requireNonNull(target), KillAura.throughRange.get()), false);
                                            Client.rotationManager.setRotations(new Rotation(oppositeRotation.getYaw(), rotation1.getPitch()), 3, MovementFix.STRICT);
                                        } else {
                                            Client.rotationManager.setRotations(oppositeRotation, 3, MovementFix.STRICT);
                                        }
                                        shouldStrict = true;
                                    }
                                    ticksSinceVelocity = 0;
                                    receiveVelocity = true;
                                }
                            }
                            break;
                        case "Vanilla":
                            event.cancelEvent();
                            break;
                    }
                }
            }

            if (buffer) {
                if (!inInBlackList(event)) {
                    event.cancelEvent();
                    @SuppressWarnings("unchecked")
                    Packet<ClientPlayPacketListener> typedPacket = (Packet<ClientPlayPacketListener>) event.packet;
                    serverPackets.add(typedPacket);
                }
            }
        }
    }
    private int computeReduceTicks(int motionX, int motionZ) {
        double kb = Math.hypot(motionX, motionZ);
        double ticksExact = 0.000643153527 * kb + 2.9419087136;
        int ticks = (int) Math.round(ticksExact);

        if (ticks < 1) ticks = 1;
        if (ticks > 10) ticks = 10;

        return ticks;
    }
    public static boolean inInBlackList(PacketEvent event) {
        if (mc.player == null) return false;

        Packet<?> packet = event.packet;
        return packet instanceof HealthUpdateS2CPacket ||
                packet instanceof PlayerPositionLookS2CPacket ||
                packet instanceof PlaySoundS2CPacket ||
                packet instanceof ChatMessageS2CPacket ||
                packet instanceof DeathMessageS2CPacket ||
                packet instanceof CloseScreenS2CPacket ||
                packet instanceof DamageTiltS2CPacket ||
                packet instanceof TitleS2CPacket ||
                packet instanceof TeamS2CPacket ||
                (packet instanceof EntityAnimationS2CPacket entityAnimationS2CPacket && entityAnimationS2CPacket.getEntityId() != mc.player.getId());
    }

    public static void handleJumpReset() {
        if (mc.player == null) return;
        if (!(mc.currentScreen instanceof InventoryScreen || mc.currentScreen instanceof GenericContainerScreen)) {
            if (ticksSinceVelocity >= 0 && !moduleManager.getModule(Scaffold.class).isEnabled()) {
                handleReset = true;
                if (ticksSinceVelocity <= 2 && ClientData.clientOnGround()) {
                    mc.options.jumpKey.setPressed(true);
                }
                if (ticksSinceVelocity <= 3) {
                    mc.options.forwardKey.setPressed(true);
                    forward = true;
                }
            }
            if (ticksSinceVelocity >= 4 && ticksSinceVelocity <= 9) {
                mc.options.jumpKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode()));
                if (forward) {
                    mc.options.forwardKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode()));
                    forward = false;
                }
                handleReset = false;
            }
        }
    }

    public static LivingEntity getClosestLivingEntity(double range, double throughWallRange , boolean onlyPlayer) {
        if (mc.world == null || mc.player == null) return null;

        LivingEntity target = null;

        double closestDistance = range;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (living instanceof ArmorStandEntity) continue;

            if (onlyPlayer && !(living instanceof PlayerEntity)) continue;

            if (!onlyPlayer) {
                if (!(living instanceof PlayerEntity ||
                        living instanceof MobEntity ||
                        living instanceof VillagerEntity)) {
                    continue;
                }
            }

            double dist = VecCalculation.newGetDistanceToEntityBoxSafe(mc.player,living,throughWallRange);

            if (dist <= closestDistance) {
                closestDistance = dist;
                target = living;
            }
        }

        return target;
    }

    public static LivingEntity getFarthestLivingEntity(boolean onlyPlayer) {
        if (mc.world == null || mc.player == null) return null;

        LivingEntity target = null;
        double farthestDistance = 0;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player) continue;
            if (living instanceof ArmorStandEntity) continue;

            if (onlyPlayer && !(living instanceof PlayerEntity)) continue;

            if (!onlyPlayer) {
                if (!(living instanceof PlayerEntity ||
                        living instanceof MobEntity ||
                        living instanceof VillagerEntity)) {
                    continue;
                }
            }

            double dist = VecCalculation.newGetDistanceToEntityBoxSafe(mc.player, living, Double.MAX_VALUE);

            if (dist < 11 && !riskMode.get()) continue;

            if (dist > farthestDistance) {
                farthestDistance = dist;
                target = living;
            }
        }

        return target;
    }

}