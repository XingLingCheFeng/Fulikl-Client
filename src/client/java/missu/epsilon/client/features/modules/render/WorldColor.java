package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.ColorValue;

import java.awt.*;

@ModuleInfo(name = "WorldColor",description = "Oh god look,my client can change world colors!",category = ModuleCategory.VISUAL)
public class WorldColor extends Module {
    public static ColorValue color = new ColorValue("Color", Color.WHITE.getRGB());
}
