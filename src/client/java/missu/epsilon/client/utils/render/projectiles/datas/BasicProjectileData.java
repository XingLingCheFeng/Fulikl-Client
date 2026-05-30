package missu.epsilon.client.utils.render.projectiles.datas;

import missu.epsilon.client.utils.render.projectiles.ProjectileData;
import net.minecraft.entity.Entity;

import java.util.Set;

public class BasicProjectileData implements ProjectileData {
   private final int color;
   private final Set<Class<?>> entityClasses;
   private final float collisionInflation;
   private final float renderRadius;
   private final float gravity;
   private final float drag;

   public BasicProjectileData(Set<Class<?>> entityClasses, int color) {
      this(entityClasses, color, 0.25F, 0.125F, 0.03F, 0.99F);
   }

   public BasicProjectileData(Set<Class<?>> entityClasses, int color, float collisionInflation, float renderRadius, float gravity, float drag) {
      this.entityClasses = entityClasses;
      this.color = color;
      this.collisionInflation = collisionInflation;
      this.renderRadius = renderRadius;
      this.gravity = gravity;
      this.drag = drag;
   }

   @Override
   public int color() {
      return color;
   }

   @Override
   public float collisionInflation() {
      return collisionInflation;
   }

   @Override
   public float renderRadius() {
      return renderRadius;
   }

   @Override
   public float gravity() {
      return gravity;
   }

   @Override
   public float drag() {
      return drag;
   }

   @Override
   public boolean matches(Entity entity) {
      for (Class<?> clazz : entityClasses) {
         if (clazz.isInstance(entity)) {
            return true;
         }
      }

      return false;
   }
}
