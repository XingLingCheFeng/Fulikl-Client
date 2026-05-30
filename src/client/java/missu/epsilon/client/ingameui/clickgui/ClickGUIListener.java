package missu.epsilon.client.ingameui.clickgui;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.PreClientUpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.modules.visual.ClickGUI;
import missu.epsilon.client.utils.Wrapper;
import missu.epsilon.client.utils.animations.basic.animation.AnimationState;
import missu.epsilon.client.utils.animations.basic.animation.frameAnimation.FrameAnimation;
import missu.epsilon.client.utils.client.ClientUtils;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Objects;

public class ClickGUIListener implements Wrapper {
    public static boolean isKeyDowned;

    public static ClickGUIListener register() {
        ClickGUIListener clickGUIListener = new ClickGUIListener();
        Client.getInstance().getEventManager().subscribe(clickGUIListener);
        clickGUIListener.resetModCategoryState();

        return clickGUIListener;
    }

    /**
     * KeyInput listener
     */
    @SuppressWarnings("unused")
    @EventTarget
    public void onClientUpdate(PreClientUpdateEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }

        if (mc.currentScreen != null) {
            return;
        }

        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            if (!isKeyDowned) {
                FrameAnimation.animationSpeed *= 2f;

                Objects.requireNonNull(Client.moduleManager.getModule(ClickGUI.class)).setEnabled(true);

                mc.setScreen(Client.clickGUI);
            }
            isKeyDowned = true;
        } else {
            isKeyDowned = false;
        }
    }

    public void resetModCategoryState() {
        Arrays.stream(ModuleCategory.values()).toList().getFirst().getSwitchedAnimation().update(AnimationState.FORWARDS);
    }

}