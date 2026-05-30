package missu.epsilon.mixin.client.render.entity;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {

    /**
     * @author Jon_awa
     * @reason For client 1.8 Animation
     */
//    @Overwrite
//    private static BipedEntityModel.ArmPose getArmPose(PlayerEntity player, ItemStack stack, Hand hand) {
//        boolean state = Client.moduleManager.getModule(Animation.class).isEnabled();
//        if (stack.isEmpty()) {
//            return BipedEntityModel.ArmPose.EMPTY;
//        } else {
//            if (state && player.getMainHandStack() == stack && Animation.blockAnimation.getValue() && (!Animation.shieldCheck.getValue() || player.getOffHandStack().getItem() instanceof ShieldItem) && (Animation.everythingBlock.getValue() || stack.getItem() instanceof SwordItem) && ((!(player.getOffHandStack().getItem() instanceof BowItem) && !(player.getOffHandStack().getItem() instanceof EnderPearlItem) && !(player.getOffHandStack().getItem() instanceof ThrowablePotionItem) && !(player.getOffHandStack().getItem() instanceof SnowballItem) && !(player.getOffHandStack().getItem() instanceof EggItem) && !(player.getOffHandStack().getItem() instanceof TridentItem) && (!(player.getOffHandStack().getItem() instanceof BlockItem) || mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) && !ItemUtils.isFood(player.getOffHandStack())) || KillAura.renderBlock) && ((player == mc.player && (mc.options.useKey.isPressed() || KillAura.renderBlock)) || (player != mc.player && player.isUsingItem() && player.getOffHandStack().getItem() instanceof ShieldItem && player.getMainHandStack().getItem() instanceof SwordItem))) {
//                return BipedEntityModel.ArmPose.BLOCK;
//            } else if (player.getActiveHand() == hand && player.getItemUseTimeLeft() > 0) {
//                UseAction useAction = stack.getUseAction();
//                if (useAction == UseAction.BLOCK) {
//                    if (stack.getItem() instanceof ShieldItem) {
//                        if (!state || !Animation.blockAnimation.getValue() || Animation.renderShield.getValue() || !(player.getMainHandStack().getItem() instanceof SwordItem) || player.getMainHandStack() == stack) {
//                            return BipedEntityModel.ArmPose.BLOCK;
//                        } else {
//                            return BipedEntityModel.ArmPose.ITEM;
//                        }
//                    } else {
//                        return BipedEntityModel.ArmPose.BLOCK;
//                    }
//                }
//                if (useAction == UseAction.BOW) {
//                    return BipedEntityModel.ArmPose.BOW_AND_ARROW;
//                }
//                if (useAction == UseAction.SPEAR) {
//                    return BipedEntityModel.ArmPose.THROW_SPEAR;
//                }
//                if (useAction == UseAction.CROSSBOW) {
//                    return BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
//                }
//                if (useAction == UseAction.SPYGLASS) {
//                    return BipedEntityModel.ArmPose.SPYGLASS;
//                }
//                if (useAction == UseAction.TOOT_HORN) {
//                    return BipedEntityModel.ArmPose.TOOT_HORN;
//                }
//                if (useAction == UseAction.BRUSH) {
//                    return BipedEntityModel.ArmPose.BRUSH;
//                }
//            } else if (!player.handSwinging && stack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(stack)) {
//                return BipedEntityModel.ArmPose.CROSSBOW_HOLD;
//            }
//            return BipedEntityModel.ArmPose.ITEM;
//        }
//    }

}
