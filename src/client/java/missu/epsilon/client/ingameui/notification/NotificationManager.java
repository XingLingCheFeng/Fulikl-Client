package missu.epsilon.client.ingameui.notification;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import lombok.SneakyThrows;
import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.visual.PostProcessing;
import missu.epsilon.client.sxmurxy.builders.Builder;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.BlurTaskInstance;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltBlur;
import missu.epsilon.client.utils.animations.Animation;
import missu.epsilon.client.utils.animations.Direction;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.render.ColorUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import net.minecraft.client.util.Window;
import org.joml.Matrix4f;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;


public class NotificationManager {
    @Getter
    private static final Deque<Notification> notifications = new ConcurrentLinkedDeque<>();
    @Getter
    private static final float toggleTime = 2;

    @SneakyThrows
    public static void post(NotificationType type, String description) {
        post(new Notification(type, description));
    }

    @SneakyThrows
    public static void post(NotificationType type, String description, float time) {
        post(new Notification(type, description, time));
    }

    @SneakyThrows
    public static void post(Notification notification) {
        if (Client.moduleManager.getModule(missu.epsilon.client.features.modules.visual.Notification.class).isEnabled()) {
            notifications.add(notification);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @SneakyThrows
    public static void publish(Window sr, Matrix4f matrix4f) {
        if (ClientUtils.isNull()) return;
        float yOffset = 5f;
        RenderSystem.getModelViewStack().pushMatrix();
        for (Notification notification : getNotifications()) {
            float width = (float) notification.getWidth();
            float height = (float) notification.getHeight();

            Animation animation = notification.getAnimation();
            animation.setDirection(notification.getTimerUtils().hasTimeElapsed((long) notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);

            if (animation.finished(Direction.BACKWARDS)) {
                getNotifications().remove(notification);
            }

            if (!animation.finished(Direction.BACKWARDS)) {
                int scaledWidth = sr.getScaledWidth();
                float smoothness = 5f;
                BuiltBlur blur = Builder.blur().size(new SizeState(width + smoothness, height + smoothness)).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(5f).color(QuadColorState.TRANSPARENT).position(new PositionState((float) (scaledWidth - width - 5f + (width + 5f) * (1f - animation.getOutput())) - smoothness / 2f, yOffset - smoothness / 2f)).matrix4f(matrix4f).build();
                BlurTaskInstance.addTask(blur);

                RenderUtils.drawAppleRoundedRect((float) (scaledWidth - width - 5f + (width + 5f) * (1 - animation.getOutput())), yOffset, width, height, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f * animation.getOutput().floatValue()), 5f);
                RenderUtils.drawAppleRoundedRect((float) (scaledWidth - width - 5f + (width + 5f) * (1 - animation.getOutput())) + width - height, yOffset, height, height, ColorUtils.colorToColorPanel(notification.getNotificationType().getColor()).mulAlpha(animation.getOutput().floatValue()), 5f);

                FontManager.FilledMaterial.drawGlowString(22f, notification.getNotificationType().getName(), (float) (scaledWidth - width - 5f + (width + 5f) * (1f - animation.getOutput())) + width - 14f, 4f + yOffset, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f * animation.getOutput().floatValue()), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f * animation.getOutput().floatValue()), false, 5f);
                FontManager.BoldPingFang.drawGlowString(16f, notification.getDescription(), (float) (scaledWidth - width - 5f + (width + 5f) * (1f - animation.getOutput())) + 4f, 5f + yOffset, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f * animation.getOutput().floatValue()), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f * animation.getOutput().floatValue()), false, 5f);

                yOffset += (float) (20f * animation.getOutput());
            }
        }
        RenderSystem.getModelViewStack().popMatrix();
    }
}