package missu.epsilon.client.features.modules.visual;

import ddev.SmtcLoader.Loader;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.ingameui.Dragging;
import missu.epsilon.client.sxmurxy.builders.Builder;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.BlurTaskInstance;
import missu.epsilon.client.sxmurxy.instance.TextureTaskInstance;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltBlur;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltTexture;
import missu.epsilon.client.utils.animations.Animation;
import missu.epsilon.client.utils.animations.ContinualAnimation;
import missu.epsilon.client.utils.animations.Direction;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.animations.impl.SmoothStepAnimation;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.render.ColorUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import net.minecraft.client.texture.AbstractTexture;

import java.awt.*;

@ModuleInfo(name = "MusicInfo", category = ModuleCategory.VISUAL)
public class MusicInfo extends Module {
    public static ContinualAnimation animation = new ContinualAnimation();

    private final Dragging dragging = Client.createDrag(this, "MusicInfo", 20, 55);
    public static Animation anim = new SmoothStepAnimation(250, 1f);
    private boolean firstEnable = true;
    private boolean lastDrawState = false;

    @EventTarget
    public void onTick(TickEvent tickEvent) {
        boolean canDraw = ClientData.curMusicinfo.state() == Loader.State.Playing && !ClientData.curMusicinfo.title().equals("No media playing");

        if (firstEnable) {
            if (!canDraw) {
                anim.setDirection(Direction.BACKWARDS);
                anim.reset();
            } else {
                anim.setDirection(Direction.FORWARDS);
                anim.reset();
            }
            firstEnable = false;
        }

        boolean stateChanged = canDraw != lastDrawState;

        if (stateChanged) {
            anim.setDirection(canDraw ? Direction.FORWARDS : Direction.BACKWARDS);
        }

        lastDrawState = canDraw;
    }

    @EventTarget
    public void onRender(RenderNvgEvent event) {
        float x = dragging.getXPos(), y = dragging.getYPos();

        boolean canDraw = ClientData.curMusicinfo.state() == Loader.State.Playing && !ClientData.curMusicinfo.title().equals("No media playing");
        float scaleValue = anim.getOutput().floatValue();

        render(scaleValue, x, y, event, canDraw, dragging);
    }


    public static void render(float scaleValue, float x, float y, RenderNvgEvent event, boolean canDraw, Dragging dragging) {
        float maxwidth = Math.max(134, Math.max(FontManager.BoldPingFang.getStringWidth(ClientData.curMusicinfo.title(), 22), FontManager.BoldPingFang.getStringWidth(ClientData.curMusicinfo.artist(), 18)) + 30);
        dragging.setWidth(maxwidth);
        dragging.setHeight(48);
        float height = dragging.getHeight();
        if (scaleValue > 0.01f) {
            float centerX = x + maxwidth / 2;
            float centerY = y + height / 2;

            float scaledWidth = maxwidth * scaleValue;
            float scaledHeight = height * scaleValue;
            float scaledX = centerX - scaledWidth / 2;
            float scaledY = centerY - scaledHeight / 2;

            Loader.MediaInfo curmusicInfo = ClientData.curMusicinfo;

            renderBackground(event, scaledX, scaledY, scaledWidth, scaledHeight, scaleValue);
            renderPlayerAvatar(curmusicInfo, scaledX, scaledY, scaleValue, event);


            if (canDraw || scaleValue > 0.1f) {
                renderTextAndOthers(event, curmusicInfo, scaledX, scaledY, scaledWidth, scaledHeight, scaleValue, dragging);
            }
        }
    }


    private static void renderBackground(RenderNvgEvent event, float scaledX, float scaledY, float scaledWidth, float scaledHeight, float scaleValue) {
        float outspace = 0f;

        if (scaleValue > 0.1f) {
            BuiltBlur blur = Builder.blur()
                    .size(new SizeState(scaledWidth + outspace * 2f, scaledHeight + outspace * 2f))
                    .radius(new QuadRadiusState(5f))
                    .blurRadius(PostProcessing.blurStrength.get().floatValue())
                    .smoothness(5f)
                    .color(QuadColorState.TRANSPARENT)
                    .position(new PositionState(scaledX - outspace, scaledY - outspace))
                    .matrix4f(event.matrix4f())
                    .build();
            BlurTaskInstance.addTask(blur);
        }

        float backgroundAlpha = 0.35f * scaleValue;
        RenderUtils.drawRoundedRect(scaledX, scaledY, scaledWidth, scaledHeight,
                ColorPanel.createColorPanel(0f, 0f, 0f, backgroundAlpha), 4 * scaleValue);
    }

