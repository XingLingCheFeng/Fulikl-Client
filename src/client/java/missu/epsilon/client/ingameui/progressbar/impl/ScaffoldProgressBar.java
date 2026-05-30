package missu.epsilon.client.ingameui.progressbar.impl;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.visual.PostProcessing;
import missu.epsilon.client.features.modules.world.Scaffold;
import missu.epsilon.client.ingameui.progressbar.PBInterface;
import missu.epsilon.client.sxmurxy.builders.Builder;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.BlurTaskInstance;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltBlur;
import missu.epsilon.client.utils.animations.Animation;
import missu.epsilon.client.utils.animations.ContinualAnimation;
import missu.epsilon.client.utils.animations.Direction;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.animations.impl.EaseOutSine;
import missu.epsilon.client.utils.entity.InventoryUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

import static missu.epsilon.client.utils.Wrapper.mc;

public class ScaffoldProgressBar implements PBInterface {
    private final Animation animation = new EaseOutSine(300, 30f, Direction.BACKWARDS);
    private final ContinualAnimation progress = new ContinualAnimation();

    @Override
    public boolean shouldRender() {
        return Client.moduleManager.getModule(Scaffold.class).countMode.is("Top");
    }

    @Override
    public void render(Matrix4f matrix4f, DrawContext context, boolean nvg) {
        if (!shouldRender()) return;

        float width = 180f;
        float renderX = (float) mc.getWindow().getScaledWidth() / 2 - 90f;
        float renderY = 5f - animation.getOutput().floatValue();

        animation.setDirection(Client.moduleManager.getModule(Scaffold.class).isEnabled() ? Direction.BACKWARDS : Direction.FORWARDS);

        if (nvg) {
            createBlurBackground(matrix4f, renderX, renderY, width);

            RenderUtils.drawRoundedRect(renderX, renderY, width, 25f,
                    ColorPanel.createColorPanel(0f, 0f, 0f, 0.35f), 6);
        }

        ItemStack itemStack = InventoryUtils.getStackInHotbarSlot(Scaffold.getBlockSlot());
        RenderUtils.renderScaledItem(context, itemStack, renderX + 2f, renderY + 2f, 1.2f);

        float progressBarWidth = calculateProgressWidth();
        progress.animate(progressBarWidth, 50);

        if (nvg) {
            drawProgressBar(renderX + 27f, renderY + 11.5f, progress.getOutput());

            // 绘制文本
            String countText = Scaffold.getBlockCount() + "";
            float textX = renderX + 165.5f - FontManager.BoldPingFang.getStringWidth(countText, 22) / 2;
            FontManager.BoldPingFang.drawGlowString(22, countText, textX, renderY + 8.5f,
                    ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f),
                    ColorPanel.createColorPanel(0f, 0f, 0f, 0f), false, 4);
        }
    }

    private float calculateProgressWidth() {
        Scaffold scaffold = Client.moduleManager.getModule(Scaffold.class);
        return scaffold.isEnabled() ? (124f * Scaffold.getBlockCount() / Scaffold.blockCount) : 124f;
    }

    private void createBlurBackground(Matrix4f matrix4f, float x, float y, float width) {
        float outspace = 0f;
        BuiltBlur blur = Builder.blur()
                .size(new SizeState(width + outspace * 2f, (float) 25.0 + outspace * 2f))
                .radius(new QuadRadiusState(5f))
                .blurRadius(PostProcessing.blurStrength.get().floatValue())
                .smoothness(5f)
                .color(QuadColorState.TRANSPARENT)
                .position(new PositionState(x - outspace, y - outspace))
                .matrix4f(matrix4f)
                .build();
        BlurTaskInstance.addTask(blur);
    }

    private void drawProgressBar(float x, float y, float progressWidth) {
        RenderUtils.drawRoundedRect(x, y, (float) 124.0, 3f,
                ColorPanel.createColorPanel(0f, 0f, 0f, 0.35f), 1.5f);

        RenderUtils.drawRoundedRect(x, y, progressWidth, 3f,
                ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f), 1.5f);
    }
}