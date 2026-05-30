package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.event.events.player.MoveInputEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;

@ModuleInfo(name = "AntiAFK",category = ModuleCategory.PLAYER, description = "Prevents you from getting kicked for being AFK")
public class AntiAFK extends Module {
    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        event.jump = true;
    }
}
