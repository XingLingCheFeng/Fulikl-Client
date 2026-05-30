package missu.epsilon.client.utils.animations.basic.animation.frameAnimation;


public class FrameAnimation {

    public static Float animationSpeed = 1.0f;

    public static Float deltaTickCounter = 20.0f;

    public static float frameAnimation(boolean state, float currentTarget, float onStateTarget, float offStateTarget) {
        if (Float.isNaN(currentTarget) || Float.isNaN(onStateTarget) || Float.isNaN(offStateTarget)) {
            currentTarget = onStateTarget = offStateTarget = 0f;

            return currentTarget + onStateTarget + offStateTarget;
        }

        boolean larger;
        float difference;

        if (state) {
            larger = onStateTarget > currentTarget;

            difference = Math.max(onStateTarget, currentTarget) - Math.min(onStateTarget, currentTarget);
            float factor = difference * (animationSpeed / deltaTickCounter);

            currentTarget = larger ? currentTarget + factor : currentTarget - factor;

            if (Math.abs(currentTarget - onStateTarget) < 0.1f) {
                currentTarget = onStateTarget;
            }

        } else {
            larger = offStateTarget > currentTarget;

            difference = Math.max(offStateTarget, currentTarget) - Math.min(offStateTarget, currentTarget);
            float factor = difference * (animationSpeed / deltaTickCounter);

            currentTarget = larger ? currentTarget + factor : currentTarget - factor;

            if (Math.abs(currentTarget - offStateTarget) < 0.1f) {
                currentTarget = offStateTarget;
            }

        }

        return currentTarget;
    }

}
