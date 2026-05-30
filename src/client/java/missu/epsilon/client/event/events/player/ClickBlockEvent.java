package missu.epsilon.client.event.events.player;

import missu.epsilon.client.event.impl.CancellableEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ClickBlockEvent extends CancellableEvent {

    public BlockPos clickedBlock;
    public Direction direction;

    public ClickBlockEvent(BlockPos clickedBlock,Direction direction) {
        this.clickedBlock = clickedBlock;
        this.direction = direction;
    }

}
