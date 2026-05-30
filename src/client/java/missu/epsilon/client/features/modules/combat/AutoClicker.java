package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.entity.ItemUtils;
import missu.epsilon.client.utils.entity.PlayerUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import net.minecraft.item.*;
import net.minecraft.util.hit.HitResult;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "AutoClicker",category = ModuleCategory.COMBAT)
public class AutoClicker extends Module {
    private final NumberValue cps = new NumberValue("CPS", 14D, 1, 20, 1);
    private final BoolValue attackCoolDown = new BoolValue("1.9+ Attack", false);
    private final BoolValue itemCheck = new BoolValue("Item Check ", false);
    private final BoolValue onlyWhenCanAttack = new BoolValue("When Attackable", false);
    private final BoolValue rightClick = new BoolValue("Right Click", true);
    private final BoolValue leftClick = new BoolValue("Left Click", true);

    private final TimerUtils clickStopWatch = new TimerUtils();
    private long nextSwing;
    private int ticksDown;

    @EventTarget
    public void onTick(TickEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }

        mc.attackCooldown = 0;

        if (clickStopWatch.hasTimeElapsed(this.nextSwing) && mc.currentScreen == null) {
            if (mc.options.attackKey.isPressed()) {
                ticksDown++;
            } else {
                ticksDown = 0;
            }

            this.nextSwing = 1000 / this.cps.get().longValue();

            if (this.rightClick.get() && mc.options.useKey.isPressed()) {
                if (!itemCheck.get() ||
                        !(mc.player.getMainHandStack().getItem() instanceof EnderPearlItem
                                        || mc.player.getOffHandStack().getItem() instanceof EnderPearlItem
                                        || mc.player.getMainHandStack().getItem() instanceof BowItem
                                        || (mc.player.getOffHandStack().getItem() instanceof SnowballItem && !(mc.player.getMainHandStack().getItem() instanceof BlockItem))
                                        || (mc.player.getOffHandStack().getItem() instanceof SnowballItem && !(mc.player.getMainHandStack().getItem() == Items.GOLDEN_APPLE))
                                        || (mc.player.getOffHandStack().getItem() instanceof SnowballItem && !(mc.player.getMainHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE))
                                        || (mc.player.getOffHandStack().getItem() instanceof EggItem && !(mc.player.getMainHandStack().getItem() instanceof BlockItem))
                                        || (mc.player.getOffHandStack().getItem() instanceof EggItem && !(mc.player.getMainHandStack().getItem() == Items.GOLDEN_APPLE))
                                        || (mc.player.getOffHandStack().getItem() instanceof EggItem && !(mc.player.getMainHandStack().getItem() == Items.ENCHANTED_GOLDEN_APPLE))
                                        || mc.player.getMainHandStack().getItem() == Items.WATER_BUCKET
                                        || mc.player.getMainHandStack().getItem() == Items.LAVA_BUCKET
                                        || mc.player.getMainHandStack().getItem() == Items.BUCKET
                                        || mc.player.getMainHandStack().getItem() instanceof FireChargeItem
                                        || (mc.player.getOffHandStack().getItem() instanceof FireChargeItem && !(mc.player.getMainHandStack().getItem() instanceof BlockItem))
                                        || ItemUtils.isFood(mc.player.getMainHandStack())
                                        || ItemUtils.isFood(mc.player.getOffHandStack())
                        )
                ) {
                    PlayerUtils.sendClick(1, true);

                    if (Math.random() > 0.9) {
                        PlayerUtils.sendClick(1, true);
                    }
                }
            } else if (this.leftClick.get() && this.ticksDown > 1 && (Math.sin(this.nextSwing) + 1 > Math.random() || Math.random() > 0.25 || this.clickStopWatch.hasTimeElapsed(200)) && (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK)) {
                if ((!this.attackCoolDown.get() || mc.player.getAttackCooldownProgress(0) == 1) && (mc.targetedEntity != null || !this.onlyWhenCanAttack.get())) {
                    PlayerUtils.sendClick(0, true);
                }
            }

            this.clickStopWatch.reset();
        }
    }
}
