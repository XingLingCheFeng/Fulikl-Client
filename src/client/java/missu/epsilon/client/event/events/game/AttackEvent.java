package missu.epsilon.client.event.events.game;

import lombok.Getter;
import missu.epsilon.client.event.impl.CancellableEvent;
import net.minecraft.entity.Entity;

@Getter
public class AttackEvent extends CancellableEvent {

    private final Entity attackedEntity;

    public AttackEvent(Entity entity) {
        this.attackedEntity = entity;
    }

}
