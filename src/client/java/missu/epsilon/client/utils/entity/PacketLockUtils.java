package missu.epsilon.client.utils.entity;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.Priorities;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.impl.EventTarget;

import java.util.concurrent.atomic.AtomicBoolean;

public class PacketLockUtils {
    private static final AtomicBoolean attackLock = new AtomicBoolean(false);
    private static final AtomicBoolean swingLock = new AtomicBoolean(false);

    public static void register(){
        Client.getInstance().getEventManager().subscribe(new PacketLockUtils());
    }

    public static boolean attackAndLock() {
        return attackLock.compareAndSet(false, true);
    }

    public static boolean swingAndLock() {
        return swingLock.compareAndSet(false, true);
    }

    @EventTarget(Priorities.VERY_HIGH)
    public void onTick(TickEvent event){
        reset();
    }

    public static void reset() {
        attackLock.set(false);
        swingLock.set(false);
    }

}
