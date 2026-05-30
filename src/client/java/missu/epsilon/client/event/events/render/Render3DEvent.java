package missu.epsilon.client.event.events.render;

import lombok.Getter;
import missu.epsilon.client.event.impl.Event;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

@Getter
public class Render3DEvent implements Event {

    private final MatrixStack matrixStack;
    private final RenderTickCounter tickCounter;
    public final Matrix4f projectionMatrix;

    public Render3DEvent(MatrixStack matrixStack, RenderTickCounter renderTickCounter,Matrix4f projectionMatrix) {
        this.matrixStack = matrixStack;
        this.tickCounter = renderTickCounter;
        this.projectionMatrix = projectionMatrix;
    }

}
