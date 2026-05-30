package missu.epsilon.client.features.modules.visual;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.ingameui.Dragging;
import missu.epsilon.client.sxmurxy.builders.Builder;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.BlurTaskInstance;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltBlur;
import missu.epsilon.client.utils.animations.Animation;
import missu.epsilon.client.utils.animations.ContinualAnimation;
import missu.epsilon.client.utils.animations.Direction;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.animations.impl.EaseInOutQuad;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import missu.epsilon.client.utils.render.font.Icon;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.*;

@ModuleInfo(name = "PotionHUD", description = "Show active potion effects on your screen", category = ModuleCategory.VISUAL)
public class PotionHUD extends Module {

    private final Dragging dragging = Client.createDrag(this, "PotionHud", 5, 120);
    private final ListValue mode = new ListValue("Render Mode", new String[]{"Progress", "Text"}, "Text");

    private final List<StatusEffectInstance> activeEffects = new ArrayList<>();
    private final Map<String, EffectData> effectDataMap = new HashMap<>();
    private final List<StatusEffectInstance> pendingRemoval = new ArrayList<>();
    private final ContinualAnimation headerAnimation = new ContinualAnimation();
    private static final float PADDING = 2f;
    private static final float SMOOTHNESS = 5f;

    @SuppressWarnings("unused")
    @EventTarget
    public void onRenderScreen(RenderNvgEvent event) {
        if (mc.player == null) return;
        float x = dragging.getXPos(), y = dragging.getYPos();
        dragging.setWidth(50);
        dragging.setHeight(50);
        boolean isOnRightSide = x > (float) mc.getWindow().getScaledWidth() / 2;
        if (isOnRightSide) {
            renderRightSideUI(event, x, y);
        } else {
            renderLeftSideUI(event, x, y);
        }
    }

    private void renderRightSideUI(RenderNvgEvent event, float x, float y) {
        float maxWidth = calculateMaxWidth();
        renderHeader(event, x + maxWidth - PADDING, y - PADDING, maxWidth, true);
        float offset = 15f;
        for (StatusEffectInstance effect : activeEffects) {
            EffectData data = getEffectData(effect);
            if (shouldSkipEffect(data)) continue;
            float effectX = calculateEffectX(data, x, true);
            renderEffect(event, effect, data, effectX, y + offset, true);
            offset += (float) (17f * data.animation.getOutput());
        }
    }

    private void renderLeftSideUI(RenderNvgEvent event, float x, float y) {
        float maxWidth = calculateMaxWidth();
        renderHeader(event, x - PADDING, y - PADDING, maxWidth, false);
        float offset = 15f;
        for (StatusEffectInstance effect : activeEffects) {
            EffectData data = getEffectData(effect);
            if (shouldSkipEffect(data)) continue;
            float effectX = calculateEffectX(data, x, false);
            renderEffect(event, effect, data, effectX, y + offset, false);
            offset += (float) (17f * data.animation.getOutput());
        }
    }

