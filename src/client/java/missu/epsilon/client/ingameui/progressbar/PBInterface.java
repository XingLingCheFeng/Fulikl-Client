package missu.epsilon.client.ingameui.progressbar;

import missu.epsilon.client.event.events.render.RenderNvgEvent;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public interface PBInterface {
    default void render(Matrix4f matrix4f, DrawContext context, boolean nvg){}
    default boolean shouldRender() { return false; }
}