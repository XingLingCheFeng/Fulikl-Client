package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.NumberValue;

@ModuleInfo(name = "AspectRatio",description = "Stretch your screen.",category = ModuleCategory.RENDER)
public class AspectRatio extends Module {
    public static NumberValue ratio = new NumberValue("Ratio",1.78,0.1,5);

}
