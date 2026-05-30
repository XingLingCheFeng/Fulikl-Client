package missu.epsilon.client.features.modules.visual;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.NumberValue;

@ModuleInfo(name = "PostProcessing",category = ModuleCategory.VISUAL, defaultOn = true)
public class PostProcessing extends Module {
    public static final NumberValue blurStrength = new NumberValue("Blur Strength", 10, 1, 100,1);
}
