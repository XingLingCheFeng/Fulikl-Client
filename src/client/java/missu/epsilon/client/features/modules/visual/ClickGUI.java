package missu.epsilon.client.features.modules.visual;


import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;

@ModuleInfo(name = "ClickGUI",description = "ClickGUI (the gui now displaying on your screen)",category = ModuleCategory.VISUAL)
public class ClickGUI extends Module {
    @Override
    public void onEnable() {
        setEnabled(false);
    }

}
