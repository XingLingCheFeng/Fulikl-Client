package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;

@ModuleInfo(name = "AntiBlind",description = "Disable the effects of blocking normal vision",category = ModuleCategory.RENDER)
public class AntiBlind extends Module {
    public static BoolValue fireAspect = new BoolValue("FireAspect", true);
    public static NumberValue opacity = new NumberValue("Fire Opacity \"%\"",20,0,100,1);
    public static BoolValue darkness = new BoolValue("Darkness",true);
    public static BoolValue pumpkin = new BoolValue("Pumpkin",true);
    public static BoolValue blind = new BoolValue("Blinding",true);
    public static BoolValue nausea = new BoolValue("Nausea",true);
}
