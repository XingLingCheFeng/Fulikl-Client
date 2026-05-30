package missu.epsilon.client.utils.render.projectiles;

import net.minecraft.entity.Entity;

public interface ProjectileData {
   int color();

   default float collisionInflation() { return 0.25F; }

   default float renderRadius() { return 0.125F; }

   default float gravity() { return 0.03F; }

   default float drag() { return 0.99F; }

   default float getData1() {
      return 0.125F;
   }
   default float getData2() {
      return 0.25F;
   }

   default float getGravity() {
      return 0.03F;
   }
   boolean matches(Entity entity);
}
