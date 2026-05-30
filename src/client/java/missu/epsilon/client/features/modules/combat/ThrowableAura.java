package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.Priorities;
import missu.epsilon.client.event.events.player.TickMovementEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.world.Scaffold;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.entity.RaycastUtils;
import missu.epsilon.client.utils.entity.RotationUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

import static missu.epsilon.client.utils.Wrapper.mc;

/**
 * @author Jon_awa
 * @since 2025/9/22
 */
@ModuleInfo(name = "ThrowableAura",category = ModuleCategory.COMBAT)
public class ThrowableAura extends Module {
    private final ListValue priorityMode = new ListValue("Priority Mode", new String[]{"Distance", "Health", "Fov"}, "Distance");
    public final NumberValue fov = new NumberValue("Fov", 90, 1, 180,1);
    private final NumberValue minRange = new NumberValue("Min Range", 3.2f, 0, 10,0.1);
    private final NumberValue maxRange = new NumberValue("Max Range", 8, 2, 16,1);
    private final NumberValue delay = new NumberValue("Delay", 500, 0, 1000,50);

    private final TimerUtils timer = new TimerUtils();
    private boolean shouldThrow;
    private int lastSlot;
    
    @Override
    public void onEnable() {
        this.timer.reset();
        this.reset();
    }

    @EventTarget(Priorities.LOW)
    public void onTickMovement(TickMovementEvent event) {
        if (ClientUtils.isNull() || mc.interactionManager == null) {
            return;
        }

        if (this.shouldThrow && this.canWork()) {
            if (mc.player.getOffHandStack().isOf(Items.SNOWBALL) || mc.player.getOffHandStack().isOf(Items.EGG)) {
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);

                if (this.lastSlot != -1) {
                    mc.player.getInventory().selectedSlot = this.lastSlot;
                }

                this.reset();
                return;
            }

            if (mc.player.getMainHandStack().isOf(Items.SNOWBALL) || mc.player.getMainHandStack().isOf(Items.EGG)) {

                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

                if (this.lastSlot != -1) {
                    mc.player.getInventory().selectedSlot = this.lastSlot;
                }

                this.reset();
                return;
            }
        } else {
            if (this.lastSlot != -1) {
                mc.player.getInventory().selectedSlot = this.lastSlot;
                this.lastSlot = -1;
            }

            this.shouldThrow = false;
        }

