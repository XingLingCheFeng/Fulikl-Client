package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "Teams",description = "Choose entity to be your friends",category = ModuleCategory.PLAYER)
public class Teams extends Module {
    private static final BoolValue armorColor = new BoolValue("Armor Color", true);
    private static final BoolValue nameColor = new BoolValue("Name Color", true);
    private static final BoolValue teamColor = new BoolValue("Team Color", true);
    private static final BoolValue scoreboard = new BoolValue("Scoreboard", false);
    private static final BoolValue nameStyle = new BoolValue("Name Style", false);

    /**
     * Check if [entity] is in your own team using scoreboard, armor color, name color, or GommeSW logic
     */
    public static boolean isInYourTeam(LivingEntity entity) {
        if (!Client.moduleManager.getModule(Teams.class).isEnabled()) {
            return false;
        }

        if (mc.player != null && mc.world != null) {
            if (scoreboard.getValue() && mc.player.getScoreboardTeam() != null && entity.getScoreboardTeam() != null && mc.player.getScoreboardTeam().isEqual(entity.getScoreboardTeam())) {
                return true;
            }

            if (armorColor.getValue()) {
                if (entity instanceof PlayerEntity entityPlayer) {
                    ItemStack myHead = mc.player.getInventory().armor.get(3);
                    ItemStack entityHead = entityPlayer.getInventory().armor.get(3);

                    if (!myHead.isEmpty() && !entityHead.isEmpty()) {
                        int myTeamColor = getArmorColor(myHead);
                        int entityTeamColor = getArmorColor(entityHead);

                        return myTeamColor == entityTeamColor;
                    }
                }
            }

            if (teamColor.getValue() && entity.getTeamColorValue() == mc.player.getTeamColorValue() && mc.player.getTeamColorValue() == 0xFFFFFF) {
                return true;
            }

            if (nameStyle.getValue()){
                if (entity.getDisplayName() != null && entity.getDisplayName().getStyle().getColor() != null &&  mc.player.getDisplayName() != null && mc.player.getDisplayName().getStyle().getColor() != null && entity.getDisplayName().getStyle().getColor().getName().equals(mc.player.getDisplayName().getStyle().getColor().getName())) {
                    return true;
                }
            }

            Text displayName = mc.player.getDisplayName();

            if (nameColor.getValue() && displayName != null && !displayName.getString().isEmpty() && entity.getDisplayName() != null && !entity.getDisplayName().getString().isEmpty()) {
                String targetName = entity.getDisplayName().getString().replace("§r", "");
                String clientName = displayName.getString().replace("§r", "");

                if (clientName.length() > 1 && targetName.length() > 1) {
                    return targetName.startsWith("§" + clientName.charAt(1));
                }
            }
        }

        return false;
    }

    private static int getArmorColor(ItemStack stack) {
        var color = stack.getComponents().get(DataComponentTypes.DYED_COLOR);

        if (color != null) {
            return color.rgb();
        }

        return -1;
    }
}
