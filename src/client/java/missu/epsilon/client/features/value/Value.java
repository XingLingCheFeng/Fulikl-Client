package missu.epsilon.client.features.value;

import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.utils.animations.basic.animation.editedAnimation.EaseFlyingAnimation;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Value<V> {

    public Dependency dependency;
    public String name;
    public EaseFlyingAnimation availableAnimation = new EaseFlyingAnimation(300);
    private final List<ValueListener<V>> valueChangeListeners = new ArrayList<>();

    public void set(V value) {
        V oldValue = this.value;
        this.value = value;

        if (oldValue != value)
            valueChangeListeners.forEach(listener -> listener.onValueChange(oldValue, value));

    }

    public V get() {
        return value;
    }

    public V value;

    public void addValueChangeListener(ValueListener<V> valueChangeListener) {
        valueChangeListeners.add(valueChangeListener);
    }

    public Value(String name, Dependency dependency) {
        this.name = name;
        this.dependency = dependency;
    }

    public Value(String name) {
        this.name = name;
        this.dependency = () -> true;
    }


    public <T> T displayable(Dependency func) {
        this.dependency = func;
        return (T) this;
    }

    public boolean isHidden() {
        return !this.isAvailable();
    }

    public boolean isAvailable() {
        return this.dependency != null && this.dependency.check();
    }

    @FunctionalInterface
    public interface Dependency {
        boolean check();
    }

}