        if (this.timer.hasTimeElapsed(this.delay.get().longValue()) && this.canWork()) {
            var targets = Client.targetManager.getTargets(this.minRange.get(), this.maxRange.get());

            if (targets.isEmpty()) {
                return;
            }

            var throwSlot = this.getThrowSlot();

            if (mc.player.getMainHandStack().isOf(Items.SNOWBALL)
                    || mc.player.getMainHandStack().isOf(Items.EGG)
                    || mc.player.getOffHandStack().isOf(Items.SNOWBALL)
                    || mc.player.getOffHandStack().isOf(Items.EGG)
                    || throwSlot != -1
            ) {
                if (targets.size() > 1) {
                    switch (this.priorityMode.get()) {
                        case "Distance" -> targets.sort(Comparator.comparingDouble(entity -> entity.distanceTo(mc.player)));
                        case "Health" -> targets.sort(Comparator.comparingDouble(LivingEntity::getHealth));
                        case "Fov" -> targets.sort(Comparator.comparingDouble(entity -> MathHelper.wrapDegrees(Math.abs(RotationUtils.getRotationDifference(RotationManager.serverRotation, RotationUtils.toRotation(mc.player.getEyePos(), RotationUtils.getCenter(entity.getBoundingBox())))))));
                    }
                }

                var target = targets.getFirst();
                var aimVec = this.getAimVec(target);
                var rotation = RotationUtils.toRotation(mc.player.getEyePos(), aimVec);
                var hit = RaycastUtils.rayCast(rotation, mc.player, mc.world, mc.player.distanceTo(target), false);

                if (hit == null || hit.getType() == HitResult.Type.BLOCK) {
                    return;
                }

                Client.rotationManager.setRotations(rotation, 2, MovementFix.SILENT);

                if (!mc.player.getMainHandStack().isOf(Items.SNOWBALL)
                        && !mc.player.getMainHandStack().isOf(Items.EGG)
                        && !mc.player.getOffHandStack().isOf(Items.SNOWBALL)
                        && !mc.player.getOffHandStack().isOf(Items.EGG)
                ) {
                    this.lastSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = throwSlot;
                }

                this.shouldThrow = true;
                this.timer.reset();
            }
        }
    }

    private void reset() {
        this.shouldThrow = false;
        this.lastSlot = -1;
    }

    private int getThrowSlot() {
        if (mc.player == null) {
            return -1;
        }

        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);

            if (stack.isOf(Items.SNOWBALL) || stack.isOf(Items.EGG)) {
                return i;
            }
        }

        return -1;
    }

    private boolean canWork() {
        assert mc.player != null;
        var scaffold = Client.moduleManager.getModule(Scaffold.class);
        var killAura = Client.moduleManager.getModule(KillAura.class);

        if (mc.player.isUsingItem()) {
            return false;
        }

        if (mc.currentScreen != null) {
            return false;
        }

        if (mc.player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            return false;
        }

        if (scaffold.isEnabled() && (scaffold.isCanPlace() || !mc.player.isOnGround())) {
            return false;
        }

        if (killAura.isEnabled() && ((KillAura.currentTarget != null && (mc.player.getEyePos().distanceTo(RotationUtils.getEntityNearestVec(mc.player, KillAura.currentTarget)) > KillAura.range.get())) || (KillAura.realBlock && !KillAura.autoBlock.is("Off") && !KillAura.autoBlock.is("Fake")))) {
            return false;
        }

        if (!mc.options.useKey.isPressed() && mc.options.attackKey.isPressed() && mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            return false;
        }

        return true;
    }

    /// By ChatGPT 5
    private Vec3d getAimVec(LivingEntity livingEntity) {
        if (mc.player == null) return livingEntity.getEyePos();

        final Vec3d shooterPosNow = mc.player.getEyePos();
        final Vec3d shooterVelNow = mc.player.getVelocity();

        // 目标中心与当前速度
        final Vec3d targetPosNow  = livingEntity.getBoundingBox().getCenter();
        final Vec3d targetVelNow  = livingEntity.getVelocity();

        // 目标 AABB 半尺寸（包含 ~0.15 的投掷物半径冗余）
        final var bb = livingEntity.getBoundingBox();
        final double hx = bb.getLengthX() * 0.5 + 0.15;
        final double hy = bb.getLengthY() * 0.5 + 0.15;
        final double hz = bb.getLengthZ() * 0.5 + 0.15;

        // 抛物线参数（雪球/鸡蛋）
        final double s = 1.5;   // 初速
        final double g = 0.03;  // 重力
        final double k = 0.99;  // 阻力

        // 预计“开火延迟 tick”（仅用于 XZ 外推；Y 保持不变）
        final boolean hasInHand =
                mc.player.getMainHandStack().isOf(Items.SNOWBALL) || mc.player.getMainHandStack().isOf(Items.EGG) ||
                        mc.player.getOffHandStack().isOf(Items.SNOWBALL)  || mc.player.getOffHandStack().isOf(Items.EGG);
        final int fireDelay = hasInHand ? 2 : 3;

        // —— 只在 XZ 上外推到开火时刻；Y 维持当前高度 —— //
        final Vec3d shooterPosFire = new Vec3d(
                shooterPosNow.x + shooterVelNow.x * fireDelay,
                shooterPosNow.y,
                shooterPosNow.z + shooterVelNow.z * fireDelay
        );
        final Vec3d targetPosFire = new Vec3d(
                targetPosNow.x + targetVelNow.x * fireDelay,
                targetPosNow.y, // 不用 y 速度
                targetPosNow.z + targetVelNow.z * fireDelay
        );

        // 相对位移（以开火时刻计）
        final Vec3d r = targetPosFire.subtract(shooterPosFire);

        // 只用水平速度做目标未来位移（Y 不随时间变）
        final Vec3d targetVelXZ = new Vec3d(targetVelNow.x, 0.0, targetVelNow.z);

        Vec3d bestDir   = null;
        double bestScore = Double.POSITIVE_INFINITY;

        final int nMax = 80; // 搜索最大飞行 tick
        for (int n = 2; n <= nMax; n++) {
            // 水平目标位移（线性 XZ）
            final Vec3d Rh = new Vec3d(r.x + targetVelXZ.x * n, 0.0, r.z + targetVelXZ.z * n);
            final double RhLen = Math.sqrt(Rh.x * Rh.x + Rh.z * Rh.z);
            if (RhLen < 1e-6) continue;

            final double dirHx = Rh.x / RhLen;
            final double dirHz = Rh.z / RhLen;

            final double Sn = geomSum(k, n);
            final double Tn = triGeomSum(k, n);

            // —— 初速 = s * dir（不叠加玩家速度）——
            // 水平：(s * cosθ) * S(n) = |R_h|
            final double cosTheta = (RhLen / Sn) / s;

            // 垂直：(s * sinθ) * S(n) - g * T(n) = R_y(n)，而 R_y(n) = r.y（目标 y 不变）
            final double sinTheta = (r.y + g * Tn) / (s * Sn);

            if (!Double.isFinite(cosTheta) || !Double.isFinite(sinTheta)) continue;
            if (cosTheta < -1 || cosTheta > 1 || sinTheta < -1 || sinTheta > 1) continue;
            final double norm2 = cosTheta * cosTheta + sinTheta * sinTheta;
            if (norm2 > 1.0005) continue;

            // 单位方向
            Vec3d dir = new Vec3d(dirHx * cosTheta, sinTheta, dirHz * cosTheta);
            double l = dir.length();
            if (l < 1e-6) continue;
            dir = dir.multiply(1.0 / l);

            // 评估误差：抛射体不带玩家速度；目标 Y 恒定，仅 XZ 运动
            double err = simulatePathAABBError(
                    shooterPosFire,
                    Vec3d.ZERO,           // 初速不叠加玩家速度
                    dir,
                    targetPosFire,
                    targetVelXZ,          // 只传 XZ 速度，Y=0
                    n, s, g, k,
                    hx, hy, hz,
                    /*dampedY*/ false     // 不做任何 Y 预测
            );

            // 轻正则，避免极端高弧
            double score = err + n * 0.002;

            if (score < bestScore) {
                bestScore = score;
                bestDir = dir;
            }
        }

        if (bestDir != null) {
            return shooterPosNow.add(bestDir); // 只要方向，交给 RotationUtils 生成 pitch/yaw
        } else {
            // 兜底：用无阻力时间估计 + 轻抬高
            double d = shooterPosNow.distanceTo(targetPosNow);
            double t = Math.max(d / Math.max(s, 1e-3), 0.05);
            Vec3d lead = targetPosNow.add(targetVelXZ.multiply(t)); // 只用 XZ 提前量
            return lead.add(0, 0.5 * g * t * t, 0);
        }
    }

    /**
     * 离散模拟整条弹道与“移动AABB”的最小距离。
     * - 每 tick 更新投掷物：v←v*k + (0,-g,0)，p←p+v
     * - 目标 A：y 线性（dampedY=false）；
     * B：y 衰减+重力（dampedY=true）：vy←vy*0.85 - 0.08
     * - 返回从 tick=1..n 的最小“点到AABB”的距离
     */
    private static double simulatePathAABBError(Vec3d shooterPos, Vec3d shooterVel, Vec3d dirUnit, Vec3d targetPos0, Vec3d targetVel0, int n, double speed, double gProj, double kProj, double hx, double hy, double hz, boolean dampedY) {
        // 初始化投掷物状态
        Vec3d p = shooterPos;
        Vec3d v = new Vec3d(
                dirUnit.x * speed + shooterVel.x,
                dirUnit.y * speed + shooterVel.y,
                dirUnit.z * speed + shooterVel.z
        );

        // 目标状态（以中心点计）
        Vec3d t = targetPos0;
        double tvx = targetVel0.x, tvy = targetVel0.y, tvz = targetVel0.z;

        double minDist = Double.POSITIVE_INFINITY;

        for (int i = 0; i < n; i++) {
            // projectile step
            p = p.add(v);
            v = v.multiply(kProj, kProj, kProj).add(0, -gProj, 0);

            // target step
            if (dampedY) {
                // 更贴近“跳起/受击后”的y演化
                tvy = tvy * 0.85 - 0.08; // 经验参数：衰减+重力
            }
            t = t.add(tvx, tvy, tvz);

            // 最近点到AABB距离（AABB以 t 为中心，半径 hx/hy/hz）
            double d = distancePointToAABB(p, t, hx, hy, hz);
            if (d < minDist) minDist = d;
        }

        return minDist;
    }

    /**
     * 点到轴对齐盒（以 center 为中心、半尺寸 hx/hy/hz）的距离
     */
    private static double distancePointToAABB(Vec3d point, Vec3d center, double hx, double hy, double hz) {
        double dx = Math.max(0.0, Math.abs(point.x - center.x) - hx);
        double dy = Math.max(0.0, Math.abs(point.y - center.y) - hy);
        double dz = Math.max(0.0, Math.abs(point.z - center.z) - hz);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * 几何级数和 S(n) = 1 + k + ... + k^{n-1} = (1 - k^n)/(1 - k)
     */
    private static double geomSum(double k, int n) {
        if (n <= 0) {
            return 0.0;
        }

        if (Math.abs(1.0 - k) < 1e-9) {
            return n;
        }

        return (1.0 - Math.pow(k, n)) / (1.0 - k);
    }

    /**
     * 双重和 T(n) = sum_{i=0}^{n-1} sum_{j=0}^{i-1} k^j
     * = ((n-1)/(1-k)) - (k*(1 - k^{n-1})/(1-k)^2)
     */
    private static double triGeomSum(double k, int n) {
        if (n <= 1) {
            return 0.0;
        }

        if (Math.abs(1.0 - k) < 1e-9) {
            return (n - 1) * (n) / 2.0;
        }

        var oneMinusK = (1.0 - k);

        return ((n - 1) / oneMinusK) - (k * (1.0 - Math.pow(k, n - 1)) / (oneMinusK * oneMinusK));
    }
}
