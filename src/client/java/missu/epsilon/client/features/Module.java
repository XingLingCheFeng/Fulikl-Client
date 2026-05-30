package missu.epsilon.client.features;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.visual.Notification;
import missu.epsilon.client.features.value.Value;
import missu.epsilon.client.features.value.impl.BindValue;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.ingameui.notification.NotificationManager;
import missu.epsilon.client.ingameui.notification.NotificationType;
import missu.epsilon.client.utils.Wrapper;
import missu.epsilon.client.utils.animations.Animation;
import missu.epsilon.client.utils.animations.basic.animation.editedAnimation.EaseFlyingAnimation;
import missu.epsilon.client.utils.animations.basic.animationinstance.AnimatingNumber;
import missu.epsilon.client.utils.animations.impl.EaseInOutQuad;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Module implements Wrapper {

    @Getter public String name;
    @Getter public String description;
    @Getter public final ModuleInfo moduleInfo;
    @Getter public ModuleCategory category;
    @Setter @Getter public EnumAutoDisableType enumAutoDisableType;
    public final BindValue bind = new BindValue("Key Bind", new String[]{"None", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"}, "None");
    public boolean show;
    public final BoolValue hide = new BoolValue("Hide", false);

    public Module() {
        this.moduleInfo = getClass().getAnnotation(ModuleInfo.class);
        this.name = moduleInfo.name();
        this.description = moduleInfo.description();
        this.category = moduleInfo.category();
        this.state = moduleInfo.defaultOn();
        this.enumAutoDisableType = moduleInfo.autoDisable();
        this.show = false;
        this.hide.set(moduleInfo.hide());
    }

    // Current state of module
    private boolean state;

    public boolean getState() {
        return state;
    }

    public boolean isHide() {
        return this.hide.get();
    }

    public void setHide(boolean hide) {
        this.hide.set(hide);
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void setState(boolean value) {
        if (state == value) return;
        // Call toggle
        onToggle();
        try {
            state = value;
            if (value) {
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f);
                }
                if (!name.equalsIgnoreCase("ClickGui") && Notification.showMode.get())
                    NotificationManager.post(NotificationType.SUCCESS, name + " Enabled");
                onEnable();
                Client.getInstance().getEventManager().subscribe(this);
            } else {
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.playSound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_OFF, 1f, 1f);
                }
                if (!name.equalsIgnoreCase("ClickGui") && Notification.showMode.get())
                    NotificationManager.post(NotificationType.FAILED, name + " Disabled");
                onDisable();
                Client.getInstance().getEventManager().unsubscribe(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggle() {
        setState(!state);
    }

    public boolean isEnabled() {
        return state;
    }

    public void setEnabled(boolean b) {
        setState(b);
    }

    public void onToggle() {
    }

    /**
     * Called when module enabled
     */
    public void onEnable() {
    }

    /**
     * Called when module disabled
     */
    public void onDisable() {
    }

    /**
     * Called when module initialized
     */
    public void onInitialize() {
    }

    /**
     * Get all values of module
     */
    public List<Value<?>> getValues() {
        List<Value<?>> classValues = getValues(this.getClass(), this);
        return Stream.concat(Stream.concat(Stream.of(bind), Stream.of(hide)), classValues.stream()).collect(Collectors.toList());
    }

    public static List<Value<?>> getValues(Class<?> clazz, Object instance) {
        List<Value<?>> values = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field valueField : fields) {
            try {
                valueField.setAccessible(true);
                Object fieldValue = valueField.get(instance);
                if (fieldValue instanceof Value) {
                    values.add((Value<?>) fieldValue);
                }
            } catch (IllegalAccessException ignored) {}
        }
        return values;
    }

    /**
     * Get module by valueName
     */
    public Value<?> getValue(String valueName) {
        return getValues().stream().filter(value -> value.getName().equalsIgnoreCase(valueName)).findFirst().orElse(null);
    }

    public final EaseFlyingAnimation mouseHoveredAnimation = new EaseFlyingAnimation(300);
    public final EaseFlyingAnimation activatedAnimation = new EaseFlyingAnimation(300);
    public final AnimatingNumber animatingNumber = new AnimatingNumber(0f);
    public final Animation moduleListAnimation = new EaseInOutQuad(200, 1);

}
