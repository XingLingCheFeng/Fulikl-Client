package missu.epsilon.client.event.events.render;

import missu.epsilon.client.event.impl.Event;
import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix4f;

public record Render2DEvent(DrawContext drawContext, Matrix4f matrix4f) implements Event { }