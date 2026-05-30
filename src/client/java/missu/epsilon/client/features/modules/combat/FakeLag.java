package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.WorldEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.world.Scaffold;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.client.VecCalculation;
import missu.epsilon.client.utils.entity.BlinkUtils;
import missu.epsilon.client.utils.entity.ItemUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.UUID;

import static missu.epsilon.client.utils.Wrapper.mc;

/**
 * Author Daniel
 * 251115
 */
@ModuleInfo(name = "FakeLag",description = "Use fake lagging to make advantage",category = ModuleCategory.COMBAT)
public class FakeLag extends Module {
    public static NumberValue time = new NumberValue("PulseInterval(ms)",350,1,1000,1);
    public static NumberValue pulseRange = new NumberValue("PulseDistance",40,0,80);
    public static NumberValue lagRange = new NumberValue("StartLagRange",12,0,20);
    public static NumberValue distance = new NumberValue("Distance",4,1,7,0.01);
    public static NumberValue cancelTime = new NumberValue("NoWorkWhenBeingHitTime(ms)",1000,0,2000,10);

    public static boolean cantWork;
    public static boolean lagging;
    public static TimerUtils timer = new TimerUtils();
    public static TimerUtils delayTimer = new TimerUtils();
    public static OtherClientPlayerEntity clonePlayer;

    @Override
    public void onEnable() {
        if (mc.world == null || mc.player == null) return;

        if (mc.isInSingleplayer()) {
            this.setState(false);
            ClientUtils.displayChat("You can't use FakeLag in singleplayer!");
            return;
        }

        timer.reset();
        BlinkUtils.startBlink();
        lagging = true;

        ClientUtils.displayChat("Lagging...");
        OtherClientPlayerEntity clone = new OtherClientPlayerEntity(mc.world,mc.player.getGameProfile());
        clone.headYaw = mc.player.headYaw;
        clone.copyPositionAndRotation(mc.player);
        clone.setSprinting(mc.player.isSprinting());
        clone.setUuid(UUID.randomUUID());
        clonePlayer = clone;
    }

    @Override
    public void onDisable() {
        if (mc.world == null) return;
        if (clonePlayer != null) clonePlayer = null;

        timer.reset();
        BlinkUtils.stopBlink();
        lagging = false;
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onWorld(WorldEvent event) {
        setState(false);
        onDisable();
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.world == null || mc.player == null) return;

        if (cantWork) {
            if (delayTimer.hasTimeElapsed(cancelTime.get())) {
                cantWork = false;
                ClientUtils.displayChat("Start lagging now!");
            }
            return;
        }

        if (projectileIncoming(clonePlayer,15) && lagging) {
            BlinkUtils.releaseTick(clonePlayer);
            ClientUtils.displayChat("Release 1 tick");
        }

        if (KillAura.currentTarget != null) {
            if (lagging) {
                BlinkUtils.stopBlink();
                ClientUtils.displayChat("Stop to attack");
                clonePlayer.headYaw = mc.player.headYaw;
                clonePlayer.copyPositionAndRotation(mc.player);
                clonePlayer.setSprinting(mc.player.isSprinting());
                timer.reset();
                lagging = false;
            }
            return;
        }

        if (mc.currentScreen instanceof GenericContainerScreen || mc.currentScreen instanceof InventoryScreen) {
            if (lagging) {
                BlinkUtils.stopBlink();
                clonePlayer.headYaw = mc.player.headYaw;
                clonePlayer.copyPositionAndRotation(mc.player);
                clonePlayer.setSprinting(mc.player.isSprinting());
                timer.reset();
                lagging = false;
            }
            return;
        }

        if (Client.moduleManager.getModule(Scaffold.class).getState()) {
            if (lagging) {
                BlinkUtils.stopBlink();
                ClientUtils.displayChat("Stop to scaffold");
                clonePlayer.headYaw = mc.player.headYaw;
                clonePlayer.copyPositionAndRotation(mc.player);
                clonePlayer.setSprinting(mc.player.isSprinting());
                timer.reset();
                lagging = false;
            }
            return;
        }
        if (mc.player.isUsingItem() && ItemUtils.isConsumable(mc.player.getMainHandStack()) || mc.player.getOffHandStack().getItem() == Items.SNOWBALL || mc.player.getOffHandStack().getItem() == Items.EGG) {
            if (lagging) {
                BlinkUtils.stopBlink();
                ClientUtils.displayChat("UsingItem -> Release");
                clonePlayer.headYaw = mc.player.headYaw;
                clonePlayer.copyPositionAndRotation(mc.player);
                clonePlayer.setSprinting(mc.player.isSprinting());
                timer.reset();
                lagging = false;
            }
            return;
        }

        for (PlayerEntity target : mc.world.getPlayers()) {
            if (target == mc.player || target == clonePlayer) continue;
            if (VecCalculation.getDistanceToEntityBox(mc.player,target) <= distance.get() || (VecCalculation.getDistanceToEntityBox(clonePlayer,target) <= distance.get()) && lagging) {
                if (lagging) {
                    ClientUtils.displayChat("Too close to stop");
                    BlinkUtils.stopBlink();
                    clonePlayer.headYaw = mc.player.headYaw;
                    clonePlayer.copyPositionAndRotation(mc.player);
                    clonePlayer.setSprinting(mc.player.isSprinting());
                    timer.reset();
                    lagging = false;
                }
                return;
            }
        }

        if (timer.hasTimeElapsed(time.get())) {
            BlinkUtils.stopBlink();
            lagging = false;
            clonePlayer.headYaw = mc.player.headYaw;
            clonePlayer.copyPositionAndRotation(mc.player);
            clonePlayer.setSprinting(mc.player.isSprinting());
            BlinkUtils.startBlink();
            lagging = true;
            timer.reset();
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (event.packet instanceof EntityVelocityUpdateS2CPacket packet) {
            if (packet.getEntityId() == mc.player.getId()) {
                if (lagging) {
                    ClientUtils.displayChat("Being hit! DelayLagging");
                    BlinkUtils.stopBlink();
                    lagging = false;
                    delayTimer.reset();
                    cantWork = true;
                }
            }
        }
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            ClientUtils.displayChat("Receive Flag, Disable module.");
            setState(false);
        }
    }

