package missu.epsilon.client.features.modules.visual.targethud.renderer;

import missu.epsilon.client.features.modules.visual.ClientSettings;
import missu.epsilon.client.features.modules.visual.PostProcessing;
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
import missu.epsilon.client.utils.animations.ContinualAnimation;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.render.ColorUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.entity.LivingEntity;
import org.joml.Matrix4f;

import java.awt.*;

import static missu.epsilon.client.utils.Wrapper.*;

public class TargetHudRenderer {

    public static ContinualAnimation animation1 = new ContinualAnimation();
    public static ContinualAnimation animation2 = new ContinualAnimation();

    public static void render(float scaleValue, float x, float y, Matrix4f matrix4f, LivingEntity renderTarget, Dragging dragging) {
        String name = renderTarget.getName().getString();
        float space = 9f * scaleValue, avatar = 32f * scaleValue, nameTextSize = 18f * scaleValue, healthTextSize = 15f * scaleValue;
        float maxWidth = space + avatar + FontManager.BoldPingFang.getStringWidth(name, nameTextSize) + 40f;

        dragging.setWidth(maxWidth);
        dragging.setHeight(46f);

        float height = dragging.getHeight();

        if (scaleValue > 0f) {
            float centerX = x + maxWidth / 2f, centerY = y + height / 2f;
            float scaledWidth = maxWidth * scaleValue, scaledHeight = height * scaleValue;
            float scaledX = centerX - scaledWidth / 2f, scaledY = centerY - scaledHeight / 2f;

            renderBackground(matrix4f, scaledX, scaledY, scaledWidth, scaledHeight, scaleValue);
            LivingEntity target = renderTarget instanceof AbstractClientPlayerEntity ? renderTarget : mc.player;
            renderPlayerAvatar(target, scaledX, scaledY, scaleValue, matrix4f);
            renderTextAndHealthBarOthers(renderTarget, scaledX, scaledY, scaledWidth, scaleValue);
        }
    }

    private static void renderBackground(Matrix4f matrix4f, float scaledX, float scaledY, float scaledWidth, float scaledHeight, float scaleValue) {
        float smoothness = 5f;
        BuiltBlur blur = Builder.blur().size(new SizeState(scaledWidth + smoothness, scaledHeight + smoothness)).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).color(QuadColorState.TRANSPARENT).position(new PositionState(scaledX - smoothness / 2f, scaledY - smoothness / 2f)).matrix4f(matrix4f).build();
        BlurTaskInstance.addTask(blur);
        RenderUtils.drawRoundedRect(scaledX, scaledY, scaledWidth, scaledHeight, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f * scaleValue), 5f * scaleValue);
    }

    private static void renderPlayerAvatar(LivingEntity renderTarget, float scaledX, float scaledY, float scaleValue, Matrix4f matrix4f) {
        float avatarSize = 32f * scaleValue;
        float avatarX = scaledX + 3f * scaleValue;
        float avatarY = scaledY + 3f * scaleValue;
        AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(((AbstractClientPlayerEntity) renderTarget).getSkinTextures().texture());
        if (abstractTexture != null) {
            BuiltTexture texture = Builder.texture().matrix4f(matrix4f).position(new PositionState(avatarX, avatarY)).size(new SizeState(avatarSize, avatarSize)).texture(0.125f, 0.125f, 0.125f, 0.125f, abstractTexture).smoothness(1f).radius(new QuadRadiusState(3f * scaleValue, 3f * scaleValue, 3f * scaleValue, 3f * scaleValue)).color(new QuadColorState(new Color(255, 255, 255, (int) (200 * scaleValue)))).build();
            TextureTaskInstance.addTask(texture);
        }
    }

    private static void renderTextAndHealthBarOthers(LivingEntity renderTarget, float scaledX, float scaledY, float scaledWidth, float scaleValue) {
        float textX = scaledX + 38f * scaleValue, textY = scaledY + 5f * scaleValue;
        float healthBarX = scaledX + 4f * scaleValue, healthBarY = scaledY + (38f) * scaleValue;
        float healthBarWidth = (scaledWidth - 8f * scaleValue), healthBarHeight = 4f * scaleValue;
        float textAlpha = 0.9f * scaleValue, shadowAlpha = 0.1f * scaleValue;
        float nameTextSize = 18f * scaleValue;
        float healthTextSize = 14f * scaleValue;
        String name = renderTarget.getName().getString();

        FontManager.BoldPingFang.drawGlowString(nameTextSize, name, textX, textY, ColorPanel.createColorPanel(1f, 1f, 1f, textAlpha), ColorPanel.createColorPanel(0f, 0f, 0f, shadowAlpha), false, 5f);
        FontManager.BoldPingFang.drawGlowString(healthTextSize, (int) renderTarget.getHealth() + "",  scaledX + scaledWidth - (13f * scaleValue), textY, ColorUtils.colorToColorPanel(ClientSettings.color()), ColorUtils.colorToColorPanel(ClientSettings.color()).updateAlpha(shadowAlpha), false, 5f);


        float healthBarAlpha = 0.1f * scaleValue;
        RenderUtils.drawAppleRoundedRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight, ColorPanel.createColorPanel(0f, 0f, 0f, healthBarAlpha), 2f * scaleValue);

        float healthPercentage = renderTarget.getHealth() / renderTarget.getMaxHealth();
        float healthWidth = healthBarWidth * healthPercentage;

        animation1.animate(Math.max(healthBarHeight, healthWidth), 15);
        animation2.animate(Math.max(healthBarHeight, healthWidth), 30);

        float animatedHealth1 = Math.min(animation1.getOutput(), healthBarWidth);
        float animatedHealth2 = Math.min(animation2.getOutput(), healthBarWidth);

        float healthBarColorAlpha1 = 0.5f * scaleValue;
        float healthBarColorAlpha2 = 0.25f * scaleValue;

        RenderUtils.drawAppleRoundedRect(healthBarX, healthBarY, animatedHealth1, healthBarHeight, ColorPanel.createColorPanel(1f, 1f, 1f, healthBarColorAlpha1), 2f * scaleValue);
        RenderUtils.drawAppleRoundedRect(healthBarX, healthBarY, animatedHealth2, healthBarHeight, ColorPanel.createColorPanel(1f, 1f, 1f, healthBarColorAlpha2), 2f * scaleValue);
    }

}
