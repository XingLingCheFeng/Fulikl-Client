package missu.epsilon.mixin.item;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.exploit.CrazyMace;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.MaceItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MaceItem.class)
public class MixinMaceItem {
    @Inject(method = "shouldDealAdditionalDamage", at = @At("HEAD"), cancellable = true)
    private static void alwaysTrigger(LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        CrazyMace module = Client.moduleManager.getModule(CrazyMace.class);
        if (module != null && module.isEnabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getBonusAttackDamage", at = @At("HEAD"), cancellable = true)
    private void modifyBonusDamage(Entity target, float baseAttackDamage,
                                   DamageSource damageSource, CallbackInfoReturnable<Float> cir) {
        CrazyMace module = Client.moduleManager.getModule(CrazyMace.class);
        if (module == null || !module.isEnabled()) return;

        Entity source = damageSource.getSource();
        if (!(source instanceof LivingEntity livingEntity)) {
            cir.setReturnValue(0.0F);
            return;
        }

        float h = module.fakeFallDist.get().floatValue();
        float i;

        if (h <= 3.0F) {
            i = 4.0F * h;
        } else if (h <= 8.0F) {
            i = 12.0F + 2.0F * (h - 3.0F);
        } else {
            i = 22.0F + h - 8.0F;
        }

        World world = livingEntity.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            cir.setReturnValue(i + EnchantmentHelper.getSmashDamagePerFallenBlock(
                    serverWorld, livingEntity.getWeaponStack(), target, damageSource, 0.0F) * h);
        } else {
            cir.setReturnValue(i);
        }
    }

    @Inject(method = "getKnockback", at = @At("RETURN"), cancellable = true)
    private static void modifyKnockback(Entity attacker, LivingEntity attacked, Vec3d distance, CallbackInfoReturnable<Double> cir) {
        CrazyMace module = Client.moduleManager.getModule(CrazyMace.class);
        if (module != null && module.isEnabled()) {
            double originalKnockback = cir.getReturnValue();
            cir.setReturnValue(originalKnockback * module.maceKnockbackMultiplier.get());
        }
    }
}
