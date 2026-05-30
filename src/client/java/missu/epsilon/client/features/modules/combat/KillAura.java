package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.AttackEvent;
import missu.epsilon.client.event.events.game.ClickEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.EnumAutoDisableType;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.player.Blink;
import missu.epsilon.client.features.modules.world.BedBreaker;
import missu.epsilon.client.features.modules.world.Scaffold;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.management.rotation.Priority;
import missu.epsilon.client.management.rotation.SmoothMode;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.entity.*;
import missu.epsilon.client.utils.miscs.TimerUtils;
import missu.epsilon.client.utils.network.ServerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.GameMode;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static missu.epsilon.client.utils.client.VecCalculation.newGetDistanceToEntityBoxSafe;
import static missu.epsilon.client.utils.client.VecCalculation.newGetPointToEntityBoxSafe;

/**
 * Created by Daniel On 01/31/2021
 */
@ModuleInfo(name = "KillAura", description = "Automatically hit entity within attack range" ,category = ModuleCategory.COMBAT, autoDisable = EnumAutoDisableType.GAME_END)
public class KillAura extends Module {
    public static NumberValue cps = new NumberValue("CPS",10, 1, 20,1);
    public static ListValue mode = new ListValue("Mode",new String[]{"Single","Switch"},"Single");
    public static NumberValue range = new NumberValue("AttackRange", 3.2, 0, 6,0.01);
    public static NumberValue throughRange = new NumberValue("ThroughWallRange", 2, 0, 6,0.01);
    public static ListValue autoBlock = new ListValue("AutoBlock", new String[]{"Hypixel(Enable NoSlow)","Fake","Off"},"Off");
    public static NumberValue blockRange = new NumberValue("BlockRange", 4, 0, 6,0.01).displayable(() -> !autoBlock.get().equals("Off"));
    public static ListValue strafe = new ListValue("Strafe",new String[]{"Silent","Strict"},"Silent");
    public static ListValue keepSprint = new ListValue("KeepSprint",new String[]{"Off","Vanilla"},"Off");
    public static NumberValue rotationSpeed = new NumberValue("RotationSpeed",180,0,180,1);
    public static MultiBoolValue particles = new MultiBoolValue("AttackParticles",new BoolValue[]{
            new BoolValue("Sharpness",true),
            new BoolValue("Critical",false)
    });

    public static BoolValue displayBlockRate = new BoolValue("DisplayEnemyBlockingRate",false);
    public static BoolValue scaffoldCheck = new BoolValue("ScaffoldCheck",false);
    public static BoolValue disableInLobby = new BoolValue("DisableInLobby",false);

    public static final List<LivingEntity> blockingTargets = new CopyOnWriteArrayList<>();
    public static final List<LivingEntity> targets = new CopyOnWriteArrayList<>();
    public static LivingEntity currentTarget;
    public static boolean realBlock = false;
    public static boolean noWorking = false;
    public static boolean renderBlock = false;
    public static float blockRate;
    public Rotation rotation;

    public static final Queue<Boolean> blockStatus = new LinkedList<>();
    public static final TimerUtils attackTimer = new TimerUtils();

    @Override
    public void onEnable() {
        targets.clear();
        currentTarget = null;
        stopBlocking();
        attackTimer.reset();
    }

    @Override
    public void onDisable() {
        stopBlocking();
        targets.clear();
        currentTarget = null;
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onClick(ClickEvent event) {
        if (mc.player == null || mc.getNetworkHandler() == null || mc.interactionManager == null)
            return;

        if (disableInLobby.get()) {
            if (ServerUtils.isInLobby()){
                return;
            }
        }

        if (cantWork()) {
            noWorking = true;
            stopBlocking();
            targets.clear();
            currentTarget = null;
            return;
        } else noWorking = false;

        updateTargets();

        if (!blockingTargets.isEmpty()) {
            startBlocking();
        }

        if (blockingTargets.isEmpty() && renderBlock) {
            stopBlocking();
        }

        if (realBlock && !(mc.player.getMainHandStack().getItem() instanceof SwordItem)) {
            stopBlocking();
        }

        if (targets.isEmpty() && renderBlock) {
            return;
        }

        selectTarget();

        rotation = RotationUtils.vecToRotation(newGetPointToEntityBoxSafe(mc.player, currentTarget,throughRange.get()),false);
        Client.rotationManager.setRotations(rotation,shouldSilent() ? MovementFix.SILENT : MovementFix.STRICT,true, SmoothMode.ADVANCED,rotationSpeed.get().floatValue(),1,0, Priority.MEDIUM);

        if (calculateBlockingRate(currentTarget) != blockRate && displayBlockRate.get()) {
            ClientUtils.displayChat(calculateBlockingRate(currentTarget) * 100 + "%");
            blockRate = calculateBlockingRate(currentTarget);
        }

        if (!isAimedAt(currentTarget)) {
            return;
        }

        double delay = 800 / cps.get();

        if (attackTimer.hasTimeElapsed(delay)) {
            if (PacketLockUtils.attackAndLock()) {
                Client.getInstance().getEventManager().call(new AttackEvent(currentTarget));
                Objects.requireNonNull(mc.interactionManager).syncSelectedSlot();
                mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(currentTarget, mc.player.isSneaking()));
                if (Objects.requireNonNull(mc.interactionManager).getCurrentGameMode() != GameMode.SPECTATOR && !keepSprint.is("Vanilla")) {
                    mc.player.attack(currentTarget);
                    mc.player.resetLastAttackedTicks();
                }
                if (PacketLockUtils.swingAndLock())
                    mc.player.swingHand(Hand.MAIN_HAND);
                if (particles.get("Critical"))
                    mc.player.addCritParticles(currentTarget);
                if (particles.get("Sharpness"))
                    mc.player.addEnchantedHitParticles(currentTarget);
            }
            attackTimer.reset();
        }
    }

