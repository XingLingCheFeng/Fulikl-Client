package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "NoAttackDelay",category = ModuleCategory.PLAYER, hide = true,description = "Cancel attack cooldown")
public class NoAttackDelay extends Module {

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player != null && mc.world != null) {
            mc.attackCooldown = 0;
        }
    }
}