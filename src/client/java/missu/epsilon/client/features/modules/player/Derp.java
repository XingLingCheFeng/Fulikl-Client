package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.management.rotation.MovementFix;
import missu.epsilon.client.management.rotation.Priority;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.management.rotation.SmoothMode;
import missu.epsilon.client.utils.entity.Rotation;
import missu.epsilon.client.utils.entity.RotationUtils;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "Derp",description = "Make your body derp",category = ModuleCategory.PLAYER)
public class Derp extends Module {
    public static ListValue mode = new ListValue("Mode",new String[]{"Real","Render"},"Real");
    public static NumberValue rotSpeed = (NumberValue) new NumberValue("RotationSpeed",180F,0F,180F,1).displayable(() -> mode.get().equals("Real"));

    @EventTarget()
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null) return;
        if (mode.is("Real")) {
            Client.rotationManager.setRotations(new Rotation(RotationManager.serverRotation.getYaw() - 180F,mc.player.getPitch()), MovementFix.SILENT,true, SmoothMode.ADVANCED,rotSpeed.get().floatValue(),1,0, Priority.MEDIUM);
        }
    }
}