    public boolean shouldSilent() {
        return strafe.is("Silent") && !AntiKnockback.shouldStrict;
    }

    public float calculateBlockingRate(LivingEntity target) {
        if (target.isBlocking()) {
            blockStatus.offer(true);
        } else {
            blockStatus.offer(false);
        }

        while (blockStatus.size() > 20) {
            blockStatus.poll();
        }

        return (float) blockStatus.stream().filter(status -> status).count() / blockStatus.size();
    }

    public void startBlocking() {
        if (mc.getNetworkHandler() == null || mc.player == null || mc.interactionManager == null) return;
        if (autoBlock.get().equals("Hypixel(Enable NoSlow)") && mc.player.getMainHandStack().getItem() instanceof SwordItem) {
            realBlock = true;
        }
        if (!autoBlock.get().equals("Off"))
            renderBlock = true;
    }

    public void stopBlocking() {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        if (realBlock) {
            realBlock = false;
        }
        renderBlock = false;
    }

    private Boolean cantWork() {
        if (mc.player != null) {
            return (Client.moduleManager.getModule(Scaffold.class).isEnabled()&& scaffoldCheck.getValue())||mc.player.isSpectator() || !mc.player.isAlive() || Client.moduleManager.getModule(Blink.class).getState() || Client.moduleManager.getModule(BedBreaker.class).getState() && (BedBreaker.breakState == BedBreaker.BreakState.PREPARE || BedBreaker.breakState == BedBreaker.BreakState.FINISHING) && !BedBreaker.noHit.get();
        }
        else return true;
    }
    private void updateTargets() {
        if (mc.world == null || mc.player == null)
            return;

        targets.removeIf(e -> {
            if (e.isDead()) return true;
            if (!Client.targetManager.isTarget(e, false)) return true;

            boolean stillExists = mc.world.getEntityById(e.getId()) != null;
            if (!stillExists) return true;

            double dist = newGetDistanceToEntityBoxSafe(mc.player,e, throughRange.get());
            return dist > range.get();
        });

        blockingTargets.removeIf(e -> {
            if (!Client.targetManager.isTarget(e, false)) return true;
            if (e.isDead()) return true;

            boolean stillExists = mc.world.getEntityById(e.getId()) != null;
            if (!stillExists) return true;

            double dist = newGetDistanceToEntityBoxSafe(mc.player,e, throughRange.get());
            return dist > blockRange.get();
        });

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof LivingEntity living
                    && living != mc.player
                    && !living.isDead()) {

                if (!Client.targetManager.isTarget(entity, false)) continue;

                double dist = newGetDistanceToEntityBoxSafe(mc.player,living, throughRange.get());

                if (dist <= range.get()) {
                    if (!targets.contains(living)) {
                        targets.add(living);
                    }
                }
                if (dist <= blockRange.get()) {
                    if (!blockingTargets.contains(living)) {
                        blockingTargets.add(living);
                    }
                }
            }
        }
    }

    private void selectTarget() {
        if (targets.isEmpty()) {
            currentTarget = null;
            return;
        }

        if (mode.is("Single")) {
            currentTarget = targets.stream()
                    .min(Comparator.comparingDouble(LivingEntity::getHealth))
                    .orElse(null);
        } else if (mode.is("Switch")) {
            currentTarget = targets.stream()
                    .min(Comparator.comparingDouble(this::ultimateTarget))
                    .orElse(null);
        }
    }

    private double ultimateTarget(Entity entity) {
        if (!RaycastUtils.couldHit(entity, rotation.getYaw(), rotation.getPitch(),range.get().floatValue()) || mc.player == null) {
            return 1000.0;
        } else {
            double distance = mc.player.distanceTo(entity);
            double hurtTime = ((LivingEntity)entity).hurtTime * 6;
            return hurtTime + distance;
        }
    }

    public static boolean isAimedAt(LivingEntity target) {
        if (mc.player == null) return false;

        return Objects.requireNonNull(RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), range.get(), newGetDistanceToEntityBoxSafe(mc.player, target, throughRange.get()) <= throughRange.get())).getType() == HitResult.Type.ENTITY;
    }
}