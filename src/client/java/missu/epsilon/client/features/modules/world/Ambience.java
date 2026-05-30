package missu.epsilon.client.features.modules.world;

import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "Ambience", category = ModuleCategory.WORLD, hide = true)
public class Ambience extends Module {
    private final ListValue mode = new ListValue("Time Mode", new String[]{"Static", "Cycle"}, "Static");

    private final NumberValue time = (NumberValue) new NumberValue("Static Time", 24000, 0, 24000, 100).displayable(() -> mode.is("Static"));
    private final NumberValue cycleSpeed = (NumberValue) new NumberValue("Cycle Speed", 24, 1, 24, 1).displayable(() -> mode.is("Cycle"));
    private final BoolValue reverseCycle = (BoolValue) new BoolValue("Reverse Cycle", false).displayable(() -> mode.is("Cycle"));
    private long timeCycle = 0;

    @Override
    public void onEnable() {
        timeCycle = 0;
    }

    @EventTarget
    public void onTickEvent(UpdateEvent event) {
        if (mc.player != null && mc.world != null) {
            if (mode.is("Static")) {
                mc.world.getLevelProperties().setTimeOfDay(time.getValue().longValue());
            } else {
                mc.world.getLevelProperties().setTimeOfDay(timeCycle);
                timeCycle += (reverseCycle.getValue() ? -cycleSpeed.getValue().longValue() : cycleSpeed.getValue().longValue()) * 10;

                if (timeCycle > 24000) {
                    timeCycle = 0;
                } else if (timeCycle < 0) {
                    timeCycle = 24000;
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket packet) {
            if (mode.is("Static")) {
                event.setPacket(new WorldTimeUpdateS2CPacket(packet.time(), time.getValue().longValue(), false));
            } else {
                event.setPacket(new WorldTimeUpdateS2CPacket(packet.time(), timeCycle, false));
            }
        }
    }
}
