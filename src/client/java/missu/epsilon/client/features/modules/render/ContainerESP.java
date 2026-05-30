package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.event.events.render.Render3DEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.visual.ClientSettings;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.entity.PlayerUtils;
import missu.epsilon.client.utils.render.ColorUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import net.minecraft.block.entity.*;
import net.minecraft.util.math.Box;

@ModuleInfo(name = "ContainerESP",category = ModuleCategory.RENDER,description = "Draw a box of the container to see")
public class ContainerESP extends Module {
    public static MultiBoolValue container = new MultiBoolValue("Interactive Container",new BoolValue[]{
            new BoolValue("Chest",true),
            new BoolValue("Furnace",false),
            new BoolValue("BlastFurnace",false),
            new BoolValue("SmokerFurnace", false),
            new BoolValue("BrewingStand", false)
    });
    private final BoolValue mark = new BoolValue("Exclude Opened Container", true);
    private final NumberValue range = new NumberValue("Range", 120, 20, 360, 10);

    @SuppressWarnings("unused")
    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }

        for (BlockEntity blockEntity : PlayerUtils.getBlockEntities(this.range.getValue())) {
            if ((blockEntity instanceof ChestBlockEntity && container.get("Chest")
                    || blockEntity instanceof FurnaceBlockEntity && container.get("Furnace")
                    || blockEntity instanceof BlastFurnaceBlockEntity && container.get("BlastFurnace")
                    || blockEntity instanceof SmokerBlockEntity && container.get("SmokerFurnace")
                    || blockEntity instanceof BrewingStandBlockEntity && container.get("BrewingStand")) && (!ClientData.clickedContainers.contains(blockEntity) || !mark.get())) {
                    RenderUtils.drawBox(event.getMatrixStack(), new Box(blockEntity.getPos()), ColorUtils.reAlpha(ClientSettings.color(0), 120), false, null, true);
            }
        }
    }
}
