package missu.epsilon.client.ingameui;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.ingameui.clickgui.MouseBehavior;

@Getter
@Setter
public class Dragging {
    @Expose
    @SerializedName("x")
    private float xPos;
    @Expose
    @SerializedName("y")
    private float yPos;

    public float initialXVal;
    public float initialYVal;

    private float startX, startY;
    private boolean dragging;

    private float width, height;

    @Expose
    @SerializedName("name")
    private String name;

    private final Module module;

    public Dragging(Module module, String name, float initialXVal, float initialYVal) {
        this.module = module;
        this.name = name;
        this.xPos = initialXVal;
        this.yPos = initialYVal;
        this.initialXVal = initialXVal;
        this.initialYVal = initialYVal;
    }

    public float getX() {
        return xPos;
    }

    public void setX(float x) {
        this.xPos = x;
    }

    public float getY() {
        return yPos;
    }

    public void setY(float y) {
        this.yPos = y;
    }

    public final void onDraw(int mouseX, int mouseY) {
        if (dragging) {
            xPos = mouseX - startX;
            yPos = mouseY - startY;
        }
    }

    public final void onClick(int mouseX, int mouseY, int button) {
        boolean canDrag = MouseBehavior.mouseHoveredFullScreen(xPos, yPos, width, height, mouseX, mouseY);
        if (button == 0 && canDrag) {
            dragging = true;
            startX = mouseX - xPos;
            startY = mouseY - yPos;
        }
    }

    public final void onRelease(int button) {
        if (button == 0) dragging = false;
    }
}
