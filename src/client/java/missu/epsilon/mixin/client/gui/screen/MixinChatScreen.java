package missu.epsilon.mixin.client.gui.screen;

import missu.epsilon.client.Client;
import missu.epsilon.client.config.impl.DragConfig;
import missu.epsilon.client.event.events.command.MessageEvent;
import missu.epsilon.client.ingameui.Dragging;
import missu.epsilon.client.ingameui.clickgui.MouseBehavior;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.skidonion.obfuscator.annotations.Renamer;

import java.util.Objects;

@Renamer(obfuscated = false)
@Mixin(ChatScreen.class)
public class MixinChatScreen extends Screen {

    protected MixinChatScreen(Text title) {
        super(title);
    }

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    public void sendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        MessageEvent messageEvent = new MessageEvent(chatText);
        Client.getInstance().getEventManager().call(messageEvent);
       // Client.commandManager.receive(messageEvent);
        if (messageEvent.isCancelled()) {
            Objects.requireNonNull(this.client).inGameHud.getChatHud().addToMessageHistory(chatText);
            Objects.requireNonNull(this.client).setScreen(null);
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (DragConfig.draggables != null) {
            DragConfig.draggables.values().forEach(dragging -> {
                if (dragging.getModule().isEnabled() && dragging.isDragging()) {
                    long window = Objects.requireNonNull(client).getWindow().getHandle();
                    if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) != org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                        dragging.onRelease(0);
                    }
                }
            });
        }

        Objects.requireNonNull(DragConfig.draggables).values().forEach(dragging -> {
            if (dragging.getModule().isEnabled()) {
                dragging.onDraw(mouseX, mouseY);
            }
        });
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void moveHUD(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        boolean hoveringResetButton = MouseBehavior.mouseHoveredFullScreen(width / 2f - 100, 20F, 200F, 20F, (int) mouseX, (int) mouseY);
        if (hoveringResetButton && button == 0) {
            for (Dragging dragging : DragConfig.draggables.values()) {
                dragging.setX(dragging.getXPos());
                dragging.setY(dragging.getYPos());
            }
            return;
        }
        DragConfig.draggables.values().forEach(dragging -> {
            if (dragging.getModule().isEnabled()) {
                dragging.onClick((int) mouseX, (int) mouseY, button);
            }
        });
    }

}
