package missu.epsilon.client.utils.client;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.combat.ThrowableAura;
import missu.epsilon.client.features.modules.player.AntiBot;
import missu.epsilon.client.features.modules.player.Teams;
import missu.epsilon.client.utils.entity.PlayerUtils;
import missu.epsilon.client.utils.entity.Rotation;
import missu.epsilon.client.utils.entity.RotationUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import static missu.epsilon.client.features.modules.player.Target.*;
import static missu.epsilon.client.utils.Wrapper.mc;

public class TargetManager {

    public Boolean isTarget(Entity entity, boolean onlyPlayer) {
        if (entity instanceof LivingEntity && (entity.isAlive()) && entity != mc.player) {
            if (!invisible.get() && entity.isInvisible()) return false;

            if (onlyPlayer && !(entity instanceof PlayerEntity)) return false;

            if (player.get() && entity instanceof PlayerEntity playerEntity) {


                return !(Client.moduleManager.getModule(Teams.class).isEnabled() && Teams.isInYourTeam(playerEntity)) && !(AntiBot.isBot(playerEntity) && Client.moduleManager.getModule(AntiBot.class).isEnabled());
            }
            return mob.get() && entity instanceof MobEntity || animal.get() && entity instanceof AnimalEntity;
        }
        return false;
    }

    public ObjectArrayList<LivingEntity> getTargets(double min, double max) {
        var targets = new ObjectArrayList<LivingEntity>();

        if (ClientUtils.isNull()) {
            return targets;
        }

        for (var livingEntity : PlayerUtils.getAttackableTargets(max)) {
            if (mc.player != null && mc.player.squaredDistanceTo((Entity) livingEntity) >= min * min &&
                    MathHelper.wrapDegrees(Math.abs(RotationUtils.getRotationDifference(
                            new Rotation(mc.player.getPitch(), mc.player.getYaw()),
                            RotationUtils.toRotation(mc.player.getEyePos(), RotationUtils.getCenter(livingEntity.getBoundingBox()))
                    ))) <= Client.moduleManager.getModule(ThrowableAura.class).fov.get()) {

                if (!(livingEntity instanceof PlayerEntity playerEntity) ||
                        (!Client.moduleManager.getModule(Teams.class).isEnabled() ||
                                !Teams.isInYourTeam(playerEntity)) &&
                                !AntiBot.isBot(playerEntity)) {
                    targets.add(livingEntity);
                }
            }
        }


        return targets;
    }
}
