package missu.epsilon.client.ingameui.notification;

import lombok.Getter;
import lombok.SneakyThrows;
import missu.epsilon.client.utils.animations.Animation;
import missu.epsilon.client.utils.animations.impl.EaseOutSine;
import missu.epsilon.client.utils.miscs.TimerUtils;
import missu.epsilon.client.utils.render.font.FontManager;


@Getter
public class Notification {
    private final NotificationType notificationType;
    private final String description;
    private final float time;
    private final TimerUtils timerUtils;
    private final Animation animation;

    public Notification(NotificationType type, String description) {
        this(type, description, NotificationManager.getToggleTime());
    }

    public Notification(NotificationType type, String description, float time) {
        this.description = description;
        this.time = (long) (time * 1000);
        timerUtils = new TimerUtils();
        this.notificationType = type;
        this.animation = new EaseOutSine(250, 1);
    }

    @SneakyThrows
    public double getWidth() {
        return FontManager.BoldPingFang.getStringWidth(getDescription(), 18) + 17;
    }

    public double getHeight() {
        return 17;
    }
}