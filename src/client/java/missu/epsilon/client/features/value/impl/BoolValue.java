package missu.epsilon.client.features.value.impl;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.features.value.Value;
import missu.epsilon.client.utils.animations.basic.animation.editedAnimation.EaseOutBackAnimation;

@Getter
@Setter
public class BoolValue extends Value<Boolean> {

    public EaseOutBackAnimation animation;
    private boolean canChangValue;

    public BoolValue(String name, Boolean value) {
        super(name);
        this.animation = new EaseOutBackAnimation(300);
        this.setValue(value);
    }

        public BoolValue(String name) {
        super(name);
        this.animation = new EaseOutBackAnimation(300);
        this.setValue(false);
    }

}
