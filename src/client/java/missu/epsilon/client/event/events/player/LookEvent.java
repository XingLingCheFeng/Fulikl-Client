package missu.epsilon.client.event.events.player;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.Event;
import net.minecraft.entity.Entity;

@Setter
@Getter
public class LookEvent implements Event {

   public Entity entity;
   public float yaw;
   public float pitch;

    public LookEvent(Entity entity, float yaw, float pitch) {
      this.entity = entity;
      this.yaw = yaw;
      this.pitch = pitch;
   }

}