    private void renderHeader(RenderNvgEvent event, float blurX, float blurY, float maxWidth, boolean isRightSide) {
        renderBlur(event, blurX + (isRightSide ? 4f : 2f), blurY, 15f);
        RenderUtils.drawAppleRoundedRect(blurX + (isRightSide ? 4f : 2f), blurY, 15f, 15f, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
        headerAnimation.animate(maxWidth, 60);
        float animatedWidth = headerAnimation.getOutput();
        float textBlurX = isRightSide ? (blurX - animatedWidth - PotionHUD.PADDING) + 4f : (blurX + 21f - PotionHUD.PADDING);
        renderBlur(event, textBlurX, blurY, animatedWidth);
        float textBgX = isRightSide ? (blurX - animatedWidth) + 1f : (blurX + 19f);
        RenderUtils.drawAppleRoundedRect(textBgX, blurY, animatedWidth, 15f, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
        float iconX = isRightSide ? (blurX + 8f) : (blurX + 6f);
        float textX = isRightSide ? (blurX - FontManager.BoldPingFang.getStringWidth("Potion Effects", 16)) - 2 : (blurX + 22f);
        FontManager.FilledMaterial.drawGlowString(22f, Icon.SCIENCE, iconX - 2, blurY + 2.5f, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
        FontManager.BoldPingFang.drawGlowString(16f, "Potion Effects", textX, blurY + 4f, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
    }

    private void renderEffect(RenderNvgEvent event, StatusEffectInstance effect, EffectData data, float x, float y, boolean isRightSide) {
        String effectName = getEffectDisplayName(effect);
        String text = effectName;
        if (mode.is("Text")) {
            text = effectName + " " + formatDuration(effect.getDuration());
        }
        float effectWidth = FontManager.BoldPingFang.getStringWidth(text, 16f) + 6f;
        float progress = calculateProgress(effect, data);
        data.progressAnimation.animate(effectWidth * progress, 30);
        if (isRightSide) {
            renderRightSideEffect(event, effect, data, x, y, effectName, effectWidth);
        } else {
            renderLeftSideEffect(event, effect, data, x, y, effectName, effectWidth);
        }
    }

    private void renderRightSideEffect(RenderNvgEvent event, StatusEffectInstance effect, EffectData data, float x, float y, String effectName, float effectWidth) {
        float maxWidth = calculateMaxWidth();
        renderBlur(event, x + maxWidth + 2f, y, 15f);
        RenderUtils.drawAppleRoundedRect(x + maxWidth + 2f, y, 15f, 15f, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
        renderBlur(event, x + maxWidth - effectWidth, y, effectWidth);
        RenderUtils.drawAppleRoundedRect(x + maxWidth - effectWidth, y, effectWidth, 15f, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
        if (mode.is("Progress")) {
            RenderUtils.drawAppleRoundedRect(x + maxWidth - effectWidth, y, Math.max(15f, data.progressAnimation.getOutput()), 15f, getEffectColor(effect), 5f);
        }
        FontManager.FilledMaterial.drawGlowString(22f, getEffectIcon(effect), x + maxWidth + 4f, y + 3f, getEffectColor(effect), ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), false, 5f);
        String text;
        if (mode.is("Text")) {
            text = effectName + " " + formatDuration(effect.getDuration());
        } else {
            text = effectName;
        }
        FontManager.BoldPingFang.drawGlowString(16f, text, x + maxWidth - FontManager.BoldPingFang.getStringWidth(text, 16f) - 3f, y + 4f, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
    }

    private void renderLeftSideEffect(RenderNvgEvent event, StatusEffectInstance effect, EffectData data, float x, float y, String effectName, float effectWidth) {
        renderBlur(event, x, y, 15);
        RenderUtils.drawAppleRoundedRect(x, y, 15, 15, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
        renderBlur(event, x + 17f, y, effectWidth);
        RenderUtils.drawAppleRoundedRect(x + 17f, y, effectWidth, 15, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
        if (mode.is("Progress")) {
            RenderUtils.drawAppleRoundedRect(x + 17f, y, Math.max(15f, data.progressAnimation.getOutput()), 15f, getEffectColor(effect), 5f);
        }
        String text;
        if (mode.is("Text")) {
            text = effectName + " " + formatDuration(effect.getDuration());
        } else {
            text = effectName;
        }
        FontManager.FilledMaterial.drawGlowString(22f, getEffectIcon(effect), x + 2f, y + 3f, getEffectColor(effect), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
        FontManager.BoldPingFang.drawGlowString(16f, text, x + 20f, y + 4f, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player == null) return;

        Collection<StatusEffectInstance> currentEffects = mc.player.getStatusEffects();
        updateEffects(currentEffects);
        cleanupEffects();
        sortEffects();
    }

    private static String formatDuration(int ticks) {
        int seconds = ticks / 20;
        if (seconds < 60) {
            return seconds + "s";
        } else {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + "m " + (remainingSeconds < 10 ? "0" : "") + remainingSeconds + "s";
        }
    }

    private void updateEffects(Collection<StatusEffectInstance> currentEffects) {
        Set<String> currentEffectKeys = new HashSet<>();
        for (StatusEffectInstance currentEffect : currentEffects) {
            if (currentEffect.getDuration() <= 0) continue;
            if (currentEffect.getEffectType() == StatusEffects.NIGHT_VISION) {
                continue;
            }
            String effectKey = getEffectKey(currentEffect);
            currentEffectKeys.add(effectKey);
            StatusEffectInstance existingEffect = findExistingEffect(effectKey);
            if (existingEffect != null) {
                updateExistingEffect(existingEffect, currentEffect);
            } else if (!isPendingRemoval(effectKey)) {
                addNewEffect(currentEffect);
            }
        }
        for (StatusEffectInstance effect : new ArrayList<>(activeEffects)) {
            String effectKey = getEffectKey(effect);
            boolean shouldRemove = !currentEffectKeys.contains(effectKey) || effect.getDuration() <= 0;
            if (shouldRemove && !pendingRemoval.contains(effect)) {
                markEffectForRemoval(effect);
            }
        }
    }

    private StatusEffectInstance findExistingEffect(String effectKey) {
        for (StatusEffectInstance effect : activeEffects) {
            if (getEffectKey(effect).equals(effectKey)) {
                return effect;
            }
        }
        return null;
    }

    private void updateExistingEffect(StatusEffectInstance existingEffect, StatusEffectInstance currentEffect) {
        String effectKey = getEffectKey(existingEffect);
        EffectData data = effectDataMap.get(effectKey);
        if (data != null) {
            if (currentEffect.getDuration() > data.originalDuration) {
                data.originalDuration = currentEffect.getDuration();
            }
            activeEffects.remove(existingEffect);
            activeEffects.add(currentEffect);
            effectDataMap.remove(effectKey);
            effectDataMap.put(getEffectKey(currentEffect), data);
            pendingRemoval.removeIf(e -> getEffectKey(e).equals(effectKey));
        }
    }

    private void cleanupEffects() {
        Iterator<StatusEffectInstance> removalIterator = pendingRemoval.iterator();
        while (removalIterator.hasNext()) {
            StatusEffectInstance effect = removalIterator.next();
            EffectData data = effectDataMap.get(getEffectKey(effect));
            if (data != null && data.animation.isDone()) {
                activeEffects.remove(effect);
                effectDataMap.remove(getEffectKey(effect));
                removalIterator.remove();
            }
        }
    }

    private void sortEffects() {
        if (mode.is("Text")) {
            activeEffects.sort((e1, e2) -> Float.compare(FontManager.BoldPingFang.getStringWidth(getEffectDisplayName(e2) + " " + formatDuration(e2.getDuration()), 16f), FontManager.BoldPingFang.getStringWidth(getEffectDisplayName(e1) + " " + formatDuration(e1.getDuration()), 16f)));
        } else {
            activeEffects.sort((e1, e2) -> Float.compare(FontManager.BoldPingFang.getStringWidth(getEffectDisplayName(e2), 16f), FontManager.BoldPingFang.getStringWidth(getEffectDisplayName(e1), 16f)));
        }
    }

    private float calculateMaxWidth() {
        return FontManager.BoldPingFang.getStringWidth("Potion Effects", 16f) + 6f;
    }

    private float calculateEffectX(EffectData data, float baseX, boolean isRightSide) {
        if (data.animation.getDirection() == Direction.FORWARDS) {
            float startX = isRightSide ? mc.getWindow().getScaledWidth() : -calculateMaxWidth() - 50;
            return (float) (startX + (baseX - startX) * data.animation.getOutput());
        } else {
            float currentX = data.targetX;
            float endX = isRightSide ? mc.getWindow().getScaledWidth() : -calculateMaxWidth() - 50;
            return (float) (currentX + (endX - currentX) * (1 - data.animation.getOutput()));
        }
    }

    private float calculateProgress(StatusEffectInstance effect, EffectData data) {
        StatusEffectInstance currentEffect = getCurrentEffectInstance(effect);
        if (currentEffect != null) {
            return Math.max(0f, Math.min(1f, (float) currentEffect.getDuration() / data.originalDuration));
        }
        return Math.max(0f, Math.min(1f, (float) effect.getDuration() / data.originalDuration));
    }

    private StatusEffectInstance getCurrentEffectInstance(StatusEffectInstance effect) {
        if (mc.player == null) {
            return effect;
        }
        String targetKey = getEffectKey(effect);
        for (StatusEffectInstance currentEffect : mc.player.getStatusEffects()) {
            if (getEffectKey(currentEffect).equals(targetKey)) {
                return currentEffect;
            }
        }
        return effect;
    }

    private boolean shouldSkipEffect(EffectData data) {
        return data.animation.getDirection() == Direction.BACKWARDS && data.animation.isDone();
    }

    private void addNewEffect(StatusEffectInstance effect) {
        Animation animation = new EaseInOutQuad(200, 1);
        animation.setDirection(Direction.FORWARDS);
        EffectData data = new EffectData();
        data.animation = animation;
        data.progressAnimation = new ContinualAnimation();
        data.originalDuration = effect.getDuration();
        data.targetX = dragging.getXPos();
        effectDataMap.put(getEffectKey(effect), data);
        activeEffects.add(effect);
    }

    private void markEffectForRemoval(StatusEffectInstance effect) {
        EffectData data = effectDataMap.get(getEffectKey(effect));
        if (data != null && data.animation.getDirection() != Direction.BACKWARDS) {
            data.animation.setDirection(Direction.BACKWARDS);
            pendingRemoval.add(effect);
        }
    }

    private EffectData getEffectData(StatusEffectInstance effect) {
        return effectDataMap.get(getEffectKey(effect));
    }

    private void renderBlur(RenderNvgEvent event, float x, float y, float width) {
        BuiltBlur blur = Builder.blur().size(new SizeState(width + SMOOTHNESS, 15f + SMOOTHNESS)).radius(new QuadRadiusState(4f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(SMOOTHNESS).color(QuadColorState.TRANSPARENT).position(new PositionState(x - SMOOTHNESS / 2f, y - SMOOTHNESS / 2f)).matrix4f(event.matrix4f()).build();
        BlurTaskInstance.addTask(blur);
    }

    private String getEffectKey(StatusEffectInstance effect) {
        StatusEffect type = effect.getEffectType().value();
        return type.getTranslationKey() + ":" + effect.getAmplifier();
    }

    private boolean isPendingRemoval(String effectKey) {
        return pendingRemoval.stream().anyMatch(effect -> getEffectKey(effect).equals(effectKey));
    }

    private String getEffectDisplayName(StatusEffectInstance effect) {
        String name = effect.getEffectType().value().getName().getString();
        int amplifier = effect.getAmplifier();
        if (amplifier > 0) {
            name += " " + getRomanNumber(amplifier + 1);
        }
        return name;
    }

    private String getEffectIcon(StatusEffectInstance effect) {
        String type = effect.getEffectType().value().getTranslationKey().toLowerCase().replace(" ", "_");
        if (type.contains("speed") || type.contains("slowness")) {
            return "\uE9E4";
        } else if (type.contains("haste")) {
            return "\uE566";
        } else if (type.contains("strength")) {
            return "\uF6E6";
        } else if (type.contains("mining_fatigue") || type.contains("weakness")) {
            return "\uEB69";
        } else if (type.contains("muscle")) {
            return "\uF6E6";
        } else if (type.contains("instant_health") || type.contains("regeneration")) {
            return "\uF6E2";
        } else if (type.contains("instant_damage")) {
            return "\uE09C";
        } else if (type.contains("jump_boost")) {
            return "\uEACF";
        } else if (type.contains("nausea") || type.contains("hunger") || type.contains("saturation")) {
            return "\uE0F1";
        } else if (type.contains("fire_resistance")) {
            return "\uF16D";
        } else if (type.contains("resistance")) {
            return "\uE32A";
        } else if (type.contains("water_breathing") || type.contains("oozing") || type.contains("conduit_power") || type.contains("dolphins_grace")) {
            return "\uF084";
        } else if (type.contains("invisibility") || type.contains("darkness")) {
            return "\uE852";
        } else if (type.contains("blindness")) {
            return "\uE8F5";
        } else if (type.contains("night_vision")) {
            return "\uE8F4";
        } else if (type.contains("poison") || type.contains("wither")) {
            return "\uF89A";
        } else if (type.contains("health_boost")) {
            return "\uE1D5";
        } else if (type.contains("absorption")) {
            return "\uE9E0";
        } else if (type.contains("glowing")) {
            return "\uE0F0";
        } else if (type.contains("levitation") || type.contains("slow_falling")) {
            return "\uF555";
        } else if (type.contains("luck") || type.contains("hero_of_the_village")) {
            return "\uE8DC";
        } else if (type.contains("unluck") || type.contains("bad_omen")) {
            return "\uE8DB";
        } else if (type.contains("trial_omen") || type.contains("raid_omen")) {
            return "\uF889";
        } else if (type.contains("wind_charged")) {
            return "\uEFD8";
        } else if (type.contains("weaving")) {
            return "\uF20A";
        } else if (type.contains("infested")) {
            return "\uF0FA";
        }
        return "\uE2EB";
    }

    private ColorPanel getEffectColor(StatusEffectInstance effect) {
        StatusEffect type = effect.getEffectType().value();
        int color = type.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        return ColorPanel.createColorPanel(r, g, b, 0.75f);
    }

    private static String getRomanNumber(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }

    private static class EffectData {
        public Animation animation;
        public ContinualAnimation progressAnimation;
        public int originalDuration;
        public float targetX;
    }

}