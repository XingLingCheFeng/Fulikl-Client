package missu.epsilon.client.features.modules.world;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.player.AntiBot;
import missu.epsilon.client.features.modules.player.Teams;
import missu.epsilon.client.ingameui.notification.Notification;
import missu.epsilon.client.ingameui.notification.NotificationManager;
import missu.epsilon.client.ingameui.notification.NotificationType;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.client.ClientUtils;
import net.minecraft.entity.player.PlayerEntity;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static missu.epsilon.client.utils.Wrapper.mc;
import static missu.epsilon.client.utils.client.VecCalculation.getDistanceToEntityBox;

@ModuleInfo(name = "PlayerTracker", category = ModuleCategory.WORLD, description = "Remind you if a player is closing to you")
public class PlayerTracker extends Module {

    private final List<PlayerEntity> targets = new CopyOnWriteArrayList<>();

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (ClientUtils.isNull())
            return;
        targets.removeIf(e -> {
            if (e.isDead()) return true;

            boolean stillExists = mc.world.getEntityById(e.getId()) != null;
            if (!stillExists) return true;

            return getDistanceToEntityBox(mc.player, e) > 10F;
        });

        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity != mc.player && !entity.isDead()) {
                if (Teams.isInYourTeam(entity) && Client.moduleManager.getModule(Teams.class).getState()) return;
                AntiBot antiBot = Client.moduleManager.getModule(AntiBot.class);
                if (antiBot != null && antiBot.isEnabled() && AntiBot.isBot(entity)) return;
                if (entity.getUuid() == null) {
                    return;
                }

                if (entity == mc.player && mc.options.getPerspective().isFirstPerson()) {
                    return;
                }

                if (entity.getDisplayName() == null) {
                    return;
                }

                if (Objects.requireNonNull(entity.getDisplayName()).getString().contains("[NPC]")) {
                    return;
                }
                if (Objects.requireNonNull(entity.getDisplayName()).getString().contains("CIT-")) {
                    return;
                }
                double dist = getDistanceToEntityBox(mc.player, entity);

                if (dist <= 10F) {
                    if (!targets.contains(entity)) {
                        targets.add(entity);
                        NotificationManager.post(new Notification(NotificationType.DANGER, entity.getName().getString() + " is closing to you."));
                    }
                }
            }
        }
    }
}
