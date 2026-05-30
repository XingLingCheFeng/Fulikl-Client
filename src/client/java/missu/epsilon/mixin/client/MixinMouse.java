package missu.epsilon.mixin.client;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.player.UpdateMouseEvent;
import missu.epsilon.client.ingameui.clickgui.MouseBehavior;
import missu.epsilon.client.utils.entity.ItemSpoofUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin(Mouse.class)
public class MixinMouse {

    @Final @Shadow private MinecraftClient client;
    @Shadow public boolean leftButtonClicked;
    @Shadow private boolean middleButtonClicked;
    @Shadow public boolean rightButtonClicked;

    @Inject(method = "onMouseButton", at = @At(value = "TAIL"))
    public void onMouseButton(long window, int button, int action, int mods, CallbackInfo callbackInfo) {
        if (this.client.player == null) {
            return;
        }
        boolean activated = action == 1;
        if ((this.client.currentScreen instanceof ChatScreen || (this.client.currentScreen != null && this.client.currentScreen.getTitle().getString().contains("Epsilon-Controllable-GUI"))) && this.client.getOverlay() == null) {
            if (button == 0) {
                this.leftButtonClicked = activated;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                this.middleButtonClicked = activated;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                this.rightButtonClicked = activated;
            }
            KeyBinding.setKeyPressed(InputUtil.Type.MOUSE.createFromCode(button), activated);
            if (activated) {
                if (this.client.player.isSpectator() && button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                    this.client.inGameHud.getSpectatorHud().useSelectedCommand();
                } else {
                    KeyBinding.onKeyPressed(InputUtil.Type.MOUSE.createFromCode(button));
                }
            }
        }
    }

    @Inject(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onUpdateMouse(DD)V"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void updateMouse(double timeDelta, CallbackInfo ci, double i, double j, double d, double e, double f, int k) {
        UpdateMouseEvent event = new UpdateMouseEvent(i, j, timeDelta);
        Client.getInstance().getEventManager().call(event);
        if (!event.isCancelled()) {
            this.client.getTutorialManager().onUpdateMouse(event.getYaw() + i, event.getPitch() + j);
            if (this.client.player != null) {
                this.client.player.changeLookDirection(event.getYaw() + i, (event.getPitch() + j) * k);
            }
        }
        ci.cancel();
    }

    @ModifyArg(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/input/Scroller;scrollCycling(DII)I"), index = 1)
    private int onMouseScrollb(int selectedIndex) {
        return ItemSpoofUtils.getSpoofedSlot();
    }

    @Inject(method = "onMouseScroll", at = @At(value = "TAIL"))
    public void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo callbackInfo) {
        if (vertical > 0.0) {
            MouseBehavior.mouseScrollValue = -1f;
        }
        if (vertical < 0.0) {
            MouseBehavior.mouseScrollValue = 1f;
        }
    }

}