package missu.epsilon.client.utils.animations.basic.animation.editedAnimation;


import missu.epsilon.client.utils.animations.basic.animation.Animation;

public class EaseOutBackAnimation extends Animation {

    public EaseOutBackAnimation(int duration) {
        super(duration);
    }

    @Override
    public double getEquation(double value) {
        double c1 = 1.70158;
        double c3 = c1 + 1.0;

        return 1.0 + c3 * Math.pow(value - 1.0, 3.0) + c1 * Math.pow(value - 1.0, 2.0);
    }
    
}
