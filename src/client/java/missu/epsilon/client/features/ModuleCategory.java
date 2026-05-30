package missu.epsilon.client.features;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.utils.animations.basic.animation.editedAnimation.EaseFlyingAnimation;
import missu.epsilon.client.utils.animations.basic.animationinstance.AnimatingNumber;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.render.font.Icon;
import net.minecraft.util.Formatting;

@Getter
public enum ModuleCategory {

    COMBAT("Combat", "Combat", Icon.SWORDS, new ColorPanel(0.9f, 0.5f, 0.6f, 1f), Formatting.RED),
    PLAYER("Player", "Player", "\uE7FD", new ColorPanel(0.9f, 0.8f, 0.9f, 1f), Formatting.LIGHT_PURPLE),
    MOVEMENT("Movement", "Movement", Icon.DIRECTIONS_WALK, new ColorPanel(0.4f, 0.6f, 0.8f, 1f), Formatting.BLUE),
    RENDER("Render", "Render", Icon.VISIBILITY, new ColorPanel(1.0f, 0.7f, 0.6f, 1f), Formatting.DARK_PURPLE),
    VISUAL("Visual", "Visual", Icon.FORMAT_LIST_BULLETED, new ColorPanel(0.8f, 0.9f, 1.0f, 1f), Formatting.GOLD),
    WORLD("World", "World", Icon.LANGUAGE, new ColorPanel(0.8f, 0.9f, 1.0f, 1f), Formatting.DARK_AQUA),
    EXPLOIT("Exploit", "Exploit", "\uE869", new ColorPanel(0.8f, 0.9f, 1.0f, 1f), Formatting.GREEN);

    private final String displayName;
    private final String configName;
    private final String htmlIcon;
    private final ColorPanel color;
    private final Formatting formatting;
    public final EaseFlyingAnimation switchedAnimation = new EaseFlyingAnimation(300);
    public final AnimatingNumber animatingNumber = new AnimatingNumber(0f);
    public final EaseFlyingAnimation switchedModSettingPageAnimation = new EaseFlyingAnimation(300);
    public final EaseFlyingAnimation mouseHoveredAnimation = new EaseFlyingAnimation(300);
    public Module currentModule;

    ModuleCategory(String displayName, String configName, String htmlIcon, ColorPanel color, Formatting formatting) {
        this.displayName = displayName;
        this.configName = configName;
        this.htmlIcon = htmlIcon;
        this.color = color;
        this.formatting = formatting;
    }

}
