package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.ingameui.notification.NotificationManager;
import missu.epsilon.client.ingameui.notification.NotificationType;

@ModuleInfo(name = "Target", category = ModuleCategory.PLAYER, defaultOn = true, hide = true)
public class Target extends Module {
    public static BoolValue player = new BoolValue("Player", true);
    public static BoolValue animal = new BoolValue("Animal", false);
    public static BoolValue invisible = new BoolValue("Invisible", false);
    public static BoolValue mob = new BoolValue("Mob", false);

    @Override
    public void onDisable() {
        NotificationManager.post(NotificationType.DANGER, "You can't disable this module");
        setEnabled(true);
    }
}
