package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "NoJumpDelay",category = ModuleCategory.MOVEMENT,description = "Remove jump delay on your client", hide = true)
public class NoJumpDelay extends Module {
    @SuppressWarnings("unused")
    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player != null) {
            mc.player.jumpingCooldown = 0;
        }
    }
}
