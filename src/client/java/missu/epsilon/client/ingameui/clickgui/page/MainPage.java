package missu.epsilon.client.ingameui.clickgui.page;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.value.Value;
import missu.epsilon.client.features.value.impl.*;
import missu.epsilon.client.ingameui.clickgui.MouseBehavior;
import missu.epsilon.client.utils.animations.basic.animation.AnimationState;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.render.ColorUtils;
import missu.epsilon.client.utils.render.RenderHelper;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import missu.epsilon.client.utils.render.font.Icon;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import static missu.epsilon.client.ingameui.clickgui.ClickGUI.animation;
import static missu.epsilon.client.utils.Wrapper.*;
import static missu.epsilon.client.utils.render.ColorUtils.hsvToRgb;

@SuppressWarnings("RedundantCast")
public class MainPage {
    public static TextValue currentFocusedTextValue = null;
    public static KeyBindValue currentWaitingForBindValue = null;
    public static boolean cursorVisible = true;
    public static long lastKeyPressTime = 0L;

    public static void onRenderMainPage() {
        float animationAlpha = animation.value();
        float backgroundAlpha = animationAlpha * 0.5f;

        float backgroundWidth = 450f, backgroundHeight = 262f, backgroundRadius = 12f;
        float backgroundX = mc.getWindow().getScaledWidth() / 2f - backgroundWidth / 2f, backgroundY = mc.getWindow().getScaledHeight() / 2f - backgroundHeight / 2f;

        if (animation.animationState.equals(AnimationState.FORWARDS)) backgroundY += 25f * (1f - animation.value());
        if (animation.animationState.equals(AnimationState.BACKWARDS)) backgroundY -= 25f * (1f - animation.value());

        if (currentFocusedTextValue != null && MouseBehavior.mouseLeftClicked() && !MouseBehavior.leftButtonClicked) {
            if (!MouseBehavior.mouseHoveredClickGUI(backgroundX, backgroundY, backgroundWidth, backgroundHeight)) {
                currentFocusedTextValue = null;
            }
            MouseBehavior.leftButtonClicked = MouseBehavior.mouseLeftClicked();
        }


        float maxSwitchedValue = 0f;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (category.getSwitchedAnimation().value() > maxSwitchedValue) {
                maxSwitchedValue = category.getSwitchedAnimation().value();
            }
        }

        ColorPanel baseColor = ColorPanel.createColorPanel(0f, 0f, 0f, backgroundAlpha * 0.2f);

        RenderUtils.drawAppleRoundedRect(backgroundX, backgroundY, backgroundWidth, backgroundHeight, baseColor, backgroundRadius);

        float modCategoryHeight = 12f, modCategorySpace = 5f, modCategoryRadius = 3f;
        float modCategoryX = backgroundX + backgroundWidth / 2f - ((ModuleCategory.values().length - 1) * modCategorySpace), modCategoryY = backgroundY + modCategorySpace;

        for (ModuleCategory modCategory : ModuleCategory.values()) {
            float contentWidth = (FontManager.FilledMaterial.getStringWidth(modCategory.getHtmlIcon(), 12f) + FontManager.BoldPingFang.getStringWidth(modCategory.getDisplayName(), 12f) + modCategorySpace * 2f) / 2f;

            modCategoryX -= contentWidth;
        }

        for (ModuleCategory modCategory : ModuleCategory.values()) {
            float contentWidth = FontManager.FilledMaterial.getStringWidth(modCategory.getHtmlIcon(), 12f) + FontManager.BoldPingFang.getStringWidth(modCategory.getDisplayName(), 12f) + modCategorySpace * 2f;

            modCategory.getMouseHoveredAnimation().update(MouseBehavior.mouseHoveredClickGUI(modCategoryX, modCategoryY, contentWidth, modCategoryHeight) ? AnimationState.FORWARDS : AnimationState.BACKWARDS);

            if (MouseBehavior.mouseHoveredClickGUI(modCategoryX, modCategoryY, contentWidth, modCategoryHeight)) {
                if (MouseBehavior.mouseLeftClicked() && !MouseBehavior.leftButtonClicked) {
                    List<ModuleCategory> modCategories = Arrays.asList(ModuleCategory.values());
                    modCategories.forEach(mc -> mc.getSwitchedAnimation().update(AnimationState.BACKWARDS));
                    modCategory.getSwitchedAnimation().update(AnimationState.FORWARDS);
                }
                MouseBehavior.leftButtonClicked = MouseBehavior.mouseLeftClicked();
            }

            RenderUtils.drawGradientRoundedRectUD(modCategoryX, modCategoryY, contentWidth, modCategoryHeight,
                    ColorPanel.createColorPanel(0f + modCategory.getColor().red * modCategory.getSwitchedAnimation().value(), 0f + modCategory.getColor().green * modCategory.getSwitchedAnimation().value(), 0f + modCategory.getColor().blue * modCategory.getSwitchedAnimation().value(),
                            animationAlpha * (0.1f + 0.05f * modCategory.getMouseHoveredAnimation().value()) + animationAlpha * modCategory.getSwitchedAnimation().value() * 0.65f),
                    ColorPanel.createColorPanel(0f + modCategory.getColor().red * modCategory.getSwitchedAnimation().value(), 0f + modCategory.getColor().green * modCategory.getSwitchedAnimation().value(), 0f + modCategory.getColor().blue * modCategory.getSwitchedAnimation().value(),
                            animationAlpha * (0.1f + 0.05f * modCategory.getMouseHoveredAnimation().value()) + animationAlpha * modCategory.getSwitchedAnimation().value() * 0.35f),
                    modCategoryRadius);


            FontManager.FilledMaterial.drawString(12f, modCategory.getHtmlIcon(), modCategoryX + 3f, modCategoryY + 3.3f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f), false);

