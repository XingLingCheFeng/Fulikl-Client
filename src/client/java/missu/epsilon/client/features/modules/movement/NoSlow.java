package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.event.events.game.ClickEvent;
import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.events.player.SlowdownEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.combat.AntiKnockback;
import missu.epsilon.client.features.modules.combat.KillAura;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.entity.BlinkUtils;
import missu.epsilon.client.utils.entity.ItemUtils;
import missu.epsilon.client.utils.entity.PlayerUtils;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static missu.epsilon.client.utils.Wrapper.mc;
import static missu.epsilon.client.utils.movement.MovementUtils.isMove;

/**
 * Updated by Daniel on 2026/02/12.
 */

@ModuleInfo(name = "NoSlow",description = "Disable slow effect when you are using item",category = ModuleCategory.MOVEMENT)
public class NoSlow extends Module {
    public static BoolValue sword = new BoolValue("Sword",true);
    public static final ListValue swordMode = new ListValue("Sword Mode", new String[]{"Vanilla", "Hypixel"}, "Vanilla");
    public static final NumberValue blinkTick = (NumberValue) new NumberValue("Hypixel Blink-Tick",2,1,2,1).displayable(() -> sword.get() && swordMode.is("Hypixel"));
    public static BoolValue consume = new BoolValue("Consume",true);
    public static final ListValue consumeMode = new ListValue("Consume Mode", new String[]{"Vanilla"}, "Vanilla");
    public static BoolValue bow = new BoolValue("Bow",true);
    public static final ListValue bowMode = new ListValue("Bow Mode", new String[]{"Vanilla"}, "Vanilla");

    public static boolean unblockState = false;
    public static boolean interactingBlockThisTick = false;
    public static boolean serverBlocking = false;
    public static boolean shouldSetState = false;
    public static boolean noSlowBlinked = false;
    public static boolean slowByWaitingServer;

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        if (sword.get() && swordMode.is("Hypixel")) {
            boolean holdingSword = !mc.player.getMainHandStack().isEmpty() && mc.player.getMainHandStack().getItem() instanceof SwordItem;
            boolean wantBlock = holdingSword && (KillAura.realBlock || mc.options.useKey.isPressed());

            if (interactingBlockThisTick) return;

            if (wantBlock) {
                if (unblockState) {
                    if (event.eventState == CancellableEvent.EventState.POST) {
                        BlinkUtils.stopBlink();
                        noSlowBlinked = false;
                        PlayerUtils.doItemUseWithoutBlock();
                        serverBlocking = true;
                        if (blinkTick.get() == 2) {
                            BlinkUtils.startBlink();
                            noSlowBlinked = true;
                        }
                        unblockState = false;
                    }
                } else {
                    if (event.eventState == CancellableEvent.EventState.PRE) {
                        if (blinkTick.get() == 1) {
                            BlinkUtils.startBlink();
                            noSlowBlinked = true;
                        }
                        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                        serverBlocking = false;
                        shouldSetState = true;
                    }
                    if (event.eventState == CancellableEvent.EventState.POST) {
                        if (shouldSetState) {
                            unblockState = true;
                            shouldSetState = false;
                        }
                    }
                }
            } else {
                if (noSlowBlinked) {
                    BlinkUtils.stopBlink();
                    noSlowBlinked = false;
                }
                if (serverBlocking) {
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
                    serverBlocking = false;
                }
            }
        }

        if (mc.player.getMainHandStack().isEmpty()) return;

        if (!isMove()) return;

        if (mc.player.getMainHandStack().getItem() instanceof SwordItem && !ItemUtils.isConsumable(mc.player.getOffHandStack()) && (mc.player.isUsingItem() || KillAura.realBlock || mc.options.useKey.isPressed()) && sword.get()) {
            switch (swordMode.get()) {
                case "Vanilla","Hypixel":
                    slowByWaitingServer = false;
                    break;
            }
        }

        if ((ItemUtils.isConsumable(mc.player.getMainHandStack()) || (ItemUtils.isConsumable(mc.player.getOffHandStack()))) && mc.player.isUsingItem() && consume.get()) {
            switch (consumeMode.get()) {
                case "Vanilla":
                    slowByWaitingServer = false;
                    break;
            }
        }

        if (((mc.player.getMainHandStack().getItem() instanceof BowItem || mc.player.getMainHandStack().getItem() instanceof CrossbowItem) || (mc.player.getOffHandStack().getItem() instanceof BowItem || mc.player.getOffHandStack().getItem() instanceof CrossbowItem) && ItemUtils.isConsumable(mc.player.getMainHandStack())) && mc.player.isUsingItem() && bow.get()) {
            switch (bowMode.get()) {
                case "Vanilla":
                    slowByWaitingServer = false;
                    break;
            }
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onSlowDown(SlowdownEvent event) {
        if (mc.player == null) return;
        if (mc.player.getMainHandStack().isEmpty()) return;
        if ((mc.player.getMainHandStack().getItem() instanceof SwordItem && !ItemUtils.isConsumable(mc.player.getOffHandStack()) && (mc.player.isUsingItem() || KillAura.realBlock || mc.options.useKey.isPressed()) && sword.get()) || ((ItemUtils.isConsumable(mc.player.getMainHandStack()) || (ItemUtils.isConsumable(mc.player.getOffHandStack()))) && mc.player.isUsingItem() && consume.get()) || (((mc.player.getMainHandStack().getItem() instanceof BowItem || mc.player.getMainHandStack().getItem() instanceof CrossbowItem) || (mc.player.getOffHandStack().getItem() instanceof BowItem || mc.player.getOffHandStack().getItem() instanceof CrossbowItem) && ItemUtils.isConsumable(mc.player.getMainHandStack())) && mc.player.isUsingItem() && bow.get())) {
            if (slowByWaitingServer) {
                event.forward = 0.2F;
                event.sideways = 0.2F;
            } else {
                event.forward = 1F;
                event.sideways = 1F;
            }
        } else {
            event.forward = 0.2F;
            event.sideways = 0.2F;
        }
    }

    public static boolean allowStopUsingItem() {
        if (mc.player == null) return false;

        boolean holdingSword = mc.player.getMainHandStack().getItem() instanceof SwordItem;
        boolean wantBlock = holdingSword && (KillAura.realBlock || mc.options.useKey.isPressed());
        return !wantBlock && !noSlowBlinked;
    }
}