package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.world.Scaffold;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.utils.entity.RaycastUtils;
import missu.epsilon.client.utils.entity.Rotation;
import missu.epsilon.client.utils.entity.RotationUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static missu.epsilon.client.utils.Wrapper.mc;

/**
 * Author Daniel
 */

@ModuleInfo(name = "AntiFireball",description = "Automatically hit the fireball came to you",category = ModuleCategory.COMBAT)
public class AntiFireball extends Module {
    public static BoolValue noKeepSprint = new BoolValue("NoKeepSprint (Bypass Grim false detection)",false);
    public static BoolValue visualSwing = new BoolValue("VisualSwing",true);
    public static NumberValue rotationSpeed = new NumberValue("RotationSpeed",180,0,180,1);
    public static NumberValue scanRange = new NumberValue("ScanRange", 8, 1, 20, 0.1);
    public static NumberValue attackRange = new NumberValue("AttackRange", 4, 1, 20, 0.1);

    private final List<FireballEntity> trackedFireballs = new CopyOnWriteArrayList<>();

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null || Client.moduleManager.getModule(Scaffold.class).isEnabled()) return;
        ClientPlayerEntity player = mc.player;

        /// 判断是否朝玩家飞来
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof FireballEntity fireball)) continue;

            Vec3d motion = fireball.getPos().subtract(fireball.prevX, fireball.prevY, fireball.prevZ);
            Vec3d toPlayer = player.getPos().subtract(fireball.getPos());

            if (motion.dotProduct(toPlayer) > 0 && fireball.squaredDistanceTo(player) <= scanRange.get() * scanRange.get()) {
                if (!trackedFireballs.contains(fireball)) trackedFireballs.add(fireball);
            }
        }

        /// 更新追踪列表
        trackedFireballs.removeIf(fireball -> {
            Vec3d motion = fireball.getPos().subtract(fireball.prevX, fireball.prevY, fireball.prevZ);
            Vec3d toPlayer = player.getPos().subtract(fireball.getPos());
            return motion.dotProduct(toPlayer) <= 0 || fireball.squaredDistanceTo(player) > scanRange.get() * scanRange.get();
        });

        if (trackedFireballs.isEmpty()) return;

        /// 选择最近的火球
        FireballEntity closest = trackedFireballs.stream()
                .min(Comparator.comparingDouble(f -> f.squaredDistanceTo(player)))
                .orElse(null);

        /// 转头
        Rotation targetRot = calculateRotationToEntity(player, closest);
        Client.rotationManager.setRotations(targetRot,1, MovementFix.SILENT);

        if (RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), attackRange.get(), false) != null && Objects.requireNonNull(RaycastUtils.rayCastEntityHit(RotationUtils.getRotationOrElseMC(), attackRange.get(), false)).getEntity() == closest) {
            Objects.requireNonNull(mc.interactionManager).syncSelectedSlot();
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(closest, mc.player.isSneaking()));
            /// Grim傻逼误检测给你妈杀了
            if (noKeepSprint.get() && mc.player.isSprinting()) {
                mc.player.setVelocity(mc.player.getVelocity().multiply(0.6, 1.0, 0.6));
                mc.player.setSprinting(false);
            }
            if (visualSwing.get()) {
                mc.player.swingHand(Hand.MAIN_HAND);
            } else {
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }
    }

    /// 计算 Rotation
    private Rotation calculateRotationToEntity(ClientPlayerEntity player, FireballEntity target) {
        Vec3d playerEye = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        Vec3d targetPos = target.getPos().add(0, target.getHeight() / 2.0, 0); // 瞄准火球中心

        Vec3d diff = targetPos.subtract(playerEye);
        double diffX = diff.x;
        double diffY = diff.y;
        double diffZ = diff.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        yaw = MathHelper.wrapDegrees(yaw);
        pitch = MathHelper.wrapDegrees(pitch);

        return new Rotation(yaw, pitch);
    }
}
