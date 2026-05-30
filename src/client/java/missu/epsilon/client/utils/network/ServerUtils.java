package missu.epsilon.client.utils.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.text.Text;
import net.minecraft.item.Items;

import java.util.Collection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static missu.epsilon.client.utils.Wrapper.mc;

public class ServerUtils {
    public static MinecraftClient lastServer;

    public static boolean isInLobby() {
        if (mc.world == null) return true;
        Iterable<Entity> entities = mc.world.getEntities();
        for (Entity entity : entities) {
            if (entity != null && entity.getName().getString().contains("§e§lCLICK TO PLAY")) {
                return true;
            }
        }
        return mc.player.getInventory().getStack(8) != null && mc.player.getInventory().getStack(8).getItem() == Items.NETHER_STAR && mc.player.getInventory().getStack(0) != null && mc.player.getInventory().getStack(0).getItem() == Items.COMPASS;
    }

    public static boolean isBedwarsPracticeOrReplay() {
        if (isHypixel()) {
            if (mc.world == null || mc.player == null) {
                return false;
            }
            final Scoreboard scoreboard = mc.world.getScoreboard();
            if (scoreboard == null) {
                return false;
            }
            final ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
            if (objective == null) {
                return false;
            }
            String stripped = stripString(objective.getDisplayName().getString());
            if (stripped.contains("BED WARS PRACTICE") || stripped.contains("REPLAY")) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static boolean isHypixel() {
        if (mc.isInSingleplayer()) {
            return false;
        }
        if (lastServer == null) {
            return false;
        }
        return getScoreboardLastLine().contains("hypixel.net");
    }

    public static boolean isHypixelAggressive() {
        if (mc.isInSingleplayer()) {
            // ChatUtils.print("[HypixelCheck] singleplayer -> false");
            return false;
        }

        // ServerData serverData = mc.getCurrentServerData();
        // String ip = serverData != null && serverData.serverIP != null ? serverData.serverIP.toLowerCase(Locale.ROOT) : "";
        String ip = "";
        boolean domainHit = ip.contains("hypixel.net") || ip.contains("hypixel.io") || ip.contains("hypixel.cc");

        // String brandRaw = mc.player != null ? mc.player.getClientBrand() : "";
        String brand = "".toLowerCase(Locale.ROOT);
        boolean brandHit = brand.contains("hypixel");

        String title = getScoreboardTitle();
        boolean titleHit = title.contains("HYPIXEL");

        String lastLine = getScoreboardLastLine();
        boolean lastLineHit = lastLine.contains("hypixel.net");

        // String tabHeader = getTabComponentText("header");
        // String tabFooter = getTabComponentText("footer");
        String tabHeader = "";
        String tabFooter = "";
        boolean tabHit = tabHeader.contains("hypixel") || tabFooter.contains("hypixel");

        // String motd = serverData != null && serverData.serverMOTD != null ? ChatUtils.stripColor(serverData.serverMOTD) : "";
        String motd = "";
        String motdClean = stripString(motd).toLowerCase(Locale.ROOT);
        boolean motdHit = motdClean.contains("hypixel");

        boolean scoreboardEvidence = titleHit || lastLineHit || tabHit;
        boolean result = domainHit || brandHit || motdHit || scoreboardEvidence;

        // ChatUtils.print("[HypixelCheck] ip=" + ip + " domain=" + domainHit + ", brand=" + brand + " brandHit=" + brandHit + ", title="" + title + "" lastLine="" + lastLine + "" tabH="" + tabHeader + "" tabF="" + tabFooter + "" motdHit=" + motdHit + " -> " + result);
        return result;
    }

    // 获取scoreboard最后一行内容的方法
    public static String getScoreboardLastLine() {
        // 由于Minecraft 1.21.4 API变化，暂时返回空字符串
        // 后续可以基于具体API实现
        return "";
    }

    private static String getScoreboardTitle() {
        // 由于Minecraft 1.21.4 API变化，暂时返回空字符串
        // 后续可以基于具体API实现
        return "";
    }

    private static String getTabComponentText(String fieldName) {
        // Minecraft 1.21.4可能没有相同的Tab API，暂时留空
        return "";
    }
    
    private static String stripString(String str) {
        if (str == null) return "";
        return str.replaceAll("\\u00A7[0-9a-fk-or]", "");
    }
}

