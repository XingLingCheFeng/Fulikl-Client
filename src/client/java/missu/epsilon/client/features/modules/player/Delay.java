package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.utils.entity.BlinkUtils;

@ModuleInfo(name = "Delay",category = ModuleCategory.PLAYER,description = "Suspends all server packets.")
public class Delay extends Module {
    @Override
    public void onEnable() {
        BlinkUtils.startDelay();
    }
    @Override
    public void onDisable() {
        BlinkUtils.stopDelay();
    }
}
