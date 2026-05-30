package missu.epsilon.client.features.value.impl;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.features.value.Value;
import missu.epsilon.client.utils.miscs.TimerUtils;

@Setter
@Getter
public class TextValue extends Value<String> {

    private TimerUtils timer;

    public TextValue(String name, String value) {
        super(name);
        this.timer = new TimerUtils();
        this.setValue(value);
    }

}
