package missu.epsilon.client.ingameui.clickgui;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.render.RenderLastScreenEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.modules.visual.PostProcessing;
import missu.epsilon.client.ingameui.clickgui.page.MainPage;
import missu.epsilon.client.sxmurxy.builders.Builder;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.BlurTaskInstance;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltBlur;
import missu.epsilon.client.utils.animations.basic.animation.AnimationState;
import missu.epsilon.client.utils.animations.basic.animation.editedAnimation.EaseFlyingAnimation;
import missu.epsilon.client.utils.animations.basic.animation.frameAnimation.FrameAnimation;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.render.font.FontManager;
import missu.epsilon.client.utils.render.font.Icon;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static missu.epsilon.client.utils.Wrapper.mc;

public class ClickGUI extends Screen {

    public ClickGUI() {
        super(Text.of("Epsilon-Controllable-GUI"));
    }

    public static ClickGUI register() {
        ClickGUI clickGUI = new ClickGUI();
        Client.getInstance().getEventManager().subscribe(clickGUI);
        return clickGUI;
    }

    public static final EaseFlyingAnimation blurAnimation = new EaseFlyingAnimation(500);
    public static final EaseFlyingAnimation animation = new EaseFlyingAnimation(500);
    missu.epsilon.client.features.modules.visual.ClickGUI module = Client.moduleManager.getModule(missu.epsilon.client.features.modules.visual.ClickGUI.class);

    @SuppressWarnings("unused")
    @EventTarget
    public void onLatestRenderScreen(RenderLastScreenEvent event) {
        if (!(mc.currentScreen instanceof ClickGUI)) {
            blurAnimation.update(AnimationState.BACKWARDS);
            animation.update(AnimationState.BACKWARDS);
        }

        if (animation.value() > 0f) {
            if (blurAnimation.value() > 0f) {
                float backgroundAlpha = animation.value();

                float backgroundWidth = 450f, backgroundHeight = 262f, backgroundRadius = 12f;
                float backgroundX = mc.getWindow().getScaledWidth() / 2f - backgroundWidth / 2f, backgroundY = mc.getWindow().getScaledHeight() / 2f - backgroundHeight / 2f;

                if (animation.animationState.equals(AnimationState.FORWARDS))
                    backgroundY += 25f * (1f - animation.value());
                if (animation.animationState.equals(AnimationState.BACKWARDS))
                    backgroundY -= 25f * (1f - animation.value());

                float strength = (float) (blurAnimation.value() * PostProcessing.blurStrength.get());

                if (strength > 0f) {
                    float smoothness = 5f;
                    BuiltBlur blur = Builder.blur()
                            .size(new SizeState(backgroundWidth + smoothness, backgroundHeight + smoothness))
                            .radius(new QuadRadiusState(backgroundRadius))
                            .blurRadius(strength)
                            .smoothness(smoothness)
                            .color(QuadColorState.TRANSPARENT)
                            .position(new PositionState(backgroundX - smoothness / 2f, backgroundY - smoothness / 2f))
                            .matrix4f(event.matrix4f())
                            .build();
                    BlurTaskInstance.addTask(blur);
                }
                MainPage.onRenderMainPage();
            }


            FontManager.FilledMaterial.drawStringOpposite(12f, Icon.NEAR_ME, (float) (MouseBehavior.mouseX), (float) (MouseBehavior.mouseY), ColorPanel.createColorPanel(1f, 1f, 1f, 0.65f * animation.value()), false);
        }
    }

    @Override
    public void close() {
        FrameAnimation.animationSpeed *= 0.5f;

        blurAnimation.update(AnimationState.BACKWARDS);
        animation.update(AnimationState.BACKWARDS);

        mc.setScreen(null);

        MouseBehavior.resetState();

        Objects.requireNonNull(module).setEnabled(false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        blurAnimation.update(AnimationState.FORWARDS);
        animation.update(AnimationState.FORWARDS);

        GLFW.glfwSetInputMode(mc.getWindow().getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);

        MouseBehavior.updatePosition(mouseX, mouseY);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        MainPage.handleKeyInput(keyCode);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        MainPage.handleCharInput(chr);
        return super.charTyped(chr, modifiers);
    }
}
