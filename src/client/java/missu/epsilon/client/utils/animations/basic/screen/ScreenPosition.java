package missu.epsilon.client.utils.animations.basic.screen;




public class ScreenPosition {

    public double screenX, screenY, screenZ;

    public boolean viewable;

    public ScreenPosition(double screenX, double screenY, double screenZ, boolean viewable) {
        this.screenX = screenX;
        this.screenY = screenY;
        this.screenZ = screenZ;

        this.viewable = viewable;
    }

}
