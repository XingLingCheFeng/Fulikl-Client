package missu.epsilon.client.utils.render.projectiles.datas;

import net.minecraft.entity.projectile.thrown.PotionEntity;

import java.util.Collections;
import java.util.HashSet;

public class EntityPotionData extends BasicProjectileData {
   public EntityPotionData() {
      super(new HashSet<>(Collections.singleton(PotionEntity.class)), 0xFFFF42F9, 0.25F, 0.20F, 0.05F, 0.95F);
   }
}
