package missu.epsilon.client.features.modules.movement;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.events.player.MoveInputEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.utils.Wrapper;
import missu.epsilon.client.utils.entity.InventoryUtils;
import missu.epsilon.client.utils.packets.PacketUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Jon_awa & Daniel
 * @since 2025/10/25
 */
@ModuleInfo(name = "GuiMove",category = ModuleCategory.MOVEMENT, description = "Allows you to move around while in a gui")
public class GuiMove extends Module {
    private final ListValue mode = new ListValue("Mode", new String[]{"Vanilla", "Hypixel"}, "Vanilla");
    private final BoolValue stopSprint = new BoolValue("Stop Sprint", false);
    public static final BoolValue noUnlockMouse = new BoolValue("Lock mouse in inventory menus", false);
    private boolean sentC0E, sentInventoryPacket, isTimeUp;
    public static boolean sent = false;
    private long lastTime;

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (Wrapper.mc.player == null) {
            return;
        }
        if (!(mc.currentScreen instanceof GameMenuScreen) && !(mc.currentScreen instanceof ChatScreen) && mc.currentScreen != null) {
            setPressed(mc.options.forwardKey, InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.forwardKey.getDefaultKey().getCode()));
            setPressed(mc.options.backKey, InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.backKey.getDefaultKey().getCode()));
            setPressed(mc.options.rightKey, InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.rightKey.getDefaultKey().getCode()));
            setPressed(mc.options.leftKey, InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.leftKey.getDefaultKey().getCode()));
            setPressed(mc.options.jumpKey, InputUtil.isKeyPressed(mc.getWindow().getHandle(), mc.options.jumpKey.getDefaultKey().getCode()));
            if (stopSprint.get() && (mc.currentScreen instanceof GenericContainerScreen || sentC0E)) {
                setPressed(mc.options.sprintKey, false);
                mc.player.setSprinting(false);
                return;
            }
            if (Objects.requireNonNull(Client.moduleManager.getModule(Sprint.class)).getState()) {
                setPressed(mc.options.sprintKey, true);
            }
        }
    }

    private static void setPressed(KeyBinding key, boolean pressed) {
        key.setPressed(pressed);
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.packetState == CancellableEvent.PacketState.SEND && this.mode.is("Hypixel")) {
            if (event.getPacket() instanceof ClickSlotC2SPacket c0e) {
                if ((mc.currentScreen instanceof InventoryScreen || InventoryUtils.serverOpenInventory) && (c0e.getActionType() == SlotActionType.PICKUP || c0e.getActionType() == SlotActionType.QUICK_MOVE) && !c0e.getStack().isEmpty()) {
                    this.sentC0E = this.sentInventoryPacket = true;
                }
            } else if (event.getPacket() instanceof CloseHandledScreenC2SPacket) {
                this.sentInventoryPacket = true;
            } else if (event.getPacket() instanceof PlayerMoveC2SPacket) {
                this.sentInventoryPacket = false;
            }
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.eventState == CancellableEvent.EventState.PRE && mc.player != null && this.mode.is("Hypixel")) {
            var isInInventory = mc.currentScreen instanceof InventoryScreen || InventoryUtils.serverOpenInventory;

            if (this.sentC0E && !isInInventory) {
                this.sentC0E = false;
            }

            if (isInInventory) {
                if (!this.sentC0E && !this.sentInventoryPacket) {
                    PacketUtils.sendPacketNoEvent(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                    sent = true;
                }
            } else {
                sent = false;
            }

            if (!stopSprint.get()) {
                if ((mc.currentScreen instanceof GenericContainerScreen || this.sentC0E) && !mc.player.hasStatusEffect(StatusEffects.SPEED)) {
                    mc.player.setSprinting(false);
                    mc.options.jumpKey.setPressed(false);
                    mc.options.sprintKey.setPressed(false);
                } else if ((mc.currentScreen instanceof GenericContainerScreen || this.sentC0E)) {
                    mc.player.setSprinting(false);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (mc.player != null && this.mode.is("Hypixel")) {
            var i = 0;

            for (var key : getKeys()) {
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), key.getDefaultKey().getCode())) {
                    ++i;
                }
            }

            if (!stopSprint.get()) {
                if (!(mc.currentScreen instanceof GenericContainerScreen) && !this.sentC0E || !mc.player.hasStatusEffect(StatusEffects.SPEED) && i != 2 && mc.player.isOnGround()) {
                    if ((mc.currentScreen instanceof GenericContainerScreen || this.sentC0E) && !this.isTimeUp) {
                        event.forward = 0;
                        event.strafe = 0;

                        if (this.lastTime == 0) {
                            this.lastTime = System.currentTimeMillis();
                        }
                    } else {
                        this.lastTime = 0;
                    }
                } else {
                    event.forward = 0;
                    event.strafe = 0;
                }
            }

            if (this.lastTime != 0 && System.currentTimeMillis() - this.lastTime >= 60) {
                this.isTimeUp = true;
                this.lastTime = 0;
            }

            if (!(mc.currentScreen instanceof GenericContainerScreen) && !this.sentC0E) {
                this.isTimeUp = false;
            }
        }
    }

    private List<KeyBinding> getKeys() {
        return Arrays.asList(
                mc.options.forwardKey,
                mc.options.backKey,
                mc.options.leftKey,
                mc.options.rightKey,
                mc.options.jumpKey,
                mc.options.sprintKey
        );
    }
}
