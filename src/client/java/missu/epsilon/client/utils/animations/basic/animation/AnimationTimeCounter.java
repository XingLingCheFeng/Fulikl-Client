package missu.epsilon.client.utils.animations.basic.animation;




public class AnimationTimeCounter {

    public long lastMS;

    public AnimationTimeCounter() {
        lastMS = System.currentTimeMillis();
    }

    public boolean elapsedTime(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public void setTime(long time) {
        lastMS = time;
    }

}
