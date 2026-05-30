package missu.epsilon.client.utils.entity;


import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.player.AntiBot;
import missu.epsilon.client.features.modules.exploit.MurderMystery;
import missu.epsilon.client.features.modules.player.HackDefender;
import missu.epsilon.client.features.modules.player.Teams;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.render.ColorUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static missu.epsilon.client.utils.Wrapper.mc;

public class EntityDataUtils {
    public static ColorPanel getEntitySpecialisedColor(Entity entity) {
        AbstractTeam abstractTeam = entity.getScoreboardTeam();

        int colorInteger = (abstractTeam != null && abstractTeam.getColor().getColorValue() != null) ? abstractTeam.getColor().getColorValue() : 16777215;

        if (colorInteger == 11184810) {
            colorInteger = 16777215;
        }

        int red = (colorInteger >> 16) & 0xFF, green = (colorInteger >> 8) & 0xFF, blue = colorInteger & 0xFF;


        if (entity instanceof PlayerEntity) {
            if (!MurderMystery.murdererList.isEmpty()) {
                if (MurderMystery.murdererList.contains(entity)) {
                    return ColorUtils.colorToColorPanel(new Color(255,0,0));
                }
            }

            if (!MurderMystery.bowList.isEmpty()) {
                if (MurderMystery.bowList.contains(entity) && !MurderMystery.murdererList.contains(entity)) {
                    return ColorUtils.colorToColorPanel(new Color(0,255,255));
                }
            }

            if (Client.moduleManager.getModule(HackDefender.class).getState()) {
                if (HackDefender.addons.get("AntiCheat")) {
                    if (!HackDefender.getViolatingPlayers().isEmpty()) {
                        if (HackDefender.getViolatingPlayers().contains(entity) && !(Teams.isInYourTeam((LivingEntity) entity) && Client.moduleManager.getModule(Teams.class).getState()) && HackDefender.getViolationLevel((PlayerEntity) entity) >= 15) {
                            return ColorUtils.colorToColorPanel(new Color(255,100,100));
                        }
                    }
                }
            }
        }

        return ColorPanel.createColorPanel(red / 255f, green / 255f, blue / 255f, 1f);
    }

    public static @NotNull StringBuilder getEntityName(LivingEntity entity) {
        StringBuilder name = new StringBuilder((entity.getDisplayName() == null ? "Invalid Name" : entity.getDisplayName().getString()));
        String nameString = entity.getDisplayName() == null ? "Invalid Name" : entity.getDisplayName().getString();

        if (entity instanceof PlayerEntity) {
            if (!MurderMystery.murdererList.isEmpty()) {
                if (MurderMystery.murdererList.contains(entity)) {
                    name.insert(0, "[Murderer] ");
                }
            }

            if (!MurderMystery.bowList.isEmpty()) {
                if (MurderMystery.bowList.contains(entity) && !MurderMystery.murdererList.contains(entity)) {
                    name.insert(0,  "[Bow] ");
                }
            }

            if (Client.moduleManager.getModule(HackDefender.class).getState()) {
                if (HackDefender.addons.get("AntiCheat")) {
                    if (!HackDefender.getViolatingPlayers().isEmpty()) {
                        if (HackDefender.getViolatingPlayers().contains(entity)) {
                            name.insert(0, "[Hacker VL: " + HackDefender.getViolationLevel((PlayerEntity) entity) + "] ");
                        }
                    }
                }
                if (HackDefender.addons.get("Cherish IRC")) {
                    if (ClientData.isCoolUser(nameString)) {
                        name.insert(0,  "[Cherish] ");
                    }
                }
            }
        }

        if (entity.isInvisible()) {
            name.insert(0, "[Invisible] ");
        }

        if (Teams.isInYourTeam(entity) && Client.moduleManager.getModule(Teams.class).getState()) {
            name.insert(0, "[Teammate] ");
        }

        if (AntiBot.isBot(entity) && Client.moduleManager.getModule(AntiBot.class).getState()) {
            name.insert(0, "[Bot] ");
        }


        return name;
    }

    @SuppressWarnings("DataFlowIssue")
    public static float getEntityScoreboardHealth(LivingEntity entity) {
        ScoreboardObjective objective = mc.world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);

        if (objective == null) {
            return 0f;
        }

        ReadableScoreboardScore score = objective.getScoreboard().getScore(entity, objective);

        if (score == null) {
            return 0f;
        }

        String displayName = objective.getDisplayName().getString();

        if (score.getScore() <= 0 || !displayName.contains("❤")) {
            return 0f;
        }

        return score.getScore();
    }

}
