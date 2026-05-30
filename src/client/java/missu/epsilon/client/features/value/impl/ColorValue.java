package missu.epsilon.client.features.value.impl;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.features.value.Value;

import java.awt.*;

@Getter
@Setter
public class ColorValue extends Value<Integer> {

    public ColorValue(String name, int color) {
        super(name);
        this.setValue(color);
    }

    public Color getColor() {
        return new Color(this.getValue(), true); // 确保支持透明度
    }

    public int getColorAsInt() {
        return this.getValue();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

}
