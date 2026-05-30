package missu.epsilon.client.features.modules.world;

import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.client.ClientData;

@ModuleInfo(name = "GameSpeed",description = "Change the client world's speed", category = ModuleCategory.WORLD)
public class GameSpeed extends Module {
    public static NumberValue timerSpeed = new NumberValue("TimerSpeed",1,0,10,0.1);

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        ClientData.timerSpeed = timerSpeed.get().floatValue();
    }

    @Override
    public void onDisable() {
        ClientData.timerSpeed = 1.0F;
    }
}