    private static void renderPlayerAvatar(Loader.MediaInfo musicInfo, float scaledX, float scaledY, float scaleValue, RenderNvgEvent event) {
        float avatarSize = 36 * scaleValue;
        float avatarX = scaledX + 3.5f * scaleValue;
        float avatarY = scaledY + 1.5f * scaleValue;
        AbstractTexture abstractTexture = mc.getTextureManager().getTexture(ClientData.getCoverTexture(musicInfo,false));
        if (abstractTexture != null) {
            BuiltTexture texture = Builder.texture().matrix4f(event.matrix4f())
                    .position(new PositionState(avatarX, avatarY))
                    .size(new SizeState(avatarSize, avatarSize))
                    .texture(1f, 1f, 1F, 1F, abstractTexture)
                    .smoothness(1f)
                    .radius(new QuadRadiusState(4f * scaleValue, 4f * scaleValue, 4f * scaleValue, 4f * scaleValue))
                    .color(new QuadColorState(new Color(255, 255, 255, (int) (255 * scaleValue))))
                    .build();
            TextureTaskInstance.addTask(texture);
        }
    }

    private static void renderTextAndOthers(RenderNvgEvent event, Loader.MediaInfo musicInfo, float scaledX, float scaledY, float scaledWidth, float scaledHeight, float scaleValue, Dragging dragging) {
        float textX = scaledX + 42 * scaleValue;
        float textY = scaledY + 5 * scaleValue;
        String passTime = musicInfo.passTime();
        float TextY = scaledY + 40f * scaleValue;
        float BarX = (float) (scaledX + 4 * scaleValue);
        float BarY = scaledY + (dragging.getHeight() * scaleValue - 8 * scaleValue);
        float BarWidth = (scaledWidth - 28 * scaleValue);
        float BarHeight = 6 * scaleValue;
        float textAlpha = 0.75f * scaleValue;
        float TextX = BarX + BarWidth + 6 + (12 - FontManager.BoldPingFang.getStringWidth(passTime, 14)) / 2;
        float shadowAlpha = 0.35f * scaleValue;

        float nameTextSize = 18 * scaleValue;
        float artiTextSize = 14 * scaleValue;
        float TextSize = 12 * scaleValue;

        if (nameTextSize < 8f || TextSize < 8f) return;
        FontManager.BoldPingFang.drawGlowString(nameTextSize, musicInfo.title(), textX, textY,
                ColorPanel.createColorPanel(1f, 1f, 1f, textAlpha),
                ColorPanel.createColorPanel(0f, 0f, 0f, shadowAlpha), true, 4);

        float textY2 = scaledY + 20 * scaleValue;
        FontManager.BoldPingFang.drawGlowString(artiTextSize, musicInfo.artist(), textX, textY2,
                ColorPanel.createColorPanel(1f, 1f, 1f, textAlpha),
                ColorPanel.createColorPanel(0f, 0f, 0f, shadowAlpha), true, 4);

        FontManager.BoldPingFang.drawGlowString(TextSize, passTime, TextX, TextY + 0.5f,
                ColorPanel.createColorPanel(1f, 1f, 1f, textAlpha),
                ColorPanel.createColorPanel(0f, 0f, 0f, shadowAlpha), true, 4);

        float BarAlpha = 0.35f * scaleValue;
        RenderUtils.drawRoundedRect(BarX, BarY, BarWidth, BarHeight,
                ColorPanel.createColorPanel(0f, 0f, 0f, BarAlpha), 2f * scaleValue);

        float progress = musicInfo.progress() / 100.0f;
        float Pwidth = BarWidth * progress;

        if (anim.getDirection() == Direction.FORWARDS || scaleValue > 0.5f) {
            MusicInfo.animation.animate(Pwidth, 200);
        } else {
            MusicInfo.animation.animate(0, 40);
        }

        float animation = Math.min(MusicInfo.animation.getOutput(), BarWidth);

        float bgBarAlpha = 0.75f * scaleValue;

        RenderUtils.drawGradientRoundedRectLR(BarX, BarY, animation, BarHeight, ColorUtils.colorToColorPanel(ColorUtils.reAlpha(ClientSettings.color(0), (int) (ClientSettings.color(0).getAlpha() * bgBarAlpha))), ColorUtils.colorToColorPanel(ColorUtils.reAlpha(ClientSettings.color(10), (int) (ClientSettings.color(10).getAlpha() * bgBarAlpha))), 2f);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        resetAllAnimations();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        resetAllAnimations();
        firstEnable = true;
    }

    private void resetAllAnimations() {
        anim = new SmoothStepAnimation(250, 1f);
    }
}

