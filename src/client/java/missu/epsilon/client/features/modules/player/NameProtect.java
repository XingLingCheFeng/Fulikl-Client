package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.TextValue;

@ModuleInfo(name = "NameProtect", category = ModuleCategory.PLAYER, hide = true)
public class NameProtect extends Module {
    public static String nick = Client.CLIENT_NAME;

    public TextValue textValue = new TextValue("Nick Name", "孙笑川");

    @EventTarget
    public void onTick(TickEvent event) {
        nick = textValue.get();
    }
}
