package missu.epsilon.client.utils.animations.basic.animation;




public abstract class Animation {

    public AnimationTimeCounter animationTimeCounter;

    public AnimationState animationState;

    public int duration;

    public Animation(int duration) {
        this.animationTimeCounter = new AnimationTimeCounter();

        this.duration = duration;

        this.update(AnimationState.BACKWARDS);
    }

    public float value() {
        if (animationState == AnimationState.FORWARDS) {
            if (animationTimeCounter.elapsedTime(this.duration)) {
                return 1f;
            }

            return (float) (getEquation(animationTimeCounter.getTime() / (double) duration));
        }

        if (animationState == AnimationState.BACKWARDS) {
            if (animationTimeCounter.elapsedTime(this.duration)) {
                return 0f;
            }

            return (float) (1f - this.getEquation(animationTimeCounter.getTime() / (double) duration));
        }

        return 0f;
    }

    public void update(AnimationState animationState) {
        if (this.animationState == animationState) {
            return;
        }

        this.animationState = animationState;

        animationTimeCounter.setTime(System.currentTimeMillis() - (duration - Math.min(duration, animationTimeCounter.getTime())));
    }

    public boolean finish(AnimationState animationState) {
        return animationTimeCounter.elapsedTime(this.duration) && this.animationState == animationState;
    }

    public abstract double getEquation(double equation);

}