    public boolean projectileIncoming(PlayerEntity player, double maxCheckDistance) {
        if (player == null || player.getWorld() == null) return false;

        World world = player.getWorld();
        Vec3d playerPos = player.getPos();

        Box expandedPlayerBox = player.getBoundingBox().expand(0.2);

        for (FishingBobberEntity arrow : world.getEntitiesByClass(
                FishingBobberEntity.class,
                player.getBoundingBox().expand(maxCheckDistance),
                a -> !a.onGround
        )) {
            Vec3d arrowPos = arrow.getPos();
            Vec3d arrowVel = arrow.getVelocity();

            if (arrowVel.lengthSquared() < 0.0001) continue;

            Vec3d dirToPlayer = playerPos.subtract(arrowPos).normalize();

            if (arrowVel.normalize().dotProduct(dirToPlayer) <= 0) {
                continue;
            }

            Vec3d nextArrowPos = arrowPos.add(arrowVel);

            Optional<Vec3d> collide = expandedPlayerBox.raycast(arrowPos, nextArrowPos);

            if (collide.isPresent()) {
                return true;
            }
        }
        for (ArrowEntity arrow : world.getEntitiesByClass(
                ArrowEntity.class,
                player.getBoundingBox().expand(maxCheckDistance),
                a -> !a.onGround
        )) {
            Vec3d arrowPos = arrow.getPos();
            Vec3d arrowVel = arrow.getVelocity();

            if (arrowVel.lengthSquared() < 0.0001) continue;

            Vec3d dirToPlayer = playerPos.subtract(arrowPos).normalize();

            if (arrowVel.normalize().dotProduct(dirToPlayer) <= 0) {
                continue;
            }

            Vec3d nextArrowPos = arrowPos.add(arrowVel);

            Optional<Vec3d> collide = expandedPlayerBox.raycast(arrowPos, nextArrowPos);

            if (collide.isPresent()) {
                return true;
            }
        }
        for (SnowballEntity arrow : world.getEntitiesByClass(
                SnowballEntity.class,
                player.getBoundingBox().expand(maxCheckDistance),
                a -> !a.onGround
        )) {
            Vec3d arrowPos = arrow.getPos();
            Vec3d arrowVel = arrow.getVelocity();

            if (arrowVel.lengthSquared() < 0.0001) continue;

            Vec3d dirToPlayer = playerPos.subtract(arrowPos).normalize();

            if (arrowVel.normalize().dotProduct(dirToPlayer) <= 0) {
                continue;
            }

            Vec3d nextArrowPos = arrowPos.add(arrowVel);

            Optional<Vec3d> collide = expandedPlayerBox.raycast(arrowPos, nextArrowPos);

            if (collide.isPresent()) {
                return true;
            }
        }
        for (EggEntity arrow : world.getEntitiesByClass(
                EggEntity.class,
                player.getBoundingBox().expand(maxCheckDistance),
                a -> !a.onGround
        )) {
            Vec3d arrowPos = arrow.getPos();
            Vec3d arrowVel = arrow.getVelocity();

            if (arrowVel.lengthSquared() < 0.0001) continue;

            Vec3d dirToPlayer = playerPos.subtract(arrowPos).normalize();

            if (arrowVel.normalize().dotProduct(dirToPlayer) <= 0) {
                continue;
            }

            Vec3d nextArrowPos = arrowPos.add(arrowVel);

            Optional<Vec3d> collide = expandedPlayerBox.raycast(arrowPos, nextArrowPos);

            if (collide.isPresent()) {
                return true;
            }
        }

        return false;
    }

}
