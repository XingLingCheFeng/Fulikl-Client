package missu.epsilon.client.utils.render.projectiles;

import missu.epsilon.client.utils.render.projectiles.datas.BasicProjectileData;
import missu.epsilon.client.utils.render.projectiles.datas.EntityArrowData;
import missu.epsilon.client.utils.render.projectiles.datas.EntityPotionData;
import missu.epsilon.client.utils.render.projectiles.datas.EntityTridentData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.*;

import java.util.*;

public final class ProjectileDataManager {
    public static final EntityArrowData ARROW = new EntityArrowData();
    public static final EntityPotionData POTION = new EntityPotionData();
    public static final EntityTridentData TRIDENT = new EntityTridentData();

    public static final BasicProjectileData SNOWBALL = new BasicProjectileData(setOf(SnowballEntity.class), 0xFFFFFFFF);
    public static final BasicProjectileData PEARL = new BasicProjectileData(setOf(EnderPearlEntity.class), 0xFF64FFDA);
    public static final BasicProjectileData EGG = new BasicProjectileData(setOf(EggEntity.class), 0xFFE3E3E3);

    private static final List<ProjectileData> ALL = List.of(ARROW, POTION, TRIDENT, SNOWBALL, EGG, PEARL);

    @SafeVarargs
    private static <T> Set<Class<?>> setOf(Class<? extends T>... cls) {
        return new HashSet<>(Arrays.asList(cls));
    }

    public static Optional<ProjectileData> byEntity(Entity entity) {
        for (ProjectileData data : ALL) {
            if (data.matches(entity)) {
                return Optional.of(data);
            }
        }

        return Optional.empty();
    }

    public static Optional<ProjectileData> byItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        var item = stack.getItem();

        if (item instanceof BowItem || item instanceof ArrowItem) {
            return Optional.of(ARROW);
        } else if (item instanceof CrossbowItem) {
            return Optional.of(ARROW);
        } else if (item instanceof TridentItem) {
            return Optional.of(TRIDENT);
        } else if (item instanceof PotionItem) {
            return Optional.of(POTION);
        } else if (item instanceof SnowballItem) {
            return Optional.of(SNOWBALL);
        } else if (item instanceof EggItem) {
            return Optional.of(EGG);
        } else if (item instanceof EnderPearlItem) {
            return Optional.of(PEARL);
        } else {
            return Optional.empty();
        }
    }
}
