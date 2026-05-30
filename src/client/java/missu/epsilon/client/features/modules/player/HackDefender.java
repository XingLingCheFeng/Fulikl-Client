package missu.epsilon.client.features.modules.player;

import com.google.common.collect.Maps;
import missu.epsilon.client.event.events.game.WorldEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.utils.client.ClientUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static missu.epsilon.client.utils.Wrapper.mc;

/**
 * Created by Daniel on 2026/01/26
 * IDK if this AntiCheat can work well haha.
 */

@ModuleInfo(name = "HackDefender", description = "Mark hackers on your screen", defaultOn = true, category = ModuleCategory.PLAYER)
public class HackDefender extends Module {
    public static MultiBoolValue addons = new MultiBoolValue("Addons", new BoolValue[]{
            new BoolValue("Cherish IRC", false),
            new BoolValue("AntiCheat", false)
    });
    public static BoolValue alert = new BoolValue("Alert",false);

    public static Map<PlayerEntity, Integer> violationLevels = Maps.newHashMap();

    @Override
    public void onEnable() {
        violationLevels.clear();
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        Map<PlayerEntity, Integer> filteredViolations = Maps.newHashMap();

        for (Map.Entry<PlayerEntity, Integer> entry : violationLevels.entrySet()) {
            if (entry.getValue() < 15) {
                filteredViolations.put(entry.getKey(), entry.getValue());
            }
        }

        violationLevels = filteredViolations;
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.world == null || mc.player == null) return;

        if (addons.get("AntiCheat")) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (player == mc.player || getViolationLevel(player) == 15) continue;

                if (isAutoBlock(player)) {
                    addViolation(player, "AutoBlock");
                }

                if (noSlowDown(player)) {
                    addViolation(player, "NoSlow");
                }
            }
        }
    }

    public static boolean isAutoBlock(PlayerEntity player) {
        return player.isBlocking() && player.handSwinging;
    }

    public static boolean noSlowDown(PlayerEntity player) {
        if (!player.isBlocking()) return false;

        Vec3d movement = player.getVelocity();
        double speedXZ = Math.sqrt(movement.x * movement.x + movement.z * movement.z);

        return speedXZ > 0.07 || player.isSprinting();
    }

    private void addViolation(PlayerEntity player,String reason) {
        int currentVL = violationLevels.getOrDefault(player, 0);
        if (currentVL >= 15) return;
        violationLevels.put(player, currentVL + 1);

        if (alert.get() && getViolationLevel(player) <= 15) {
            ClientUtils.addMessage("§7[§b" + "AntiCheat" + "§7]§r" + " " + player.getName().getString() + " Failed: " + reason + " " + "§7VL: " + getViolationLevel(player));
        }
    }
    public static int getViolationLevel(PlayerEntity player) {
        return violationLevels.getOrDefault(player, 0);
    }

    public static List<PlayerEntity> getViolatingPlayers() {
        return new ArrayList<>(violationLevels.keySet());
    }
}
