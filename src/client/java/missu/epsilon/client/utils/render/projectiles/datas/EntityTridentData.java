package missu.epsilon.client.utils.render.projectiles.datas;

import net.minecraft.entity.projectile.TridentEntity;

import java.util.Collections;
import java.util.HashSet;

public class EntityTridentData extends BasicProjectileData {
    public EntityTridentData() {
        super(new HashSet<>(Collections.singletonList(TridentEntity.class)), 0xFF00D8FF, 0.35F, 0.25F, 0.05F, 0.99F);
    }
}
