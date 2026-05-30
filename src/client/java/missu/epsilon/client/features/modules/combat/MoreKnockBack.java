package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.event.events.game.AttackEvent;
import missu.epsilon.client.event.events.game.PostUpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.utils.client.ClientData;
import net.minecraft.entity.LivingEntity;

import static missu.epsilon.client.utils.Wrapper.mc;

/**
 * Create by Daniel on 2026/1/31
 */
@ModuleInfo(name = "MoreKnockBack",category = ModuleCategory.COMBAT,description = "Give your attack a sprint knockback bonus")
public class MoreKnockBack extends Module {
   public static ListValue mode = new ListValue("Mode", new String[]{"SprintReset"}, "SprintReset");
   public static ListValue triggerMode = new ListValue("TriggerMode",new String[]{"SmartPredict","Always"},"SmartPredict");
   public static BoolValue noVelocityWorking = new BoolValue("NoAntiKBWorking",true);

   public static LivingEntity target;
   public static boolean handle;

   @EventTarget
   public void onAttack(AttackEvent event) {
      handle = true;
      target = (LivingEntity) event.getAttackedEntity();
   }

   @EventTarget
   public void onPostUpdate(PostUpdateEvent event) {
      if (mc.player == null) return;
      if (ClientData.realSprint && handle) {
         if (mode.is("SprintReset") && (!noVelocityWorking.get() || !AntiKnockback.receiveVelocity && !AntiKnockback.buffer)) {
            if (triggerMode.is("SmartPredict")) {
               if (target != null && (target.hurtTime == 0 || target.hurtTime == 1)) {
                  mc.player.setSprinting(false);
                  handle = false;
               }
            } else {
               mc.player.setSprinting(false);
               handle = false;
            }
         }
      }
   }
}
