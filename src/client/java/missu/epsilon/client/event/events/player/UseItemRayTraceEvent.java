package missu.epsilon.client.event.events.player;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.event.impl.Event;

@Setter
@Getter
public class UseItemRayTraceEvent implements Event {

   private float yaw;
   private float pitch;

   public UseItemRayTraceEvent(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof UseItemRayTraceEvent other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else {
         return Float.compare(this.getYaw(), other.getYaw()) == 0 && Float.compare(this.getPitch(), other.getPitch()) == 0;
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof UseItemRayTraceEvent;
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = result * 59 + Float.floatToIntBits(this.getYaw());
      return result * 59 + Float.floatToIntBits(this.getPitch());
   }

   @Override
   public String toString() {
      return "UseItemRayTraceEvent(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
   }

}
