package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.NumberValue;

@ModuleInfo(name = "AutoRod",category = ModuleCategory.COMBAT,description = "Automatically rod enemies using accurate projectile simulation")
public class AutoRod extends Module {
    public static NumberValue minRange = new NumberValue("MinRange", 3.0, 1.0, 10.0);
    public static NumberValue maxRange = new NumberValue("MaxRange", 6.0, 1.0, 10.0);

}
