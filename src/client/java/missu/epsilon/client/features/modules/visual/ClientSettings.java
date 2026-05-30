package missu.epsilon.client.features.modules.visual;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.events.render.Render2DEvent;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.*;
import missu.epsilon.client.ingameui.notification.NotificationManager;
import missu.epsilon.client.ingameui.notification.NotificationType;
import missu.epsilon.client.utils.render.ColorUtils;

import java.awt.*;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "ClientSettings", category = ModuleCategory.VISUAL, defaultOn = true)
public class ClientSettings extends Module {
    public static TextValue clientName = new TextValue("Client Name ", Client.CLIENT_NAME);
    public static TextValue scoreboard = new TextValue("Score Board Nick ", "萝莉岛");
    public static BoolValue autoDis = new BoolValue("Auto Disable Mode", true);
    public static BoolValue hideBossBar = new BoolValue("Hide Boss Bar", true);
    public static BoolValue hidePotion = new BoolValue("Hide Potion HUD", true);
    public static final ListValue colorMode = new ListValue("Color Mode", new String[]{"Fade", "Static", "Rainbow", "Dynamic"}, "Dynamic");
    public static final NumberValue rainbowSaturation = (NumberValue) new NumberValue("Rainbow Saturation", 0.5, 0, 1, 0.1).displayable(() -> colorMode.is("Rainbow"));
    public static final NumberValue rainbowBrightness = (NumberValue) new NumberValue("Rainbow Brightness", 0.9, 0, 1, 0.1).displayable(() -> colorMode.is("Rainbow"));
    public static ColorValue firstColor = (ColorValue) new ColorValue("First Color", new Color(255, 255, 255, 120).getRGB()).displayable(() -> !colorMode.is("Rainbow"));
    public static ColorValue secondColor = (ColorValue) new ColorValue("Second Color", new Color(128, 128, 128, 120).getRGB()).displayable(() -> colorMode.is("Dynamic"));
    boolean noti = false;

    @EventTarget
    public void Render(RenderNvgEvent event) {
        Client.pbManager.render(event.matrix4f(), event.drawContext(), true);
    }

    @EventTarget
    public void RenderMc(Render2DEvent event) {
        Client.pbManager.render(event.matrix4f(), event.drawContext(), false);
    }

    @Override
    public void onDisable() {
        NotificationManager.post(NotificationType.DANGER, "You can't disable this module");
        setEnabled(true);
    }

    @EventTarget
    public void OnUpdate(UpdateEvent event) {
        if (mc.player != null && mc.player.getHealth() <= 6) {
            if (!noti) {
                NotificationManager.post(NotificationType.HEALTH_WARN, "Low Health");
                noti = true;
            }
        }
    }

    public static Color color() {
        return color(0);
    }

    public static Color color(int tick) {
        return switch (colorMode.getValue()) {
            case "Rainbow" ->
                    ColorUtils.getRainbow(tick, rainbowSaturation.getValue().floatValue(), rainbowBrightness.getValue().floatValue());
            case "Fade" -> ColorUtils.fade(5, tick * 20, new Color(firstColor.getColorAsInt()), 1);
            case "Static" -> firstColor.getColor();
            case "Dynamic" -> {
                tick *= 100;
                yield new Color(ColorUtils.colorSwitch(firstColor.getColor(), secondColor.getColor(), 2000, -tick / 40, 75, 2, 255));
            }

            default -> throw new IllegalStateException("Unexpected value: " + colorMode.getValue());
        };
    }
}