            FontManager.BoldPingFang.drawString(12f, modCategory.getDisplayName(), modCategoryX + 12f, modCategoryY + 3.3f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f), false);

            modCategoryX += contentWidth + modCategorySpace;

            if (modCategory.getSwitchedAnimation().value() > 0f && modCategory.getSwitchedModSettingPageAnimation().value() < 1f) {
                if (modCategory.getSwitchedModSettingPageAnimation().value() <= 0f) {
                    if (MouseBehavior.mouseHoveredClickGUI(backgroundX, backgroundY, backgroundWidth, backgroundHeight)) {
                        onModCategoryScrollAction(modCategory);
                    }
                }

                float modSpace = 5f, modWidthSpace = 10f;
                float modX = backgroundX + modWidthSpace, modY = modCategoryY + modCategoryHeight + modSpace + modCategory.animatingNumber.animatingNumber;
                float modWidth = (backgroundWidth - (modWidthSpace * 2f + modSpace)) / 2f, modHeight = 25f, modRadius = 6f;
                int modCount = 0;


                List<Module> modulesInCategory = Client.moduleManager.getModules().stream()
                        .filter(module -> module.getCategory() == modCategory)
                        .toList();

                float scissorSpace = 1f;
                RenderHelper.scissorStart(backgroundX, backgroundY + modCategorySpace + modCategoryHeight + modSpace - scissorSpace, backgroundWidth, backgroundHeight - modCategorySpace - modCategoryHeight - modSpace + scissorSpace);
                RenderHelper.scaleStart(backgroundX + backgroundWidth / 2f, backgroundY + backgroundHeight / 2f, 1f - (0.1f * (1f - modCategory.getSwitchedAnimation().value() * (1f - modCategory.getSwitchedModSettingPageAnimation().value()))));
                for (Module module : modulesInCategory) {
                    modCount++;

                    if (modCategory.getSwitchedModSettingPageAnimation().value() <= 0f) {
                        module.mouseHoveredAnimation.update(MouseBehavior.mouseHoveredClickGUI(modX, modY, modWidth, modHeight) ? AnimationState.FORWARDS : AnimationState.BACKWARDS);

                        if (MouseBehavior.mouseHoveredClickGUI(modX, modY, modWidth, modHeight) && MouseBehavior.mouseY > modCategoryY + modCategoryHeight + modSpace && MouseBehavior.mouseY < backgroundY + backgroundHeight) {
                            if (MouseBehavior.mouseLeftClicked() && !MouseBehavior.leftButtonClicked) {
                                module.setState(!module.getState());
                            }
                            if (MouseBehavior.mouseRightClicked() && !MouseBehavior.rightButtonClicked) {
                                modCategory.getSwitchedModSettingPageAnimation().update(AnimationState.FORWARDS);
                                modCategory.currentModule = module;
                            }
                            MouseBehavior.leftButtonClicked = MouseBehavior.mouseLeftClicked();
                            MouseBehavior.rightButtonClicked = MouseBehavior.mouseRightClicked();
                        }
                    }

                    RenderUtils.drawAppleRoundedRect(modX, modY, modWidth, modHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * (0.15f + 0.075f * module.mouseHoveredAnimation.value()) * modCategory.getSwitchedAnimation().value() * (1f - modCategory.getSwitchedModSettingPageAnimation().value())), modRadius);

                    FontManager.FilledMaterial.drawString(24f, module.getCategory().getHtmlIcon(), modX + 5f + 7.5f - FontManager.FilledMaterial.getStringWidth(module.getCategory().getHtmlIcon(), 24f) / 2f, modY + 7.5f, ColorPanel.createColorPanel(1, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * (1f - modCategory.getSwitchedModSettingPageAnimation().value())), false);

                    FontManager.BoldPingFang.drawString(12f, module.name, modX + 5f + 20f, modY + 5f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * (1f - modCategory.getSwitchedModSettingPageAnimation().value())), false);

                    FontManager.BoldPingFang.drawString(12f, module.getDescription(), modX + 5f + 20f, modY + 15f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.325f * modCategory.getSwitchedAnimation().value() * (1f - modCategory.getSwitchedModSettingPageAnimation().value())), false);

                    module.activatedAnimation.update(module.getState() ? AnimationState.FORWARDS : AnimationState.BACKWARDS);
                    if (module.activatedAnimation.value() > 0f) {
                        FontManager.FilledMaterial.drawString(20f, Icon.DONE, modX + modWidth - 7.5f - FontManager.FilledMaterial.getStringWidth(Icon.DONE, 20f), modY + 9f, ColorPanel.createColorPanel(1, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * module.activatedAnimation.value() * (1f - modCategory.getSwitchedModSettingPageAnimation().value())), false);
                    }

                    modX += modWidth + modSpace;

                    if (modCount >= 2 && modCount % 2 == 0) {
                        modX = backgroundX + modWidthSpace;
                        modY += modHeight + modSpace;
                    }
                }
                RenderHelper.scaleEnd();
                RenderHelper.scissorEnd();
            }
            if (modCategory.getSwitchedAnimation().value() > 0f && modCategory.getSwitchedModSettingPageAnimation().value() > 0f) {
                if (modCategory.currentModule != null) {
                    onModRender(modCategory, modCategory.currentModule);
                }
            }
        }
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "SuspiciousNameCombination"})
    private static void onModRender(ModuleCategory modCategory, Module module) {
        float animationAlpha = animation.value();

        float backgroundWidth = 450f, backgroundHeight = 262f;
        float backgroundX = mc.getWindow().getScaledWidth() / 2f - backgroundWidth / 2f, backgroundY = mc.getWindow().getScaledHeight() / 2f - backgroundHeight / 2f;

        if (animation.animationState.equals(AnimationState.FORWARDS)) backgroundY += 25f * (1f - animation.value());
        if (animation.animationState.equals(AnimationState.BACKWARDS)) backgroundY -= 25f * (1f - animation.value());

        if (currentFocusedTextValue != null && MouseBehavior.mouseLeftClicked() && !MouseBehavior.leftButtonClicked) {
            if (!MouseBehavior.mouseHoveredClickGUI(backgroundX, backgroundY, backgroundWidth, backgroundHeight)) {
                currentFocusedTextValue = null;
            }
            MouseBehavior.leftButtonClicked = MouseBehavior.mouseLeftClicked();
        }

        float modCategoryHeight = 12f, modCategorySpace = 5f;
        float modCategoryY = backgroundY + modCategorySpace;

        float modSpace = 5f, modWidthSpace = 10f;
        float modX = backgroundX + modWidthSpace, modY = modCategoryY + modCategoryHeight + modSpace;
        float modWidth = backgroundWidth - (modWidthSpace * 2f), modHeight = 25f, modRadius = 6f;

        module.mouseHoveredAnimation.update(MouseBehavior.mouseHoveredClickGUI(modX, modY, modWidth, modHeight) ? AnimationState.FORWARDS : AnimationState.BACKWARDS);

        RenderHelper.scaleStart(backgroundX + backgroundWidth / 2f, backgroundY + backgroundHeight / 2f, 1f - (0.1f * (1f - modCategory.getSwitchedAnimation().value() * (modCategory.getSwitchedModSettingPageAnimation().value()))));

        RenderUtils.drawAppleRoundedRect(modX, modY, modWidth, modHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * (0.15f + 0.075f * module.mouseHoveredAnimation.value()) * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value()), modRadius);

        FontManager.FilledMaterial.drawString(24f, module.getCategory().getHtmlIcon(), modX + 5f + 7.5f - FontManager.FilledMaterial.getStringWidth(module.getCategory().getHtmlIcon(), 24f) / 2f, modY + 7.5f, ColorPanel.createColorPanel(1, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value()), false);

        FontManager.BoldPingFang.drawString(12f, module.name, modX + 5f + 20f, modY + 5f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value()), false);

        FontManager.BoldPingFang.drawString(12f, module.getDescription(), modX + 5f + 20f, modY + 15f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.325f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value()), false);

        module.activatedAnimation.update(module.getState() ? AnimationState.FORWARDS : AnimationState.BACKWARDS);
        if (module.activatedAnimation.value() > 0f) {
            FontManager.FilledMaterial.drawString(20f, Icon.DONE, modX + modWidth - 7.5f - FontManager.FilledMaterial.getStringWidth(Icon.DONE, 20f), modY + 9f, ColorPanel.createColorPanel(1, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * module.activatedAnimation.value() * modCategory.getSwitchedModSettingPageAnimation().value()), false);
        }

        if (modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {
            if (MouseBehavior.mouseHoveredClickGUI(modX, modY, modWidth, modHeight) && MouseBehavior.mouseY > modCategoryY + modCategoryHeight + modSpace && MouseBehavior.mouseY < backgroundY + backgroundHeight) {
                if (MouseBehavior.mouseLeftClicked() && !MouseBehavior.leftButtonClicked) {
                    module.setState(!module.getState());
                }
                if (MouseBehavior.mouseRightClicked() && !MouseBehavior.rightButtonClicked) {
                    modCategory.getSwitchedModSettingPageAnimation().update(AnimationState.BACKWARDS);
                }
                MouseBehavior.leftButtonClicked = MouseBehavior.mouseLeftClicked();
                MouseBehavior.rightButtonClicked = MouseBehavior.mouseRightClicked();
            }
        }

        float settingX = modX, settingY = modY + modHeight + modSpace + module.animatingNumber.animatingNumber;
        float settingSpace = 5f;

        if (module.getValues().isEmpty()) {
            return;
        }

        if (modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {
            onModSettingScrollAction(module);
        }

        float settingHeight = 25f;
        float maxSettingHeightInPage = 7f * (settingHeight + settingSpace);
        RenderHelper.scissorStart(backgroundX, modY + modHeight + modSpace, backgroundWidth, maxSettingHeightInPage);
        for (Value<?> value : module.getValues()) {
            value.availableAnimation.update(value.isAvailable() ? AnimationState.FORWARDS : AnimationState.BACKWARDS);

            if (value.availableAnimation.value() <= 0f) {
                continue;
            }

            if (value instanceof BoolValue) {
                float boxX = settingX, boxY = settingY, boxWidth = backgroundWidth - modWidthSpace * 2f, boxHeight = getSettingHeight(value), boxRadius = 5f;

                RenderUtils.drawAppleRoundedRect(boxX, boxY, boxWidth, boxHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.15f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((BoolValue) value).availableAnimation.value()), boxRadius);

                FontManager.BoldPingFang.drawString(12f, value.name, boxX + 5f, boxY + 5f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((BoolValue) value).availableAnimation.value()), false);

                if (MouseBehavior.mouseHoveredClickGUI(boxX, boxY, boxWidth, boxHeight) && modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {
                    if (MouseBehavior.mouseLeftClicked() && !MouseBehavior.leftButtonClicked && MouseBehavior.mouseY > modY + modHeight + modSpace && MouseBehavior.mouseY < backgroundY + backgroundHeight) {
                        ((BoolValue) value).setValue(!((BoolValue) value).value);
                    }
                    MouseBehavior.leftButtonClicked = MouseBehavior.mouseLeftClicked();
                }

                ((BoolValue) value).animation.update(((BoolValue) value).value ? AnimationState.FORWARDS : AnimationState.BACKWARDS);
                if (((BoolValue) value).animation.value() > 0f) {
                    FontManager.FilledMaterial.drawString(16f, Icon.DONE, boxX + boxWidth - 6f - FontManager.FilledMaterial.getStringWidth(Icon.DONE, 16f), boxY + 4.5f, ColorPanel.createColorPanel(1, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((BoolValue) value).availableAnimation.value() * ((BoolValue) value).animation.value()), false);
                }
            }

            if (value instanceof ColorValue colorValue) {
                float boxX = settingX, boxY = settingY, boxWidth = backgroundWidth - modWidthSpace * 2f, boxHeight = getSettingHeight(value), boxRadius = 5f;

                RenderUtils.drawAppleRoundedRect(boxX, boxY, boxWidth, boxHeight,
                        ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.15f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * colorValue.availableAnimation.value()),
                        boxRadius);

                FontManager.BoldPingFang.drawString(12f, value.name, boxX + 5f, boxY + 5f,
                        ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * colorValue.availableAnimation.value()),
                        false);

                float colorPickerSize = 100f;
                float colorPickerX = boxX + 5f;
                float colorPickerY = boxY + 20f;
                float colorPickerRadius = 8f;

                drawColorPicker(colorPickerX, colorPickerY, colorPickerSize, colorPickerRadius, colorValue, animationAlpha, modCategory);

                float previewX = colorPickerX + colorPickerSize + 10f;
                float previewY = colorPickerY;

                Color currentColor = colorValue.getColor();
                float previewSize = 30f;

                drawTransparencyGrid(previewX, previewY, previewSize, previewSize, 5f, animationAlpha);

                RenderUtils.drawAppleRoundedRect(previewX, previewY, previewSize, previewSize,
                        ColorPanel.createColorPanel(
                                currentColor.getRed() / 255f,
                                currentColor.getGreen() / 255f,
                                currentColor.getBlue() / 255f,
                                currentColor.getAlpha() / 255f * animation.value()
                        ),
                        0f);

                String hexColor = String.format("#%02X%02X%02X", currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue());
                FontManager.BoldPingFang.drawString(12f, hexColor, previewX, previewY + previewSize + 10f,
                        ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value()),
                        false);

                String rgbaText = String.format("RGBA: %d, %d, %d, %d",
                        currentColor.getRed(),
                        currentColor.getGreen(),
                        currentColor.getBlue(),
                        currentColor.getAlpha());
                FontManager.BoldPingFang.drawString(12f, rgbaText, previewX, previewY + previewSize + 25f,
                        ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.5f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value()),
                        false);
            }

            if (value instanceof NumberValue numbervalue) {
                float boxX = settingX, boxY = settingY, boxWidth = backgroundWidth - modWidthSpace * 2f, boxHeight = getSettingHeight(value), boxRadius = 5f;

                RenderUtils.drawAppleRoundedRect(boxX, boxY, boxWidth, boxHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.15f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * value.availableAnimation.value()), boxRadius);

                FontManager.BoldPingFang.drawString(12f, value.name, boxX + 5f, boxY + 5f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * value.availableAnimation.value()), false);

                FontManager.BoldPingFang.drawString(12f, String.format("%.2f, %.2f ~ %.2f", numbervalue.get(), numbervalue.getMin(), numbervalue.getMax()), boxX + boxWidth - 5f - FontManager.BoldPingFang.getStringWidth(String.format("%.2f, %.2f ~ %.2f", numbervalue.get(), numbervalue.getMin(), numbervalue.getMax()), 12f), boxY + 5f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.325f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * value.availableAnimation.value()), false);

                float barX = settingX + 5f, barY = settingY + 15f, barWidth = boxWidth - 10f, barHeight = 5f, barRadius = 2.5f;

                RenderUtils.drawAppleRoundedRect(barX, barY, barWidth, barHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.1f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * value.availableAnimation.value()), barRadius);

                float controllableSpace = 1f;
                if (MouseBehavior.mouseHoveredClickGUI(barX - controllableSpace, barY - controllableSpace, barWidth + controllableSpace * 2f, barHeight + controllableSpace * 2f) && modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {
                    if (MouseBehavior.mouseLeftClicked()) {
                        double steppedValue = getSteppedValue(numbervalue, barX, barWidth);

                        numbervalue.set(steppedValue);
                    }
                }


                numbervalue.animatingNumber.number = (float) (barX + barWidth * ((numbervalue.get() - numbervalue.getMin()) / (numbervalue.getMax() - numbervalue.getMin())));
                numbervalue.animatingNumber.number = Math.max(barX + 2.5f, Math.min(barX + barWidth - barHeight / 2f, numbervalue.animatingNumber.number));
                numbervalue.animatingNumber.animate();

                RenderUtils.drawGradientRoundedRectUD(numbervalue.animatingNumber.animatingNumber - 2.5f, barY, barHeight, barHeight, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.5f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * value.availableAnimation.value()), ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.35f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * numbervalue.availableAnimation.value()), barRadius);
            }

            if (value instanceof TextValue textValue) {
                float boxX = settingX, boxY = settingY, boxWidth = backgroundWidth - modWidthSpace * 2f, boxHeight = getSettingHeight(value), boxRadius = 5f;

                RenderUtils.drawAppleRoundedRect(boxX, boxY, boxWidth, boxHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.15f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * textValue.availableAnimation.value()), boxRadius);

                FontManager.BoldPingFang.drawString(12f, value.name, boxX + 5f, boxY + 5f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * textValue.availableAnimation.value()), false);

                float barX = settingX + 5f, barY = settingY + 13.5f, barWidth = boxWidth - 10f, barHeight = 10f, barRadius = 2.5f;

                RenderUtils.drawAppleRoundedRect(barX, barY, barWidth, barHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.1f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * value.availableAnimation.value()), barRadius);

                boolean isFocused = currentFocusedTextValue == textValue;

                if (isFocused && textValue.getTimer().hasTimeElapsed(500L)) {
                    textValue.getTimer().reset();
                    cursorVisible = !cursorVisible;
                }

                String displayText = textValue.value;
                if (displayText.isEmpty() && !isFocused) {
                    FontManager.BoldPingFang.drawString(12f, "Click to type...", barX + 2f, barY + 2f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.325f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * textValue.availableAnimation.value()), false);
                } else {
                    String textToDraw = displayText;
                    float textWidth = FontManager.BoldPingFang.getStringWidth(displayText, 12f);
                    float maxTextWidth = barWidth - 4f;

                    if (textWidth > maxTextWidth) {
                        while (textWidth > maxTextWidth && textToDraw.length() > 1) {
                            textToDraw = textToDraw.substring(1);
                            textWidth = FontManager.BoldPingFang.getStringWidth(textToDraw, 12f);
                        }
                        textToDraw = "..." + textToDraw;
                    }

                    FontManager.BoldPingFang.drawString(12f, textToDraw, barX + 2f, barY + 2f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * textValue.availableAnimation.value()), false);

                    if (isFocused && cursorVisible) {
                        float cursorX = barX + 2f + FontManager.BoldPingFang.getStringWidth(textToDraw, 12f);
                        RenderUtils.drawRect(cursorX, barY + 1f, 1f, 8f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f));
                    }
                }

                if (MouseBehavior.mouseHoveredClickGUI(boxX, boxY, boxWidth, boxHeight) && modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {
                    if (MouseBehavior.mouseLeftClicked() && !MouseBehavior.leftButtonClicked && MouseBehavior.mouseY > modY + modHeight + modSpace && MouseBehavior.mouseY < backgroundY + backgroundHeight) {
                        currentFocusedTextValue = textValue;
                        cursorVisible = true;
                        textValue.getTimer().reset();
                    }
                    MouseBehavior.leftButtonClicked = MouseBehavior.mouseLeftClicked();
                }
            }

            if (value instanceof BindValue) {
                ((BindValue) value).animatingNumberWidth.animate();
                ((BindValue) value).animatingNumberX.animate();
                ((BindValue) value).animatingNumberY.animate();

                ((BindValue) value).animatingNumberWidth.number = FontManager.BoldPingFang.getStringWidth((String) value.value, 12f);

                ((BindValue) value).silentAnimation.update(module.animatingNumber.animatingNumber != module.animatingNumber.number ? AnimationState.FORWARDS : AnimationState.BACKWARDS);

                float boxX = settingX, boxY = settingY, boxWidth = backgroundWidth - modWidthSpace * 2f, boxHeight = getSettingHeight(value), boxRadius = 5f;

                if (((BindValue) value).silentAnimation.value() < 1f) {
                    RenderUtils.drawAppleRoundedRect(((BindValue) value).animatingNumberX.animatingNumber - 1f, ((BindValue) value).animatingNumberY.animatingNumber - 1f, ((BindValue) value).animatingNumberWidth.animatingNumber + 2f, 6f + 2f, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.15f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((BindValue) value).availableAnimation.value() * (1f - ((BindValue) value).silentAnimation.value())), 1f);
                }

                RenderUtils.drawAppleRoundedRect(boxX, boxY, boxWidth, boxHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.15f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((BindValue) value).availableAnimation.value()), boxRadius);

                FontManager.BoldPingFang.drawString(12f, value.name, boxX + 5f, boxY + 5f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((BindValue) value).availableAnimation.value()), false);

                float eachX = boxX + 5f, eachY = boxY + 15f;
                float maxCount = 5f;
                float eachWidth = (boxWidth - 10f) / maxCount, basicSpace = 10f;
                int settingCount = 0, settingPosition = 0;
                float controllableSpace = 1.5f;
                for (String mode : ((BindValue) value).getModes()) {
                    settingCount++;

                    ((BindValue) value).activatedAnimations.get(settingPosition).update(value.value.equals(mode) ? AnimationState.FORWARDS : AnimationState.BACKWARDS);
                    if (value.value.equals(mode)) {
                        ((BindValue) value).animatingNumberX.number = eachX + eachWidth / 2f - FontManager.BoldPingFang.getStringWidth(mode, 12f) / 2f;
                        ((BindValue) value).animatingNumberY.number = eachY;
                    }

                    if (MouseBehavior.mouseHoveredClickGUI(eachX + eachWidth / 2f - FontManager.BoldPingFang.getStringWidth(mode, 12f) / 2f - controllableSpace, eachY - controllableSpace, FontManager.BoldPingFang.getStringWidth(mode, 12f) + controllableSpace * 2f, basicSpace) && modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {
                        if (MouseBehavior.mouseLeftClicked() && !value.value.equals(mode) && MouseBehavior.mouseY > modY + modHeight + modSpace && MouseBehavior.mouseY < backgroundY + backgroundHeight) {
                            ((BindValue) value).set(mode);
                        }
                    }

                    FontManager.BoldPingFang.drawString(12f, mode, eachX + eachWidth / 2f - FontManager.BoldPingFang.getStringWidth(mode, 12f) / 2f, eachY, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * (0.325f + ((BindValue) value).activatedAnimations.get(settingPosition).value() * 0.325f) * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((BindValue) value).availableAnimation.value()), false);

                    eachX += eachWidth;

                    if (settingCount >= 5 && settingCount % 5 == 0) {
                        eachX = boxX + 5f;
                        eachY += basicSpace;
                    }

                    settingPosition++;
                }
            }

            if (value instanceof ListValue) {
                ((ListValue) value).animatingNumberWidth.animate();
                ((ListValue) value).animatingNumberX.animate();
                ((ListValue) value).animatingNumberY.animate();

                ((ListValue) value).animatingNumberWidth.number = FontManager.BoldPingFang.getStringWidth((String) value.value, 12f);

                ((ListValue) value).silentAnimation.update(module.animatingNumber.animatingNumber != module.animatingNumber.number ? AnimationState.FORWARDS : AnimationState.BACKWARDS);

                float boxX = settingX, boxY = settingY, boxWidth = backgroundWidth - modWidthSpace * 2f, boxHeight = getSettingHeight(value), boxRadius = 5f;

                if (((ListValue) value).silentAnimation.value() < 1f) {
                    RenderUtils.drawAppleRoundedRect(((ListValue) value).animatingNumberX.animatingNumber - 1f, ((ListValue) value).animatingNumberY.animatingNumber - 1f, ((ListValue) value).animatingNumberWidth.animatingNumber + 2f, 6f + 2f, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.15f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((ListValue) value).availableAnimation.value() * (1f - ((ListValue) value).silentAnimation.value())), 1f);
                }

                RenderUtils.drawAppleRoundedRect(boxX, boxY, boxWidth, boxHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.15f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((ListValue) value).availableAnimation.value()), boxRadius);

                FontManager.BoldPingFang.drawString(12f, value.name, boxX + 5f, boxY + 5f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((ListValue) value).availableAnimation.value()), false);

                float eachX = boxX + 5f, eachY = boxY + 15f;
                float maxCount = 5f;
                float eachWidth = (boxWidth - 10f) / maxCount, basicSpace = 10f;
                int settingCount = 0, settingPosition = 0;
                float controllableSpace = 1.5f;
                for (String mode : ((ListValue) value).getModes()) {
                    settingCount++;

                    ((ListValue) value).activatedAnimations.get(settingPosition).update(value.value.equals(mode) ? AnimationState.FORWARDS : AnimationState.BACKWARDS);
                    if (value.value.equals(mode)) {
                        ((ListValue) value).animatingNumberX.number = eachX + eachWidth / 2f - FontManager.BoldPingFang.getStringWidth(mode, 12f) / 2f;
                        ((ListValue) value).animatingNumberY.number = eachY;
                    }

                    if (MouseBehavior.mouseHoveredClickGUI(eachX + eachWidth / 2f - FontManager.BoldPingFang.getStringWidth(mode, 12f) / 2f - controllableSpace, eachY - controllableSpace, FontManager.BoldPingFang.getStringWidth(mode, 12f) + controllableSpace * 2f, basicSpace) && modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {
                        if (MouseBehavior.mouseLeftClicked() && !value.value.equals(mode) && MouseBehavior.mouseY > modY + modHeight + modSpace && MouseBehavior.mouseY < backgroundY + backgroundHeight) {
                            ((ListValue) value).set(mode);
                        }
                    }

                    FontManager.BoldPingFang.drawString(12f, mode, eachX + eachWidth / 2f - FontManager.BoldPingFang.getStringWidth(mode, 12f) / 2f, eachY, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * (0.325f + ((ListValue) value).activatedAnimations.get(settingPosition).value() * 0.325f) * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((ListValue) value).availableAnimation.value()), false);

                    eachX += eachWidth;

                    if (settingCount >= 5 && settingCount % 5 == 0) {
                        eachX = boxX + 5f;
                        eachY += basicSpace;
                    }

                    settingPosition++;
                }
            }

            if (value instanceof MultiBoolValue) {
                ((MultiBoolValue) value).animatingNumberWidth.animate();
                ((MultiBoolValue) value).animatingNumberX.animate();
                ((MultiBoolValue) value).animatingNumberY.animate();

                ((MultiBoolValue) value).animatingNumberWidth.number = FontManager.BoldPingFang.getStringWidth((String) value.value, 12f);

                ((MultiBoolValue) value).silentAnimation.update(module.animatingNumber.animatingNumber != module.animatingNumber.number ? AnimationState.FORWARDS : AnimationState.BACKWARDS);

                float boxX = settingX, boxY = settingY, boxWidth = backgroundWidth - modWidthSpace * 2f, boxHeight = getSettingHeight(value), boxRadius = 5f;

                RenderUtils.drawAppleRoundedRect(boxX, boxY, boxWidth, boxHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.15f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((MultiBoolValue) value).availableAnimation.value()), boxRadius);

                FontManager.BoldPingFang.drawString(12f, value.name, boxX + 5f, boxY + 5f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((MultiBoolValue) value).availableAnimation.value()), false);

                float eachX = boxX + 5f, eachY = boxY + 15f;
                float maxCount = 5f;
                float eachWidth = (boxWidth - 10f) / maxCount, basicSpace = 10f;
                int settingCount = 0, settingPosition = 0;
                float controllableSpace = 1.5f;

                for (BoolValue mode : ((MultiBoolValue) value).getModes()) {
                    settingCount++;


                    ((MultiBoolValue) value).activatedAnimations.get(settingPosition).update(mode.get() ? AnimationState.FORWARDS : AnimationState.BACKWARDS);

                    float animationValue = ((MultiBoolValue) value).activatedAnimations.get(settingPosition).value();


                    if (animationValue > 0f) {
                        float bgX = eachX + eachWidth / 2f - FontManager.BoldPingFang.getStringWidth(mode.getName(), 12f) / 2f - controllableSpace;
                        float bgY = eachY - controllableSpace;
                        float bgWidth = FontManager.BoldPingFang.getStringWidth(mode.getName(), 12f) + controllableSpace * 2f;
                        float bgHeight = 10f;

                        RenderUtils.drawAppleRoundedRect(bgX, bgY, bgWidth, bgHeight,
                                ColorPanel.createColorPanel(0f, 0f, 0f,
                                        animationAlpha * 0.15f *
                                                modCategory.getSwitchedAnimation().value() *
                                                modCategory.getSwitchedModSettingPageAnimation().value() *
                                                ((MultiBoolValue) value).availableAnimation.value() *
                                                animationValue
                                ), 3f);
                    }

                    ((MultiBoolValue) value).animatingNumberX.number = eachX + eachWidth / 2f - FontManager.BoldPingFang.getStringWidth(mode.getName(), 12f) / 2f;
                    ((MultiBoolValue) value).animatingNumberY.number = eachY;

                    if (MouseBehavior.mouseHoveredClickGUI(eachX + eachWidth / 2f - FontManager.BoldPingFang.getStringWidth(mode.getName(), 12f) / 2f - controllableSpace, eachY - controllableSpace, FontManager.BoldPingFang.getStringWidth(mode.getName(), 12f) + controllableSpace * 2f, basicSpace) && modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {
                        if (MouseBehavior.mouseLeftClicked() && MouseBehavior.mouseY > modY + modHeight + modSpace && MouseBehavior.mouseY < backgroundY + backgroundHeight) {
                            mode.set(!mode.get());
                        }
                    }

                    FontManager.BoldPingFang.drawString(12f, mode.getName(), eachX + eachWidth / 2f - FontManager.BoldPingFang.getStringWidth(mode.getName(), 12f) / 2f, eachY,
                            ColorPanel.createColorPanel(1f, 1f, 1f,
                                    animationAlpha * (0.325f + ((MultiBoolValue) value).activatedAnimations.get(settingPosition).value() * 0.325f) *
                                            modCategory.getSwitchedAnimation().value() *
                                            modCategory.getSwitchedModSettingPageAnimation().value() *
                                            ((MultiBoolValue) value).availableAnimation.value()),
                            false);

                    eachX += eachWidth;

                    if (settingCount >= 5 && settingCount % 5 == 0) {
                        eachX = boxX + 5f;
                        eachY += basicSpace;
                    }

                    settingPosition++;
                }
            }

            if (value instanceof KeyBindValue) {
                float boxX = settingX, boxY = settingY, boxWidth = backgroundWidth - modWidthSpace * 2f, boxHeight = getSettingHeight(value), boxRadius = 5f;

                RenderUtils.drawAppleRoundedRect(boxX, boxY, boxWidth, boxHeight, ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.15f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((KeyBindValue) value).availableAnimation.value()), boxRadius);

                FontManager.BoldPingFang.drawString(12f, value.name, boxX + 5f, boxY + 5f, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((KeyBindValue) value).availableAnimation.value()), false);

                String keyText;
                if (currentWaitingForBindValue == (KeyBindValue) value) {
                    keyText = "Press a key to bind...";
                } else {
                    if (((KeyBindValue) value).getKey() == 0) {
                        keyText = "None";
                    } else {
                        keyText = InputUtil.fromKeyCode(((KeyBindValue) value).getKey(), -1).getLocalizedText().getString();
                    }
                }

                float textWidth = FontManager.BoldPingFang.getStringWidth(keyText, 12f);
                float buttonX = boxX + boxWidth - textWidth - 10f;
                float buttonY = boxY + 5f;

                FontManager.BoldPingFang.drawString(12f, keyText, buttonX, buttonY, ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.65f * modCategory.getSwitchedAnimation().value() * modCategory.getSwitchedModSettingPageAnimation().value() * ((KeyBindValue) value).availableAnimation.value()), false);

                if (MouseBehavior.mouseHoveredClickGUI(boxX, boxY, boxWidth, boxHeight) && modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {
                    if (MouseBehavior.mouseLeftClicked() && !MouseBehavior.leftButtonClicked && MouseBehavior.mouseY > modY + modHeight + modSpace && MouseBehavior.mouseY < backgroundY + backgroundHeight) {
                        currentWaitingForBindValue = (KeyBindValue) value;
                    }
                    MouseBehavior.leftButtonClicked = MouseBehavior.mouseLeftClicked();
                }
            }
            settingY += (getSettingHeight(value) + settingSpace) * value.availableAnimation.value();
        }
        RenderHelper.scaleEnd();
        RenderHelper.scissorEnd();
    }

    private static double getSteppedValue(NumberValue numbervalue, float barX, float barWidth) {
        float mouseX = (float) (MouseBehavior.mouseX - barX);
        float newProgress = Math.max(0f, Math.min(1f, mouseX / barWidth));

        double rawValue = numbervalue.getMin() + (numbervalue.getMax() - numbervalue.getMin()) * newProgress;

        double steppedValue = numbervalue.getMin() + Math.round((rawValue - numbervalue.getMin()) / numbervalue.getInc()) * numbervalue.getInc();

        steppedValue = Math.max(numbervalue.getMin(), Math.min(numbervalue.getMax(), steppedValue));
        return steppedValue;
    }

    private static float getSettingHeight(Value<?> setting) {
        if (setting instanceof BoolValue) {
            return 15f;
        }

        if (setting instanceof NumberValue) {
            return 25f;
        }

        if (setting instanceof TextValue) {
            return 28f;
        }

        if (setting instanceof ColorValue) {
            return 91f;
        }

        if (setting instanceof ListValue ) {
            float basicHeight = 25f, basicSpace = 10f;
            int settingCount = 0;
            for (String ignored : ((ListValue) setting).getModes()) {
                settingCount++;

                if (settingCount > 5 && settingCount % 5 == 1) {
                    basicHeight += basicSpace;
                }
            }
            return basicHeight;
        }

        if (setting instanceof BindValue ) {
            float basicHeight = 25f, basicSpace = 10f;
            int settingCount = 0;
            for (String ignored : ((BindValue) setting).getModes()) {
                settingCount++;

                if (settingCount > 5 && settingCount % 5 == 1) {
                    basicHeight += basicSpace;
                }
            }
            return basicHeight;
        }

        if (setting instanceof MultiBoolValue) {
            float basicHeight = 25f, basicSpace = 10f;
            int settingCount = 0;
            for (BoolValue ignored : ((MultiBoolValue) setting).getModes()) {
                settingCount++;

                if (settingCount > 5 && settingCount % 5 == 1) {
                    basicHeight += basicSpace;
                }
            }
            return basicHeight;
        }

        if (setting instanceof KeyBindValue) {
            return 25f;
        }
        return 0f;
    }

    private static float getModuleAllValueHeight(Module module) {
        if (module.getValues().isEmpty()) {
            return 0f;
        }

        float allValueHeight = 0f;
        float valueSpace = 5f;

        for (Value<?> value : module.getValues()) {
            if (value.availableAnimation.value() <= 0f) {
                continue;
            }

            allValueHeight += (getSettingHeight(value) + valueSpace) * value.availableAnimation.value();
        }

        return allValueHeight;
    }

    private static void onModSettingScrollAction(Module module) {
        float scrollHeight = 25f;

        float valueHeight = 25f, valueSpace = 5f;
        float maxValueHeightInPage = 7f * (valueHeight + valueSpace);

        if (MouseBehavior.mouseScrollValue > 0f) {
            module.animatingNumber.number -= scrollHeight;
            MouseBehavior.mouseScrollValue = 0f;
        }
        if (MouseBehavior.mouseScrollValue < 0f) {
            module.animatingNumber.number += scrollHeight;
            MouseBehavior.mouseScrollValue = 0f;
        }

        if (module.animatingNumber.number > 0f) {
            module.animatingNumber.number = 0f;
        }
        if (getModuleAllValueHeight(module) <= maxValueHeightInPage) {
            module.animatingNumber.number = 0f;
        }
        if (getModuleAllValueHeight(module) > maxValueHeightInPage) {
            if (module.animatingNumber.number < -getModuleAllValueHeight(module) + maxValueHeightInPage) {
                module.animatingNumber.number = -getModuleAllValueHeight(module) + maxValueHeightInPage;
            }
        }

        module.animatingNumber.animate();
    }

    private static void onModCategoryScrollAction(ModuleCategory modCategory) {
        float modHeight = 25f, modSpace = 5f;
        float maxModCount = 8f, maxModHeightInPage = maxModCount * (modHeight + modSpace);

        List<Module> modulesInCategory = Client.moduleManager.getModules().stream()
                .filter(module -> module.getCategory() == modCategory)
                .toList();
        int modCount = modulesInCategory.size();
        float allModHeight = (float) Math.ceil(modCount / 2f) * (modHeight + modSpace);

        if (MouseBehavior.mouseScrollValue > 0f) {
            modCategory.animatingNumber.number -= modHeight + modSpace;
            MouseBehavior.mouseScrollValue = 0f;
        }
        if (MouseBehavior.mouseScrollValue < 0f) {
            modCategory.animatingNumber.number += modHeight + modSpace;
            MouseBehavior.mouseScrollValue = 0f;
        }

        if (modCategory.animatingNumber.number > 0f) {
            modCategory.animatingNumber.number = 0f;
        }
        if (allModHeight <= maxModHeightInPage) {
            modCategory.animatingNumber.number = 0f;
        }
        if (allModHeight > maxModHeightInPage) {
            if (modCategory.animatingNumber.number < -allModHeight + maxModHeightInPage) {
                modCategory.animatingNumber.number = -allModHeight + maxModHeightInPage;
            }
        }

        modCategory.animatingNumber.animate();
    }

    public static void handleKeyInput(int keyCode) {
        if (currentFocusedTextValue != null) {
            lastKeyPressTime = System.currentTimeMillis();

            if (keyCode == 256) {
                currentFocusedTextValue = null;
                return;
            }

            if (keyCode == 257) {
                currentFocusedTextValue = null;
                return;
            }

            if (keyCode == 259) {
                if (!currentFocusedTextValue.value.isEmpty()) {
                    currentFocusedTextValue.value = currentFocusedTextValue.value.substring(0, currentFocusedTextValue.value.length() - 1);
                }
                return;
            }
        }


        if (currentWaitingForBindValue != null) {
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                currentWaitingForBindValue.setKey(keyCode);
            }
            currentWaitingForBindValue = null;
        }
    }

    public static void handleCharInput(char character) {
        if (currentFocusedTextValue != null) {

            if (character >= ' ' && character != 127) {
                currentFocusedTextValue.value += character;
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void drawTransparencyGrid(float x, float y, float width, float height, float gridSize, float animationAlpha) {
        boolean white = true;

        for (float gridY = y; gridY < y + height; gridY += gridSize) {
            for (float gridX = x; gridX < x + width; gridX += gridSize) {
                ColorPanel gridColor = white ?
                        ColorPanel.createColorPanel(1f, 1f, 1f, animationAlpha * 0.1f) :
                        ColorPanel.createColorPanel(0.8f, 0.8f, 0.8f, animationAlpha * 0.1f);

                RenderUtils.drawRect(gridX, gridY, gridSize, gridSize, gridColor);
                white = !white;
            }
            white = !white;
        }
    }

    private static void drawSaturationBrightnessPicker(float x, float y, float size, ColorValue colorValue, float animationAlpha, ModuleCategory modCategory) {
        Color currentColor = colorValue.getColor();
        float[] hsv = ColorUtils.rgbToHsv(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue());
        float currentHue = hsv[0];

        for (float px = 0; px < size; px++) {
            for (float py = 0; py < size; py++) {
                float saturation = px / size;
                float brightness = 1f - (py / size);

                Color hsvColor = hsvToRgb(currentHue, saturation, brightness);
                float pixelX = x + px;
                float pixelY = y + py;

                RenderUtils.drawRect(pixelX, pixelY, 1f, 1f,
                        ColorPanel.createColorPanel(hsvColor.getRed() / 255f, hsvColor.getGreen() / 255f, hsvColor.getBlue() / 255f, animationAlpha));
            }
        }

        float cursorX = x + hsv[1] * size;
        float cursorY = y + (1f - hsv[2]) * size;
        float cursorSize = 2f;

        cursorX = Math.max(x + cursorSize, Math.min(x + size - cursorSize, cursorX));
        cursorY = Math.max(y + cursorSize, Math.min(y + size - cursorSize, cursorY));

        float cursorBrightness = (currentColor.getRed() * 0.299f + currentColor.getGreen() * 0.587f + currentColor.getBlue() * 0.114f) / 255f;
        Color cursorColor = cursorBrightness > 0.5f ? Color.BLACK : Color.WHITE;

        RenderUtils.drawCircle(cursorX, cursorY, cursorSize,
                ColorPanel.createColorPanel(cursorColor.getRed() / 255f, cursorColor.getGreen() / 255f, cursorColor.getBlue() / 255f, 0.75f * animation.value()));
        RenderUtils.drawCircleOutline(cursorX, cursorY, cursorSize - 1f,
                ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f * animation.value()), 1f);

        float controllableSpace = 2f;
        if (MouseBehavior.mouseHoveredClickGUI(x - controllableSpace, y - controllableSpace,
                size + controllableSpace * 2f, size + controllableSpace * 2f) &&
                modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {

            if (MouseBehavior.mouseLeftClicked()) {
                float mouseX = (float) (MouseBehavior.mouseX - x);
                float mouseY = (float) (MouseBehavior.mouseY - y);

                if (mouseX >= 0 && mouseX <= size && mouseY >= 0 && mouseY <= size) {
                    float saturation = Math.max(0f, Math.min(1f, mouseX / size));
                    float brightness = 1f - Math.max(0f, Math.min(1f, mouseY / size));

                    Color newColor = new Color(
                            hsvToRgb(currentHue, saturation, brightness).getRGB(),
                            false
                    );
                    newColor = new Color(
                            newColor.getRed(),
                            newColor.getGreen(),
                            newColor.getBlue(),
                            currentColor.getAlpha()
                    );
                    colorValue.setValue(newColor.getRGB());

                    MouseBehavior.leftButtonClicked = true;
                }
            }
        }
    }

    private static void drawColorPicker(float x, float y, float size, float radius, ColorValue colorValue, float animationAlpha, ModuleCategory modCategory) {
        float mainPickerSize = size - 40f;
        float hueBarWidth = 15f;
        float alphaBarWidth = 15f;
        float hueBarX = x + mainPickerSize + 5f;
        float alphaBarX = hueBarX + hueBarWidth + 5f;

        drawSaturationBrightnessPicker(x, y, mainPickerSize, colorValue, animationAlpha, modCategory);

        drawHueBar(hueBarX, y, hueBarWidth, mainPickerSize, radius, colorValue, animationAlpha, modCategory);

        drawAlphaBar(alphaBarX, y, alphaBarWidth, mainPickerSize, colorValue, animationAlpha, modCategory);
    }

    private static void drawAlphaBar(float x, float y, float width, float height, ColorValue colorValue, float animationAlpha, ModuleCategory modCategory) {
        RenderUtils.drawAppleRoundedRect(x, y, width, height,
                ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.3f), 0f);

        Color currentColor = colorValue.getColor();
        int segments = 20;
        float segmentHeight = height / segments;

        Color opaqueColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 255);

        for (int i = 0; i < segments; i++) {
            float segY = y + i * segmentHeight;
            float alpha = (float) i / segments;

            Color alphaColor = new Color(
                    opaqueColor.getRed(),
                    opaqueColor.getGreen(),
                    opaqueColor.getBlue(),
                    (int) (alpha * 255)
            );

            RenderUtils.drawRect(x, segY, width, segmentHeight,
                    ColorPanel.createColorPanel(
                            alphaColor.getRed() / 255f,
                            alphaColor.getGreen() / 255f,
                            alphaColor.getBlue() / 255f,
                            alphaColor.getAlpha() / 255f * animationAlpha * animation.value()
                    ));
        }

        float currentAlpha = currentColor.getAlpha() / 255f;
        float cursorY = y + currentAlpha * height;
        float cursorSize = 4f;

        cursorY = Math.max(y + cursorSize / 2, Math.min(y + height - cursorSize / 2, cursorY));

        RenderUtils.drawRoundedRect(x, cursorY, width, 2, ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f * animation.value()), 1f);

        float controllableSpace = 5f;
        if (MouseBehavior.mouseHoveredClickGUI(x - controllableSpace, y - controllableSpace,
                width + controllableSpace * 2f, height + controllableSpace * 2f) &&
                modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {

            if (MouseBehavior.mouseLeftClicked()) {
                float mouseY = (float) (MouseBehavior.mouseY - y);
                float newAlpha = Math.max(0f, Math.min(1f, mouseY / height));

                Color newColor = new Color(
                        currentColor.getRed(),
                        currentColor.getGreen(),
                        currentColor.getBlue(),
                        (int) (newAlpha * 255)
                );
                colorValue.setValue(newColor.getRGB());

                MouseBehavior.leftButtonClicked = true;
            }
        }
    }

    private static void drawHueBar(float x, float y, float width, float height, float radius, ColorValue colorValue, float animationAlpha, ModuleCategory modCategory) {
        RenderUtils.drawAppleRoundedRect(x, y, width, height,
                ColorPanel.createColorPanel(0f, 0f, 0f, animationAlpha * 0.3f), radius / 2f);

        int segments = 20;
        float segmentHeight = height / segments;

        for (int i = 0; i < segments; i++) {
            float segY = y + i * segmentHeight;
            float hue = (float) i / segments;

            Color hueColor = hsvToRgb(hue, 1f, 1f);
            RenderUtils.drawRect(x, segY, width, segmentHeight,
                    ColorPanel.createColorPanel(hueColor.getRed() / 255f, hueColor.getGreen() / 255f, hueColor.getBlue() / 255f, animationAlpha));
        }

        Color currentColor = colorValue.getColor();
        float[] hsv = ColorUtils.rgbToHsv(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue());
        float currentHue = hsv[0];

        float cursorY = y + currentHue * height;
        float cursorSize = 4f;

        cursorY = Math.max(y + cursorSize / 2, Math.min(y + height - cursorSize / 2, cursorY));

        RenderUtils.drawRoundedRect(x, cursorY, width, 2f, ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f * animation.value()), 1f);

        float controllableSpace = 5f;
        if (MouseBehavior.mouseHoveredClickGUI(x - controllableSpace, y - controllableSpace,
                width + controllableSpace * 2f, height + controllableSpace * 2f) &&
                modCategory.getSwitchedModSettingPageAnimation().value() >= 1f) {

            if (MouseBehavior.mouseLeftClicked()) {
                float mouseY = (float) (MouseBehavior.mouseY - y);
                float newHue = Math.max(0f, Math.min(1f, mouseY / height));

                Color newColor = new Color(
                        hsvToRgb(newHue, hsv[1], hsv[2]).getRGB(),
                        false
                );
                newColor = new Color(
                        newColor.getRed(),
                        newColor.getGreen(),
                        newColor.getBlue(),
                        currentColor.getAlpha()
                );
                colorValue.setValue(newColor.getRGB());

                MouseBehavior.leftButtonClicked = true;
            }
        }
    }
}