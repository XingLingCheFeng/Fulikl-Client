package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;

@ModuleInfo(name = "FastItems",description = "2D Items",category = ModuleCategory.RENDER)
public class FastItems extends Module {
    public MultiBoolValue addons = new MultiBoolValue("Addons", new BoolValue[]{
            new BoolValue("Cast Shadows", true),
            new BoolValue("Render Side of Items", false),
            new BoolValue("Affect 3D Models", true)
    });
}
