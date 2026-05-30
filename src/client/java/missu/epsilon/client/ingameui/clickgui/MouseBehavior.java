package missu.epsilon.client.ingameui.clickgui;

import static missu.epsilon.client.utils.Wrapper.mc;

public class MouseBehavior {

    public static boolean leftButtonClicked, rightButtonClicked;

    public static double mouseX, mouseY, mouseScrollValue;

    public static boolean mouseHoveredClickGUI(float x, float y, float width, float height) {
        float boxWidth = 450f, boxHeight = 262f;
        float boxX = (mc.getWindow().getScaledWidth() / 2f) - (boxWidth / 2f), boxY = (mc.getWindow().getScaledHeight() / 2f) - (boxHeight / 2f);

        return (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height) && (mouseX > boxX && mouseX < boxX + boxWidth && mouseY > boxY && mouseY < boxY + boxHeight);
    }

    public static boolean mouseHoveredFullScreen(float x, float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static boolean mouseHoveredFullScreen(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public static boolean mouseLeftClicked() {
        return mc.mouse.wasLeftButtonClicked();
    }

    public static boolean mouseRightClicked() {
        return mc.mouse.wasRightButtonClicked();
    }

    public static void updatePosition(float x, float y) {
        mc.mouse.unlockCursor();

        mouseX = x;
        mouseY = y;
    }

    public static void resetState() {
        mc.mouse.leftButtonClicked = false;
        mc.mouse.rightButtonClicked = false;

        leftButtonClicked = rightButtonClicked = false;

        mouseScrollValue = 0f;
    }
}