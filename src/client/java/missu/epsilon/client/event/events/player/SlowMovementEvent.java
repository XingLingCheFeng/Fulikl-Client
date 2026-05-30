package missu.epsilon.client.event.events.player;

import missu.epsilon.client.event.impl.CancellableEvent;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;

public class SlowMovementEvent extends CancellableEvent {

    public BlockState state;
    public Vec3d movementMultiplier;

    public SlowMovementEvent(BlockState state,Vec3d movementMultiplier) {
        this.state = state;
        this.movementMultiplier = movementMultiplier;
    }

}
