package missu.epsilon.client.utils.entity;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.AttackEvent;
import missu.epsilon.client.event.events.game.ReceiveMessageEvent;
import missu.epsilon.client.event.events.game.EntityKilledEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.utils.Wrapper;
import missu.epsilon.client.utils.miscs.TimerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class CombatUtils implements Wrapper {
    public static TimerUtils lastAttackTimer = new TimerUtils();
    public static boolean inCombat = false;
    public static LivingEntity target = null;

    private static final List<LivingEntity> attackedEntityList = new CopyOnWriteArrayList<>();

    public static void register() {
        Client.getInstance().getEventManager().subscribe(new CombatUtils());
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null) return;

        for (LivingEntity entity : new ArrayList<>(attackedEntityList)) {
            if (entity.isDead() || entity.isSpectator()) {
                attackedEntityList.remove(entity);
            }
        }

        inCombat = false;

        if (!lastAttackTimer.hasTimeElapsed(250L)) {
            inCombat = true;
            return;
        }

        if (target != null) {
            if (mc.player.distanceTo(target) > 7 || target.isDead() || target.isSpectator()) {
                target = null;
            } else {
                inCombat = true;
            }
        }
    }

    @EventTarget
    public void onReceiveMessage(ReceiveMessageEvent e) {
        String msg = e.getMessage().getString();
        if (msg == null || mc.player == null) {
            return;
        }
        if (msg.contains("fell into the void") || msg.contains("by " + mc.player.getName())) {
            String[] parts = msg.split(" ");
            if (parts.length > 0) {
                String playerName = parts[0];
                Client.getInstance().getEventManager().call(new EntityKilledEvent(playerName));
            }
        }
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        Entity attacked = event.getAttackedEntity();

        if (attacked instanceof LivingEntity && Client.targetManager.isTarget(attacked,false)) {
            target = (LivingEntity) attacked;

            if (!attackedEntityList.contains(target)) {
                attackedEntityList.add(target);
            }

            lastAttackTimer.reset();
        }
    }
}
