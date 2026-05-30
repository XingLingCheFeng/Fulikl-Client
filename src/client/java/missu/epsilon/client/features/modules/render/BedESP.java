package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.event.events.render.Render3DEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.visual.ClientSettings;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.render.ColorUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "BedESP",category = ModuleCategory.RENDER,description = "Draw a box to render bed through wall")
public class BedESP extends Module {
    private final NumberValue radius = new NumberValue("Radius", 10, 1, 100,1);

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (ClientUtils.isNull()) return;

        World world = mc.world;
        BlockPos playerPos = mc.player.getBlockPos();

        int r = radius.get().intValue();

        for (BlockPos pos : BlockPos.iterateOutwards(playerPos, r, 5, r)) {
            BlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof BedBlock bed)) continue;

            if (state.get(BedBlock.PART) != BedPart.HEAD)
                continue; // 只渲染床头

            Direction facing = state.get(BedBlock.FACING);
            BlockPos footPos = pos.offset(facing.getOpposite());

            // 合并 AABB
            double minX = Math.min(pos.getX(), footPos.getX());
            double minY = Math.min(pos.getY(), footPos.getY());
            double minZ = Math.min(pos.getZ(), footPos.getZ());
            double maxX = Math.max(pos.getX(), footPos.getX()) + 1.0;
            double maxY = Math.max(pos.getY(), footPos.getY()) + 0.5625;
            double maxZ = Math.max(pos.getZ(), footPos.getZ()) + 1.0;

            Box box = new Box(minX, minY, minZ, maxX, maxY, maxZ);

            RenderUtils.drawBox(event.getMatrixStack(), box, ColorUtils.reAlpha(ClientSettings.color(0), 100), false, null, true);
        }
    }

}