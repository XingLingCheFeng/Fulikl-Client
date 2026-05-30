package missu.epsilon.client.features.value.impl;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.features.value.Value;
import missu.epsilon.client.utils.animations.basic.animation.editedAnimation.EaseFlyingAnimation;
import missu.epsilon.client.utils.animations.basic.animationinstance.AnimatingNumber;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ListValue extends Value<String> {

    private final String[] modes;
    private boolean isUnfold;

    public List<EaseFlyingAnimation> activatedAnimations = new ArrayList<>();
    public EaseFlyingAnimation silentAnimation = new EaseFlyingAnimation(300);
    public AnimatingNumber animatingNumberX = new AnimatingNumber(0f);
    public AnimatingNumber animatingNumberY = new AnimatingNumber(0f);
    public AnimatingNumber animatingNumberWidth = new AnimatingNumber(0f);

    public ListValue(String name, String[] modes, String value) {
        super(name);
        this.modes = modes;
        this.setValue(value);

        for (int i = 0; i < modes.length; i++) {
            activatedAnimations.add(new EaseFlyingAnimation(300));
        }
    }

    public ListValue(String name, String[] modes) {
        super(name);
        this.modes = modes;
        this.setValue(modes[0]);

        for (int i = 0; i < modes.length; i++) {
            activatedAnimations.add(new EaseFlyingAnimation(300));
        }
    }

    public boolean is(String mode) {
        return this.getValue().equalsIgnoreCase(mode);
    }

    public void setMode(String mode) {
        boolean hasValue = false;

        for (String modes : this.modes) {
            if (modes.equalsIgnoreCase(mode)) {
                this.setValue(modes);
                hasValue = true;
            }
        }

        if (!hasValue) {
            this.setValue(this.modes[0]);
        }
    }
}
