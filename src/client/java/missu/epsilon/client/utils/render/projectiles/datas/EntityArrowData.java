package missu.epsilon.client.utils.render.projectiles.datas;

import net.minecraft.entity.projectile.ArrowEntity;

import java.util.Collections;
import java.util.HashSet;

public class EntityArrowData extends BasicProjectileData {
   public EntityArrowData() {
      super(new HashSet<>(Collections.singletonList(ArrowEntity.class)), 0xFFFF0000, 0.50F, 0.25F, 0.05F, 0.99F);
   }
}
