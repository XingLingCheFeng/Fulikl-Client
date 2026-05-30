package missu.epsilon.client.features.value;

@FunctionalInterface
public interface ValueListener<T> {
    void onValueChange(T oldValue, T value);
}
