package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.world.Scaffold;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.mixin.client.MinecraftClientAccessor;
import net.minecraft.item.BedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "FastPlace",category = ModuleCategory.PLAYER, hide = true)
public class FastPlace extends Module {
    private final NumberValue delay = new NumberValue("Delay", 0D, 0, 3, 1);
    private final BoolValue noBed = new BoolValue("No Bed", true);
    private final BoolValue disableOnCobweb = new BoolValue("Disable On Web", true);
    private int originalRightClickDelay;

    @Override
    public void onEnable() {
        originalRightClickDelay = ((MinecraftClientAccessor) mc).getItemUseCooldown();
    }

    @Override
    public void onDisable() {
        ((MinecraftClientAccessor) mc).setItemUseCooldown(originalRightClickDelay);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player != null && mc.world != null) {
            if ((mc.player.getMainHandStack().getItem() == Items.COBWEB || mc.player.getOffHandStack().getItem() == Items.COBWEB) && disableOnCobweb.get()) {
                return;
            }

            if (Client.moduleManager.getModule(Scaffold.class).isEnabled()) {
                return;
            }

            if ((mc.player.getMainHandStack().getItem() instanceof BlockItem && (!noBed.get() || !(mc.player.getMainHandStack().getItem() instanceof BedItem)) || mc.player.getOffHandStack().getItem() instanceof BlockItem && (!noBed.get() || !(mc.player.getMainHandStack().getItem() instanceof BedItem)) ) && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                if (((MinecraftClientAccessor) mc).getItemUseCooldown() != 0) {
                    ((MinecraftClientAccessor) mc).setItemUseCooldown(delay.get().intValue());
                }
            }
        }
    }
}
