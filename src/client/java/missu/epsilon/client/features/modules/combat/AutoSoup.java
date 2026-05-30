package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.miscs.TimerUtils;
import missu.epsilon.client.utils.packets.PacketUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "AutoSoup", category = ModuleCategory.COMBAT)
public class AutoSoup extends Module {
    private final TimerUtils timer = new TimerUtils();
    private final NumberValue delay = new NumberValue("Delay", 500.0, 300.0, 1000.0, 1.0);
    private final NumberValue health = new NumberValue("Health Percent", 0.5, 0.0, 1.0, 0.05);
    private boolean switchBack = false;
    private boolean useItem = false;
    private boolean throwItem = false;

    @EventTarget
    public void onMotion(MotionEvent e) {
        if (e.eventState == CancellableEvent.EventState.PRE) {
            if (this.useItem) {
                PacketUtils.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, e.getYaw(), e.getPitch()));
                this.useItem = false;
                return;
            }

            if (this.throwItem) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.DROP_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                this.throwItem = false;
                return;
            }

            if (this.switchBack) {
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
                this.switchBack = false;
                return;
            }

            if (!this.timer.hasTimeElapsed((double) this.delay.get())) {
                return;
            }

            if (mc.player.getHealth() / mc.player.getMaxHealth() < this.health.get()) {
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = (ItemStack) mc.player.getInventory().main.get(i);
                    if (stack.getItem() == Items.MUSHROOM_STEW) {
                        this.switchUseItem(i, true);
                        this.switchBack = true;
                        break;
                    }
                }
            }
        }
    }

    private void switchUseItem(int slot, boolean throwItem) {
        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        this.throwItem = throwItem;
        this.useItem = true;
    }
}
