package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.event.events.player.StrafeEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.utils.client.ClientUtils;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "Sprint",description = "Automatically sprinting when walking forward",category = ModuleCategory.MOVEMENT, hide = true)
public class Sprint extends Module {
    @Override
    public void onEnable() {
        if (ClientUtils.isNull()) {
            return;
        }

        mc.options.sprintKey.setPressed(false);
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }

        mc.options.sprintKey.setPressed(true);
    }
}
