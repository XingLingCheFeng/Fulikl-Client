package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import net.minecraft.block.AirBlock;
import net.minecraft.util.math.BlockPos;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "Eagle", category = ModuleCategory.MOVEMENT,description = "Automatically sneak when you are on the edge of the block.")
public class Eagle extends Module {
    @Override
    public void onDisable() {
        mc.options.sneakKey.setPressed(false);
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;
        mc.options.sneakKey.setPressed(mc.player.onGround && mc.world.getBlockState(new BlockPos(mc.player.getBlockPos()).add(0, -1, 0)).getBlock() instanceof AirBlock);
    }
}
