package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.event.events.player.TickMovementEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.miscs.RandomUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "MiddlePearl", category = ModuleCategory.PLAYER)
public class MidPearl extends Module {
    private final NumberValue mindelay = new NumberValue("Min Switch Delay", 0, 0, 1000, 50);
    private final NumberValue maxdelay = new NumberValue("Max Switch Delay", 5, 0, 1000, 50);
    private boolean doWork = false;
    private boolean switchBack = false;
    private int lastSlot = 0;
    private final TimerUtils timer = new TimerUtils();

    @Override
    public void onEnable() {
        this.lastSlot = -1;
        this.doWork = false;
        this.switchBack = false;
    }

    @EventTarget
    public void onTickMovement(TickMovementEvent event) {
        if (ClientUtils.isNull() || mc.interactionManager == null || mc.player.isDead()) {
            return;
        }

        if (mc.options.pickItemKey.isPressed()) {
            this.timer.reset();
            this.doWork = true;
            this.switchBack = false;
        }

        if (this.doWork && this.timer.hasTimeElapsed(RandomUtils.getRandom(mindelay.get(), maxdelay.get()))) {
            int pearlSlot = findPearlSlot();

            if (pearlSlot != -1) {
                this.lastSlot = mc.player.getInventory().selectedSlot;

                mc.player.getInventory().selectedSlot = pearlSlot;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

                this.timer.reset();
                this.doWork = false;
                this.switchBack = true;
            }
        }

        if (this.switchBack && this.timer.hasTimeElapsed(RandomUtils.getRandom(mindelay.get(), maxdelay.get()))) {
            mc.player.getInventory().selectedSlot = this.lastSlot;
            this.switchBack = false;
        }
    }

    private int findPearlSlot() {
        if (mc.player != null) {
            for (int i = 0; i <= 8; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);

                if (!stack.isEmpty() && stack.getItem() == Items.ENDER_PEARL) {
                    return i;
                }
            }
        }

        return -1;
    }
}
