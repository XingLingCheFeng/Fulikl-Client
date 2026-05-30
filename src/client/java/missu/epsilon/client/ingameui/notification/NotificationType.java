package missu.epsilon.client.ingameui.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import missu.epsilon.client.utils.render.font.Icon;

import java.awt.*;

@Getter
@AllArgsConstructor
public enum NotificationType {
    SUCCESS(Icon.CHECK_CIRCLE, new Color(65, 252, 65, 200)),
    NOTIFY(Icon.INFO, new Color(127, 174, 210, 200)),
    WARRING(Icon.WARRING, new Color(255, 255, 94, 200)),
    FAILED(Icon.CANCEL, new Color(226, 87, 76, 200)),
    HEALTH_WARN(Icon.HEART_BROKEN, new Color(221, 72, 30, 200)),
    AUTO_PLAY(Icon.AUTOPLAY, new Color(221, 72, 30, 200)),
    DANGER(Icon.CRISIS_ALERT, new Color(255, 32, 36, 200)),
    AntiBot(Icon.ROBOT, new Color(255, 255, 94, 200));
    private final String name;
    private final Color color;
}