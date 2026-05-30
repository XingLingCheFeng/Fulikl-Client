package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import net.minecraft.client.gui.screen.DeathScreen;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "AutoRespawn",category = ModuleCategory.PLAYER,description = "Automatically respawn when you are dead", hide = true)
public class AutoRespawn extends Module {
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null) return;
        if (mc.player.getHealth() == 0 || mc.player.isDead() || mc.currentScreen instanceof DeathScreen) {
            mc.player.requestRespawn();
            mc.setScreen(null);
        }
    }
}
