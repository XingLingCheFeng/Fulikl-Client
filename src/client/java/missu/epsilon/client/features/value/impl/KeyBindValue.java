package missu.epsilon.client.features.value.impl;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.features.value.Value;

@Getter
@Setter
public class KeyBindValue extends Value<Integer> {

    public int key;

    public KeyBindValue(String name, int key) {
        super(name);
        this.key = key;
    }

}
