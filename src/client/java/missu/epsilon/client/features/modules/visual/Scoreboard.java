package missu.epsilon.client.features.modules.visual;

import lombok.Setter;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.ingameui.Dragging;
import missu.epsilon.client.sxmurxy.builders.Builder;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.BlurTaskInstance;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltBlur;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.Comparator;

@Setter
@ModuleInfo(name = "Scoreboard", category = ModuleCategory.VISUAL)
public class Scoreboard extends Module {
    private final BoolValue showScore = new BoolValue("Show Score", false);
    private final ListValue font = new ListValue("Show Score", new String[]{"Minecraft","Client"},"Minecraft");
    private ScoreboardObjective objective;

    private final Dragging dragging = Client.createDrag(this, "Scoreboard", 200, 250);

    private record SidebarRow(Text name, Text score, int scoreWidth) {}

    @EventTarget
    public void render2D(RenderNvgEvent event) {
        float posY = dragging.getYPos();
        float posX = dragging.getXPos();
        DrawContext drawContext = event.drawContext();
        if (mc.player != null && mc.world != null) {
            if (this.objective != null) {
                final NumberFormat numberFormat = this.objective.getNumberFormatOr(StyledNumberFormat.RED);

                // 收集并排好序的侧边栏行
                final SidebarRow[] sidebarEntries = this.objective.getScoreboard().getScoreboardEntries(objective)
                        .stream()
                        .filter(entry -> !entry.hidden())
                        .sorted(Comparator.comparing(ScoreboardEntry::value).reversed()
                                .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER))
                        .limit(15L)
                        .map(entry -> {
                            final Team team = this.objective.getScoreboard().getScoreHolderTeam(entry.owner());
                            final Text rawName = entry.name();
                            final Text decoratedName = Team.decorateName(team, rawName);
                            final Text formattedScore = entry.formatted(numberFormat);
                            final int scoreTextWidth = font.is("Client") ? (int) FontManager.BoldPingFang.getStringWidth(formattedScore.getString(),18) : mc.textRenderer.getWidth(formattedScore);
                            return new SidebarRow(decoratedName, formattedScore, scoreTextWidth);
                        })
                        .toArray(SidebarRow[]::new);


                // 标题与宽度计算
                final Text title = this.objective.getDisplayName();
                final int titleWidth = font.is("Client") ? (int) FontManager.BoldPingFang.getStringWidth(title.getString(), 18) :mc.textRenderer.getWidth(title);
                final int colonWidth = font.is("Client") ? (int) FontManager.BoldPingFang.getStringWidth(": ", 18) : mc.textRenderer.getWidth(": ");
                int maxRowWidth = titleWidth;

                for (SidebarRow row : sidebarEntries) {
                    final int nameWidth = font.is("Client") ?  (int) FontManager.BoldPingFang.getStringWidth(row.name.getString(), 18) : mc.textRenderer.getWidth(row.name);
                    final int rowWidth = row.scoreWidth > 0 ? nameWidth + colonWidth + row.scoreWidth : nameWidth;
                    maxRowWidth = Math.max(maxRowWidth, rowWidth);
                }

                // 布局参数
                final int rowCount = sidebarEntries.length;
                final int rowHeight = 9;
                final int totalRowsHeight = rowCount * rowHeight;
                final int centerY = (int) (posY + totalRowsHeight + rowHeight);
                final int leftX = (int) posX + 2;
                final int rightX = leftX + maxRowWidth;
                final int contentTopY = (int) posY;

                BuiltBlur blur = Builder.blur()
                        .size(new SizeState(maxRowWidth + 4, (rowCount + 1) * 9 + 4))
                        .radius(new QuadRadiusState(5f))
                        .blurRadius(PostProcessing.blurStrength.get().floatValue())
                        .smoothness(5f)
                        .color(QuadColorState.TRANSPARENT)
                        .position(new PositionState(leftX - 2, contentTopY - 2))
                        .matrix4f(event.matrix4f())
                        .build();
                BlurTaskInstance.addTask(blur);

                RenderUtils.drawRoundedRect(leftX - 2, contentTopY - 2, maxRowWidth + 4, (rowCount + 1) * 9 + 4, ColorPanel.createColorPanel(0f,0f,0f,0.35f),4f);


                // 标题居中绘制
                if (font.is("Minecraft")) {
                    drawContext.drawText(mc.textRenderer, title, leftX + maxRowWidth / 2 - titleWidth / 2, contentTopY + 1, new Color(255, 255, 255, 191).getRGB(), false);
                } else {
                    FontManager.BoldPingFang.drawGlowString(18, title.getString(), leftX + (float) maxRowWidth / 2 - (float) titleWidth / 2, contentTopY + 1, ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f), ColorPanel.createColorPanel(1f, 1f, 1f, 0.35f), false, 5f);
                }


                // 各行绘制
                for (int index = 0; index < rowCount; index++) {
                    final SidebarRow row = sidebarEntries[index];
                    final int rowY = centerY - (rowCount - index) * rowHeight + 1;

                    if (font.is("Minecraft")) {
                        drawContext.drawText(mc.textRenderer, row.name, leftX, rowY, new Color(255, 255, 255, 191).getRGB(), false);
                    } else {
                        FontManager.BoldPingFang.drawGlowString(18, row.name.getString(), leftX, rowY, ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f), ColorPanel.createColorPanel(1f, 1f, 1f, 0.35f), false, 5f);
                    }

                    if (this.showScore.getValue()) {
                        if (font.is("Minecraft")) {
                            drawContext.drawText(mc.textRenderer, row.score, rightX - row.scoreWidth, rowY, new Color(255, 255, 255, 191).getRGB(), false);
                        } else {
                            FontManager.BoldPingFang.drawGlowString(18, row.score.getString(), rightX - row.scoreWidth, rowY, ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f), ColorPanel.createColorPanel(1f, 1f, 1f, 0.35f), false, 5f);
                        }
                    }
                }

                if (mc.currentScreen instanceof ChatScreen) {
                    dragging.setWidth(maxRowWidth + 4);
                    dragging.setHeight((rowCount + 1) * 9 + 4);
                }

                this.objective = null;
            } else if (mc.currentScreen instanceof ChatScreen) {
                dragging.setWidth(80);
                dragging.setHeight(120);

                BuiltBlur blur = Builder.blur()
                        .size(new SizeState(dragging.getWidth(), dragging.getHeight()))
                        .radius(new QuadRadiusState(5f))
                        .blurRadius(PostProcessing.blurStrength.get().floatValue())
                        .smoothness(5f)
                        .color(QuadColorState.TRANSPARENT)
                        .position(new PositionState(posX, posY))
                        .matrix4f(event.matrix4f())
                        .build();
                BlurTaskInstance.addTask(blur);

                RenderUtils.drawRoundedRect(posX, posY, dragging.getWidth(),dragging.getHeight(), ColorPanel.createColorPanel(0f,0f,0f,0.35f),4f);

                if (font.is("Minecraft")) {
                    drawContext.drawText(mc.textRenderer, "Example", (int) (posX + dragging.getWidth() / 2 - mc.textRenderer.getWidth("Example") / 2f), (int) (posY + dragging.getHeight() / 2 - mc.textRenderer.fontHeight / 2f), new Color(255, 255, 255, 191).getRGB(), false);
                } else {
                    FontManager.BoldPingFang.drawGlowString(18, "Example", (posX + dragging.getWidth() / 2 - FontManager.BoldPingFang.getStringWidth("Example", 18) / 2f), (int) (posY + dragging.getHeight() / 2 - 20 / 2f), ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f), ColorPanel.createColorPanel(1f, 1f, 1f, 0.35f), false, 5f);
                }
            }
        }
    }
}
