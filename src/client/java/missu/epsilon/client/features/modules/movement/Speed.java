package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.movement.MovementUtils;
import net.minecraft.client.util.InputUtil;

/**
 * Author Daniel
 */

@ModuleInfo(name = "Speed",description = "Increase your speed when you are moving",category = ModuleCategory.MOVEMENT)
public class Speed extends Module {

    public static ListValue antiCheat = new ListValue("AntiCheat",new String[]{"AutoJump"},"AutoJump");

    @Override
    public void onDisable() {
        if (antiCheat.is("AutoJump"))
            mc.options.jumpKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode()));
        ClientData.timerSpeed = 1F;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        switch (antiCheat.get()) {
            case "AutoJump":
                if (MovementUtils.isMove()) {
                    mc.options.jumpKey.setPressed(true);
                } else {
                    mc.options.jumpKey.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode()));
                }
                break;
        }
    }
}
