package missu.epsilon.client.ingameui.progressbar.impl;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.visual.PostProcessing;
import missu.epsilon.client.features.modules.world.BedBreaker;
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
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

import static missu.epsilon.client.utils.Wrapper.mc;

public class BedNukerProgressBar implements PBInterface {
    private final Animation animation = new EaseOutSine(300, 30f, Direction.BACKWARDS);
    private final ContinualAnimation progress = new ContinualAnimation();

    @Override
    public boolean shouldRender() {
        return Client.moduleManager.getModule(BedBreaker.class).render.is("Top") && BedBreaker.pos != null;
    }

    @Override
    public void render(Matrix4f matrix4f, DrawContext context, boolean nvg) {
        if (!shouldRender()) return;

        float width = 190f;
        float renderX = (float) mc.getWindow().getScaledWidth() / 2 - 95f;
        float renderY = calculateRenderY();

        animation.setDirection(BedBreaker.pos != null ? Direction.BACKWARDS : Direction.FORWARDS);

        if (nvg) {
            createBlurBackground(matrix4f, renderX, renderY, width, 25f);

            RenderUtils.drawRoundedRect(renderX, renderY, width, 25f,
                    ColorPanel.createColorPanel(0f, 0f, 0f, 0.35f), 6);
        }

        if (BedBreaker.pos != null) {
            BlockState state = mc.world.getBlockState(BedBreaker.pos);
            ItemStack stack = state.getBlock().asItem().getDefaultStack();
            RenderUtils.renderScaledItem(context, stack, renderX + 2f, renderY + 2f, 1.2f);
        }

        float progressBarWidth = calculateProgressWidth();
        progress.animate(progressBarWidth, 50);

        if (nvg) {
            drawProgressBar(renderX + 27f, renderY + 11.5f, progress.getOutput());

            String percentText = Math.min((int) (BedBreaker.currentDamage * 100), 100) + "%";
            float textX = renderX + 183.5f - FontManager.BoldPingFang.getStringWidth(percentText, 22);
            FontManager.BoldPingFang.drawGlowString(22, percentText, textX, renderY + 8.5f,
                    ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f),
                    ColorPanel.createColorPanel(0f, 0f, 0f, 0f), false, 4);
        }
    }

    private float calculateRenderY() {
        float baseY = 5f;
        boolean scaffoldActive = Client.moduleManager.getModule(Scaffold.class).isEnabled() &&
                Client.moduleManager.getModule(Scaffold.class).countMode.is("Top");
        float offsetY = scaffoldActive ? 30f : 0f;
        return baseY + offsetY - animation.getOutput().floatValue();
    }

    private float calculateProgressWidth() {
        return Client.moduleManager.getModule(BedBreaker.class).isEnabled() ?
                (124f * BedBreaker.currentDamage) : 124f;
    }

    private void createBlurBackground(Matrix4f matrix4f, float x, float y, float width, float height) {
        float outspace = 0f;
        BuiltBlur blur = Builder.blur()
                .size(new SizeState(width + outspace * 2f, height + outspace * 2f))
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
        // 绘制进度条背景
        RenderUtils.drawRoundedRect(x, y, (float) 124.0, 3f,
                ColorPanel.createColorPanel(0f, 0f, 0f, 0.35f), 1.5f);

        // 绘制进度条前景
        RenderUtils.drawRoundedRect(x, y, progressWidth, 3f,
                ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f), 1.5f);
    }
}