package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;

@ModuleInfo(name = "Camera",category = ModuleCategory.RENDER, description = "Modify your client camera")
public class Camera extends Module {
    public static final BoolValue modifyFov = new BoolValue("Modify Fov", true);
    public static final NumberValue fov = (NumberValue) new NumberValue("Fov", 1.00,0.00,1.50,0.01).displayable(modifyFov::get);
    public static final NumberValue cameraDistance = new NumberValue("3rd Camera Distance", 4, 0, 10, 0.1);
    public static final MultiBoolValue cameraOptions = new MultiBoolValue("Camera Options", new BoolValue[] {
            new BoolValue("No Fog", true),
            new BoolValue("No Camera Clip", false),
            new BoolValue("No Hurt Tilt", false),
            new BoolValue("Smooth", true),
            new BoolValue("Motion Camera", false)
    });

    public static final NumberValue cameraSpeed = (NumberValue) new NumberValue("Camera Speed", 0.1, 0.01, 0.5, 0.01).displayable(() -> cameraOptions.get("Motion Camera"));
}
