package missu.epsilon.client.utils.animations.basic.animationinstance;


import missu.epsilon.client.utils.animations.basic.animation.frameAnimation.FrameAnimationCustomSpeed;

public class AnimatingNumber {

    public float number;
    public float animatingNumber;
    public float speed;

    public AnimatingNumber(float number) {
        this.number = number;
        this.animatingNumber = number;
        this.speed = 1F;
    }

    public AnimatingNumber(float number,float speed) {
        this.number = number;
        this.animatingNumber = number;
        this.speed = speed;
    }

    public void animate() {
        this.animatingNumber = FrameAnimationCustomSpeed.frameAnimation(true, animatingNumber, number, 0f,speed);
    }

    public void update(float number) {
        this.number = number;
    }

}
