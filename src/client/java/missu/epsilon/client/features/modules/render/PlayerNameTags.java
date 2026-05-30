package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.render.RenderNameTagsEvent;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.exploit.MurderMystery;
import missu.epsilon.client.features.modules.player.AntiBot;
import missu.epsilon.client.features.modules.player.HackDefender;
import missu.epsilon.client.features.modules.player.Teams;
import missu.epsilon.client.features.modules.visual.PostProcessing;
import missu.epsilon.client.sxmurxy.builders.Builder;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.BlurTaskInstance;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltBlur;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.animations.basic.screen.ScreenPosition;
import missu.epsilon.client.utils.animations.basic.screen.ScreenPositionUtils;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.render.ColorUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Objects;
import java.util.Set;

@ModuleInfo(name = "PlayerNameTags", category = ModuleCategory.RENDER, description = "Show NameTags on players")
public class PlayerNameTags extends Module {

    @SuppressWarnings("unused")
    @EventTarget
    public void onRenderNameTags(RenderNameTagsEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }
        if (event.entity instanceof PlayerEntity player) {
            if (player.getUuid() == null) {
                return;
            }
            if (player == mc.player && mc.options.getPerspective().isFirstPerson()) {
                return;
            }
            if (player.getDisplayName() == null) {
                return;
            }
            if (Objects.requireNonNull(player.getDisplayName()).getString().contains("[NPC]")) {
                return;
            }
            if (Objects.requireNonNull(player.getDisplayName()).getString().contains("CIT-")) {
                return;
            }
            event.cancelEvent();
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onRenderScreen(RenderNvgEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }
        for (LivingEntity entity : Objects.requireNonNull(mc.world).getPlayers()) {
            if (entity.getUuid() == null) {
                continue;
            }
            if (entity.getDisplayName() == null) {
                continue;
            }
            if (entity.getDisplayName().getString().contains("[NPC]")) {
                continue;
            }
            if (entity.getDisplayName().getString().contains("CIT-")) {
                continue;
            }
            ScreenPosition entityScreenPosition = ScreenPositionUtils.createScreenPosition(entity);
            if (!entityScreenPosition.viewable) {
                continue;
            }
            float screenX = (float) entityScreenPosition.screenX;
            float screenY = (float) entityScreenPosition.screenY;
            StringBuilder title = new StringBuilder();
            onNameTagRender(event.drawContext(), event.matrix4f(), entity, screenX, screenY);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    private static void onNameTagRender(DrawContext drawContext, Matrix4f matrix4f, LivingEntity entity, float screenX, float screenY) {
        onItemRender(drawContext, matrix4f, entity, screenX, screenY);
        float preoffset = 5f;
        String entityname = entity.getName().getString();
        float totalWidth = 0f;
        screenY = screenY - 18f;
        java.util.List<NameTagPart> parts = new java.util.ArrayList<>();
        if (Client.moduleManager.getModule(HackDefender.class).getState()) {
            if (HackDefender.addons.get("AntiCheat")) {
                if (!HackDefender.getViolatingPlayers().isEmpty()) {
                    if (HackDefender.getViolatingPlayers().contains(entity)) {
                        String text = Formatting.RED + "Hacker" + Formatting.RESET;
                        float stringWidth = FontManager.BoldPingFang.getStringWidth(text, 14);
                        parts.add(new NameTagPart(text, stringWidth));
                        totalWidth += stringWidth + preoffset;
                    }
                }
            }
        }
        if (!MurderMystery.murdererList.isEmpty()) {
            if (MurderMystery.murdererList.contains(entity)) {
                String text = Formatting.RED + "Murderer" + Formatting.RESET;
                float stringWidth = FontManager.BoldPingFang.getStringWidth(text, 14);
                parts.add(new NameTagPart(text, stringWidth));
                totalWidth += stringWidth + preoffset;
            }
        }
        if (!MurderMystery.bowList.isEmpty()) {
            if (MurderMystery.bowList.contains(entity) && !MurderMystery.murdererList.contains(entity)) {
                String text = Formatting.GREEN + "Bow" + Formatting.RESET;
                float stringWidth = FontManager.BoldPingFang.getStringWidth(text, 14);
                parts.add(new NameTagPart(text, stringWidth));
                totalWidth += stringWidth + preoffset;
            }
        }
        if (Teams.isInYourTeam(entity)) {
            String text = Formatting.GREEN + "Team" + Formatting.RESET;
            float stringWidth = FontManager.BoldPingFang.getStringWidth(text, 14);
            parts.add(new NameTagPart(text, stringWidth));
            totalWidth += stringWidth + preoffset;
        }
        if (AntiBot.isBot(entity)) {
            String text = Formatting.GRAY + "Bot" + Formatting.RESET;
            float stringWidth = FontManager.BoldPingFang.getStringWidth(text, 14);
            parts.add(new NameTagPart(text, stringWidth));
            totalWidth += stringWidth + preoffset;
        }
        if (Client.moduleManager.getModule(HackDefender.class).getState() && HackDefender.addons.get("Cherish IRC") && ClientData.isCoolUser(entityname)) {
            ClientData.UserData data = ClientData.userData.get(entityname);
            String coolUserText = entityname + Formatting.RED + "(" + data.name + ")" + Formatting.RESET;
            String coolClientText = (data.beta ? Formatting.DARK_RED : Formatting.RED) + data.client + (data.beta ? "[Beta]" : "") + Formatting.RESET;
            float userStringWidth = FontManager.BoldPingFang.getStringWidth(coolUserText, 14);
            float clientStringWidth = FontManager.BoldPingFang.getStringWidth(coolClientText, 14);
            parts.add(new NameTagPart(coolClientText, clientStringWidth));
            totalWidth += clientStringWidth + preoffset;
            parts.add(new NameTagPart(coolUserText, userStringWidth));
            totalWidth += userStringWidth + preoffset;
        } else {
            float stringWidth = FontManager.BoldPingFang.getStringWidth(entityname, 14);
            parts.add(new NameTagPart(entityname, stringWidth));
            totalWidth += stringWidth + preoffset;
        }
        if (entity instanceof LivingEntity) {
            String health = Formatting.WHITE + formatHealth(entity.getHealth()) + Formatting.RESET;
            float stringWidth = FontManager.BoldPingFang.getStringWidth(health, 12) + 2f;
            String heart = Formatting.RED + "\uE87D" + Formatting.RESET;
            stringWidth += FontManager.FilledMaterial.getStringWidth(heart, 13);
            parts.add(new NameTagPart(health, stringWidth, "\uE87D"));
            totalWidth += stringWidth + preoffset;
        }
        totalWidth -= preoffset;
        float currentX = screenX - (totalWidth / 2f);
        for (NameTagPart part : parts) {
            float padding = 2f;
            float bgX = currentX - padding;
            float bgY = screenY - 3.5f;
            float bgWidth = part.width + padding * 2f;
            float bgHeight = 7f + padding * 2f;
            drawback(matrix4f, bgX, bgY, bgWidth, bgHeight);
            if (!part.icon.isEmpty()) {
                FontManager.BoldPingFang.drawGlowString(14f, part.text, currentX, screenY - 1f, new ColorPanel(1f, 1f, 1f, 0.9f), new ColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
                FontManager.FilledMaterial.drawGlowString(13f, "\uE87D", currentX + FontManager.BoldPingFang.getStringWidth(part.text, 12) + 2f, screenY - 0.5f, new ColorPanel(1f, 0f, 0f, 0.9f), new ColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
            } else {
                if (part.text.contains(entityname)) {
                    FontManager.BoldPingFang.drawGlowString(14f, part.text, currentX, screenY - 1f, new ColorPanel(1f, 1f, 1f, 0.9f), new ColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
                } else {
                    FontManager.BoldPingFang.drawGlowString(14f, part.text, currentX + 0.5f, screenY - 1f, new ColorPanel(1f, 1f, 1f, 0.9f), new ColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
                }
            }
            currentX += part.width + preoffset;
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void onItemRender(DrawContext context, Matrix4f matrix4f, LivingEntity entity, float screenX, float screenY) {
        try {
            float preoffset = 5f;
            float totalWidth = 0f;
            screenY = screenY - 36f;
            float everyWidth = 12f;
            java.util.List<ItemTagPart> parts = new java.util.ArrayList<>();
            Set<String> tags = ClientData.tags.get(entity);
            if (tags != null) {
                for (String tag : tags) {
                    switch (tag) {
                        case "God Axe":
                            parts.add(new ItemTagPart(Items.GOLDEN_AXE, tag, everyWidth));
                            totalWidth += everyWidth + preoffset;
                            break;
                        case "Enchanted Golden Apple":
                            parts.add(new ItemTagPart(Items.ENCHANTED_GOLDEN_APPLE, tag, everyWidth));
                            totalWidth += everyWidth + preoffset;
                            break;
                        case "End Crystal":
                            parts.add(new ItemTagPart(Items.END_CRYSTAL, tag, everyWidth));
                            totalWidth += everyWidth + preoffset;
                            break;
                        case "KB Ball":
                            parts.add(new ItemTagPart(Items.SLIME_BALL, tag, everyWidth));
                            totalWidth += everyWidth + preoffset;
                            break;
                        case "KB Stick":
                            parts.add(new ItemTagPart(Items.STICK, tag, everyWidth));
                            totalWidth += everyWidth + preoffset;
                            break;
                        case "Punch Bow", "Power Bow":
                            parts.add(new ItemTagPart(Items.BOW, tag, everyWidth));
                            totalWidth += everyWidth + preoffset;
                            break;
                    }
                }
            }
            totalWidth -= preoffset;
            float currentX = screenX - (totalWidth / 2f);
            for (ItemTagPart part : parts) {
                float padding = 2f;
                float bgX = currentX - padding;
                float bgY = screenY - 3.5f;
                float bgWidth = part.width + padding * 2f;
                drawback(matrix4f, bgX, bgY, bgWidth, bgWidth);
                if (part.text.equals("Punch Bow")) {
                    RenderUtils.renderScaledItem(context, Items.SLIME_BALL.getDefaultStack(), bgX + 6f, bgY + 6.5f, 0.6f);
                } else if (part.text.equals("Power Bow") ) {
                    FontManager.FilledMaterial.drawString(18, "\uEBF2", bgX + 6f, bgY + 6.8f, ColorUtils.colorToColorPanel(new Color(255, 137, 54)).updateAlpha(0.75f), false);
                }
                RenderUtils.renderScaledItem(context, part.item.getDefaultStack(), bgX, bgY, 0.9f);
                currentX += part.width + preoffset;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String formatHealth(float health) {
        if (health == (int) health) {
            return String.valueOf((int) health);
        } else {
            return String.format("%.1f", health);
        }
    }

    public static void drawback(Matrix4f matrix4f, float x, float y, float w, float h) {
        float smoothness = 5f;
        BuiltBlur blur = Builder.blur().size(new SizeState(w + smoothness, h + smoothness)).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).color(QuadColorState.TRANSPARENT).position(new PositionState(x - smoothness / 2f, y - smoothness / 2f)).matrix4f(matrix4f).build();
        BlurTaskInstance.addTask(blur);
        RenderUtils.drawAppleRoundedRect(x, y, w, h, new ColorPanel(0f, 0f, 0f, 0.3f), 5f);
    }

    private static class NameTagPart {
        String text;
        String icon;
        float width;
        LivingEntity entity;
        ColorPanel bgColor;

        NameTagPart(String text, float width) {
            this.text = text;
            this.icon = "";
            this.width = width;
            this.entity = null;
            this.bgColor = new ColorPanel(0f, 0f, 0f, 0.3f);
        }

        NameTagPart(String text, float width, String icon) {
            this.text = text;
            this.icon = icon;
            this.width = width;
            this.entity = null;
            this.bgColor = new ColorPanel(0f, 0f, 0f, 0.3f);
        }
    }

    private static class ItemTagPart {
        Item item;
        String text;
        float width;
        ColorPanel bgColor;

        ItemTagPart(Item item, String text, float width) {
            this.item = item;
            this.text = text;
            this.width = width;
            this.bgColor = new ColorPanel(0f, 0f, 0f, 0.3f);
        }
    }

}
