package missu.epsilon.client.event.events.render;

import missu.epsilon.client.event.impl.CancellableEvent;
import net.minecraft.entity.Entity;

public class RenderNameTagsEvent extends CancellableEvent {

    public Entity entity;

    public RenderNameTagsEvent(Entity entity) {
        this.entity = entity;
    }

}