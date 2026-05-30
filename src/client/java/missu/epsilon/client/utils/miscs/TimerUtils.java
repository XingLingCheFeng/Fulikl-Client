package missu.epsilon.client.utils.miscs;

public class TimerUtils {
    public long lastMS = System.currentTimeMillis();


    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset) reset();
            return true;
        }

        return false;
    }

    public boolean hasTimeElapsed(float time, boolean reset) {
        if (System.currentTimeMillis() - this.lastMS >= time) {
            if (reset) {
                this.reset();
            }
            return true;
        }
        return false;
    }

    public boolean finished(long delay) {
        return System.currentTimeMillis() - delay >= lastMS;
    }

    public boolean hasTimeElapsed(double time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public void setTime(long time) {
        lastMS = time;
    }

    public void waitForAtLeast(long ms) {
        this.lastMS = Math.max(this.lastMS, System.currentTimeMillis() + ms);
    }
}
