package missu.epsilon.client.features.value.impl;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.features.value.Value;
import missu.epsilon.client.utils.animations.basic.animationinstance.AnimatingNumber;

@Getter
@Setter
public class NumberValue extends Value<Double> {

    private float animatedLength;
    private float sliderSize;
    private boolean sliding;
    private double max;
    private double min;
    private double inc;
    public AnimatingNumber animatingNumber;

    public NumberValue(String name, double value, double min, double max, double inc) {
        super(name);
        this.setValue(value);
        this.max = max;
        this.min = min;
        this.inc = inc;
        this.animatingNumber = new AnimatingNumber(0f);
    }

    public NumberValue(String name, double value, double min, double max) {
        super(name);
        this.setValue(value);
        this.max = max;
        this.min = min;
        this.inc = 1;
        this.animatingNumber = new AnimatingNumber(0f);
    }

    @Override
    public Double getValue() {
        double value = super.getValue();
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public Double get() {
        double value = super.getValue();
        return Math.round(value * 100.0) / 100.0;
    }

}
