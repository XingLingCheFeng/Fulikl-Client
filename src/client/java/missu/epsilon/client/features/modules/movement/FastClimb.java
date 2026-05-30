package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.event.events.game.MoveEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.ListValue;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.VineBlock;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "FastClimb",category = ModuleCategory.MOVEMENT,description = "Make you climb ladder or vine faster")
public class FastClimb extends Module {

    public static ListValue mode = new ListValue("Mode",new String[]{"Grim(1.9-1.18.1)"},"Grim(1.9-1.18.1)");

    @SuppressWarnings("unused")
    @EventTarget
    public void onMove(MoveEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isClimbing() && mc.player.horizontalCollision && mode.is("Grim(1.9-1.18.1)")) {
            if (mc.player.getVelocity().x >= 0.07 || mc.player.getVelocity().z >= 0.07 || mc.player.getVelocity().x <= -0.07 || mc.player.getVelocity().z <= -0.07) return;
            if (mc.world.getBlockState(mc.player.getBlockPos().up()).getBlock() instanceof LadderBlock || mc.world.getBlockState(mc.player.getBlockPos().up()).getBlock() instanceof VineBlock) {
                event.y = 0.1786;
            }
        }
    }
}
