package missu.epsilon.client.utils.render;

public interface ViewBobbingStorage {
    // Bobbing Tilt
    void setBobbingTilt(float bobbingTilt);

    float getBobbingTilt();

    float getPreviousBobbingTilt();


    float getHorizontalSpeed();

    float getPreviousHorizontalSpeed();
}
