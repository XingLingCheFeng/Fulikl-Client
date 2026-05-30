package missu.epsilon.client.features.value.impl;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.features.value.Value;
import missu.epsilon.client.utils.animations.basic.animation.editedAnimation.EaseFlyingAnimation;
import missu.epsilon.client.utils.animations.basic.animationinstance.AnimatingNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MultiBoolValue extends Value<String> {

    public final BoolValue[] modes;
    private final Map<String, BoolValue> map = new HashMap<>();
    private boolean isUnfold;
    public List<EaseFlyingAnimation> activatedAnimations = new ArrayList<>();
    public EaseFlyingAnimation silentAnimation = new EaseFlyingAnimation(300);
    public AnimatingNumber animatingNumberX = new AnimatingNumber(0f);
    public AnimatingNumber animatingNumberY = new AnimatingNumber(0f);
    public AnimatingNumber animatingNumberWidth = new AnimatingNumber(0f);

    public MultiBoolValue(String name, BoolValue[] modes) {
        super(name);
        this.modes = modes;
        this.setValue(value);

        for (BoolValue mode : modes) {
            map.put(mode.getName(), mode);
            activatedAnimations.add(new EaseFlyingAnimation(300));
        }
    }

    public boolean getValue(String values) {
        return map.get(values).get();
    }

    public boolean get(String values) {
        return getValue(values);
    }

    public void setValue(String valuen, Boolean value) {
        if (map.containsKey(valuen)) {
            map.get(valuen).set(value);
        }
    }

    public void set(String valuen, Boolean value) {
        setValue(valuen, value);
    }

}
