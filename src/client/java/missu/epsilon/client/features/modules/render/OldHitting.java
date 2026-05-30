package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.event.events.render.Render2DEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import net.minecraft.item.ItemStack;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Hand;

import static missu.epsilon.client.utils.Wrapper.mc;

/**
 * By Marta & Daniel & Zeroth
 */

@ModuleInfo(name = "OldHitting",description = "Fix some problems with some versions",category = ModuleCategory.RENDER)
public class OldHitting extends Module {
    public static ListValue cameraVersion = new ListValue("Camera position version",new String[]{"Pre 1.8","Pre 1.13","Pre 1.14","Latest"},"Pre 1.8");
    public static BoolValue sneakTiming = new BoolValue("Sneak timing (Pre 1.21.3)",false);
    public static BoolValue cancelSwimming = new BoolValue("Cancel swimming (Pre 1.13)",false);
    public static BoolValue blocking = new BoolValue("Always sword blocking (Pre 1.9)",false);
    public static BoolValue noCooldown = new BoolValue("Remove item cooldown (Pre 1.9)",false);
    public static BoolValue swingWhileUsing = new BoolValue("Swing while using item (1.7)",false);

    public static BoolValue smoothSneaking = new BoolValue("Sneak interpolation (Since 1.13)",false);
    public static BoolValue alternativeSmoothSneaking = new BoolValue("Old sneak interpolation (1.7)",false);
    public static BoolValue fakeEyeHeight = new BoolValue("Old eye height",false);
    public static BoolValue bodyOffset = new BoolValue("Offset sneaking body position (1.7)",false);
    public static BoolValue syncPlayerModelWithEyeHeight = new BoolValue("Sync player model size (1.7)",false);
    public static BoolValue sneakAnimationWhileFlying = new BoolValue("Show sneak animation while flying (Pre 1.12)",false);


    @EventTarget
    public void onUpdate(Render2DEvent event) {
        if (mc.player == null) return;

        if (swingWhileUsing.get()) {
            if (mc.player.handSwinging) {
                return;
            }

            boolean using = mc.player.isUsingItem() && mc.options.attackKey.isPressed();

            if (!using) return;

            for (ItemStack stack : mc.player.getHandItems()){
                if (stack.getUseAction() != UseAction.NONE && mc.player.getActiveItem() == stack) {
                    mc.player.handSwinging = true;
                    mc.player.handSwingTicks = 0;
                    mc.player.preferredHand = Hand.MAIN_HAND;
                    return;
                }

                if (stack.getUseAction() != UseAction.NONE && mc.player.getActiveItem() == stack) {
                    mc.player.handSwinging = true;
                    mc.player.handSwingTicks = 0;
                    mc.player.preferredHand = Hand.OFF_HAND;
                }
            }
        }
    }


}
