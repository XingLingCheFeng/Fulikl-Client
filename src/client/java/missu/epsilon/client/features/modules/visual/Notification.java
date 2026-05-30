package missu.epsilon.client.features.modules.visual;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;

@ModuleInfo(name = "Notification",category = ModuleCategory.VISUAL,description = "Show module enabled and disabled information", defaultOn = true)
public class Notification extends Module {
    public static BoolValue showMode = new BoolValue("Show Module", true);

}
