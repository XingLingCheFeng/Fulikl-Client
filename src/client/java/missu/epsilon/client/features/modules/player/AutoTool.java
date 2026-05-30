package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.event.events.player.ClickBlockEvent;
import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.world.BedBreaker;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.utils.entity.ItemSpoofUtils;
import missu.epsilon.client.utils.entity.ItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "AutoTool", category = ModuleCategory.PLAYER, hide = true)
public class AutoTool extends Module {
    public static final BoolValue spoof = new BoolValue("Spoof", true);
    private int originalSlot = -1;
    private boolean hasStartedSpoofing = false;

    @SuppressWarnings("unused")
    @EventTarget
    public void onClick(ClickBlockEvent event) {
        switchSlot(event.clickedBlock);
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player == null || event.eventState != CancellableEvent.EventState.PRE) return;
        if (!mc.options.attackKey.isPressed() && BedBreaker.breakingBlockPos == null) {
            if (hasStartedSpoofing) {
                if (originalSlot != -1) {
                    mc.player.getInventory().selectedSlot = originalSlot;
                    originalSlot = -1;
                }
                ItemSpoofUtils.stopSpoof();
                hasStartedSpoofing = false;
            }
        }
    }

    public void switchSlot(BlockPos blockPos) {
        if (mc.world == null || mc.player == null) return;
        float bestSpeed = 1F;
        int bestSlot = -1;

        BlockState blockState = mc.world.getBlockState(blockPos);

        for (int i = 0; i <= 8; i++) {
            ItemStack item = mc.player.getInventory().getStack(i);
            if (ItemUtils.isGodItem(item)) {
                continue;
            }
            if (!item.isEmpty()) {
                float speed = item.getMiningSpeedMultiplier(blockState);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
        }
        if (bestSlot != -1 && mc.player.getInventory().selectedSlot != bestSlot) {
            if (spoof.get() && !ItemSpoofUtils.isSpoofing) {
                ItemSpoofUtils.startSpoof();
                hasStartedSpoofing = true;
            }
            if (originalSlot == -1) {
                originalSlot = mc.player.getInventory().selectedSlot;
            }
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }
}
