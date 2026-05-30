package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.ListValue;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "FullBright",category = ModuleCategory.RENDER)
public class FullBright extends Module {
    public static final ListValue mode = new ListValue("Mode", new String[]{"Potion", "Gamma"}, "Potion");


    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (mode.is("Potion")) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null) return;
        if (mode.is("Potion")) {
            mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1337, 0));
        }
    }
}

