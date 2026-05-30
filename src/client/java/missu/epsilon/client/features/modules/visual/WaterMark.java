package missu.epsilon.client.features.modules.visual;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.sxmurxy.builders.Builder;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.BlurTaskInstance;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltBlur;
import missu.epsilon.client.utils.animations.basic.animationinstance.AnimatingNumber;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.entity.PlayerUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import missu.epsilon.client.utils.render.font.Icon;

import java.util.Objects;

@ModuleInfo(name = "WaterMark", description = "Title of the client", category = ModuleCategory.VISUAL)
public class WaterMark extends Module {

    public static ListValue textmode = new ListValue("Text Mode", new String[]{"Fps", "Time", "Userinfo", "Bps"}, "Fps");

    public AnimatingNumber subtitleWidth = new AnimatingNumber(0f);

    @SuppressWarnings("unused")
    @EventTarget
    public void onRenderScreen(RenderNvgEvent event) {
        float x = 5f, y = 5f, space = 3f;
        float smoothness = 5f;
        float height = 15f;
        String title = Client.getClientName();
        String subtitle = switch (textmode.get()) {
            case "Fps" -> mc.getCurrentFps() + " " + "fps";
            case "Time" -> ClientUtils.formatTime(System.currentTimeMillis());
            case "Userinfo" -> Client.username;
            case "Bps" -> PlayerUtils.getBPS(Objects.requireNonNull(mc.player)) + " " + "bps";
            default -> throw new IllegalStateException("Unexpected value: " + textmode.get());
        };
        String icontext = switch (textmode.get()) {
            case "Fps" -> "\uE8F4";
            case "Time" -> "\uE855";
            case "Userinfo" -> "\uE853";
            case "Bps" -> "\uE934";
            default -> throw new IllegalStateException("Unexpected value: " + textmode.get());
        };
        ColorPanel colorPanel = ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f);
        ColorPanel shadowColorPanel = ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f);

        float width = space * 2f;
        float titleWidth = FontManager.FilledMaterial.getStringWidth(Icon.HIVE, 20f) + space + FontManager.BoldPingFang.getStringWidth(title, 15f);
        width += titleWidth;
        BuiltBlur blur = Builder.blur().position(new PositionState(x - smoothness / 2f, y - smoothness / 2f)).size(new SizeState(width + smoothness, height + smoothness)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).radius(new QuadRadiusState(5f)).color(QuadColorState.TRANSPARENT).matrix4f(event.matrix4f()).build();
        BlurTaskInstance.addTask(blur);
        RenderUtils.drawAppleRoundedRect(x, y, width, height, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
        x += space;
        y += space + 0.5f;
        FontManager.FilledMaterial.drawGlowString(20f, Icon.HIVE, x, y, colorPanel, shadowColorPanel, false, 5f);
        x += FontManager.FilledMaterial.getStringWidth(Icon.HIVE, 20f) + space;
        y += 1f;
        FontManager.BoldPingFang.drawGlowString(15f, title, x, y, colorPanel, shadowColorPanel, false, 5f);

        x = 5f + width + 5f;
        y = 5f;
        float subtitleWidth = space * 2f;
        subtitleWidth += FontManager.FilledMaterial.getStringWidth(icontext, 20f) + space + FontManager.BoldPingFang.getStringWidth(subtitle, 15f);
        this.subtitleWidth.update(subtitleWidth);
        this.subtitleWidth.animate();
        BuiltBlur subblur = Builder.blur().position(new PositionState(x - smoothness / 2f, y - smoothness / 2f)).size(new SizeState(this.subtitleWidth.animatingNumber + smoothness, height + smoothness)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).radius(new QuadRadiusState(5f)).color(QuadColorState.TRANSPARENT).matrix4f(event.matrix4f()).build();
        BlurTaskInstance.addTask(subblur);
        RenderUtils.drawAppleRoundedRect(x, y, this.subtitleWidth.animatingNumber, height, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
        x += space;
        y += space + 0.5f;
        FontManager.FilledMaterial.drawGlowString(20f, icontext, x, y, colorPanel, shadowColorPanel, false, 5f);
        x += FontManager.FilledMaterial.getStringWidth(icontext, 20f) + space;
        y += 1f;
        FontManager.BoldPingFang.drawGlowString(15f, subtitle, x, y, colorPanel, shadowColorPanel, false, 5f);
    }

}
