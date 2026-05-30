package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.visual.PostProcessing;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
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
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ModuleInfo(name = "ESP", category = ModuleCategory.RENDER)
public class ESP extends Module {
    public static BoolValue twoD = new BoolValue("2D (OnlyPlayer)", true);
    public static BoolValue player = new BoolValue("Player", true);
    public static BoolValue heldItem = (BoolValue) new BoolValue("HeldItem", true).displayable(() -> twoD.get() && player.get());
    public static BoolValue health = (BoolValue) new BoolValue("Health", true).displayable(() -> twoD.get() && player.get());
    public static BoolValue potionEsp = (BoolValue) new BoolValue("Potion", true).displayable(() -> player.get());

    public static BoolValue glow = new BoolValue("Glow", true);
    public static MultiBoolValue glowaddons = (MultiBoolValue) new MultiBoolValue("Glow Addons", new BoolValue[]{
            new BoolValue("Player", true),
            new BoolValue("Item", true),
            new BoolValue("Mob", true),
            new BoolValue("Animals", true),
            new BoolValue("Arrows", true)
    }).displayable(glow::get);

    public static boolean shouldGlow(Entity entity) {
        ESP module = Client.moduleManager.getModule(ESP.class);
        if (!module.isEnabled()) {
            return false;
        } else if (entity instanceof PlayerEntity && glowaddons.get("Player")) {
            return true;
        } else if (entity instanceof ItemEntity && glowaddons.get("Item")) {
            return true;
        } else if (entity instanceof MobEntity && glowaddons.get("Mob")) {
            return true;
        } else {
            return entity instanceof AnimalEntity && glowaddons.get("Animals") || entity instanceof ArrowEntity && glowaddons.get("Arrows");
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onRenderScreen(RenderNvgEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }

        for (LivingEntity entity : mc.world.getPlayers()) {

            if (entity == mc.player && mc.options.getPerspective().isFirstPerson()) continue;

            if (entity.getUuid() == null) continue;

            if (entity.getDisplayName() == null) continue;

            if (entity.getDisplayName().getString().contains("CIT-")) continue;

            ScreenPosition entityScreenPosition = ScreenPositionUtils.createScreenLeftTopPosition(entity);
            ScreenPosition entityScreenWidthHeightPosition = ScreenPositionUtils.createScreenRightBottomPosition(entity);

            if (!entityScreenPosition.viewable) continue;

            float screenX = (float) entityScreenPosition.screenX;
            float screenY = (float) entityScreenPosition.screenY;

            float screenW = (float) (entityScreenWidthHeightPosition.screenX - entityScreenPosition.screenX);
            float screenH = (float) (entityScreenWidthHeightPosition.screenY - entityScreenPosition.screenY);

            onESPRender(entity, screenX, screenY, screenW, screenH);
            onPotionEsp(event.matrix4f(), entity, screenX, screenY + 4, screenW);
        }
    }

    private static void onESPRender(LivingEntity entity, float screenX, float screenY, float screenW, float screenH) {
        if (!twoD.get()) return;
        RenderUtils.drawOutlineRect(screenX, screenY, screenW, screenH, new ColorPanel(1, 1, 1, 1), 0.5F);
        RenderUtils.drawOutlineRect(screenX - 0.5F, screenY - 0.5F, screenW + 1F, screenH + 1F, new ColorPanel(0, 0, 0, 1), 0.5F);

        if (health.get() && (screenX + screenW) > 0) {
            float percent = entity.getHealth() / entity.getMaxHealth();
            RenderUtils.drawOutlineRect(screenX + screenW + 5F, screenY, 3F, screenH, new ColorPanel(1, 1, 1, 1), 0.5F);
            RenderUtils.drawOutlineRect(screenX + screenW + 4.5F, screenY - 0.5F, 4F, screenH + 1F, new ColorPanel(0, 0, 0, 1), 0.5F);
            float newHeight = (screenH - 1F) * percent;
            RenderUtils.drawRect(screenX + screenW + 5.5F, (screenY + 0.5F) + ((screenH - 1F) - newHeight), 2F, newHeight, new ColorPanel(0, 1, 0, 1));
        }

        if (heldItem.get()) {
            if (screenY + screenH <= 0) return;
            if (entity.getMainHandStack() != null && entity.getMainHandStack() != ItemStack.EMPTY) {
                float x1;
                x1 = screenX + screenW / 2 - FontManager.BoldPingFang.getStringWidth("[M]" + entity.getMainHandStack().getName().getString(), 15) / 2;
                FontManager.BoldPingFang.drawGlowString(15, "[M]" + entity.getMainHandStack().getName().getString(), x1, screenY + screenH + 5F, new ColorPanel(1, 1, 1, 1), ColorPanel.createColorPanel(0f, 0f, 0f, 0.35f), false, 5F);
                if (entity.getOffHandStack() != null && entity.getOffHandStack() != ItemStack.EMPTY) {
                    float x2;
                    x2 = screenX + screenW / 2 - FontManager.BoldPingFang.getStringWidth("[OF]" + entity.getOffHandStack().getName().getString(), 15) / 2;
                    FontManager.BoldPingFang.drawGlowString(15, "[OF]" + entity.getOffHandStack().getName().getString(), x2, screenY + screenH + 13F, new ColorPanel(1, 1, 1, 1), ColorPanel.createColorPanel(0f, 0f, 0f, 0.35f), false, 5F);
                }
            } else if (entity.getOffHandStack() != null && entity.getOffHandStack() != ItemStack.EMPTY) {
                float x;
                x = screenX + screenW / 2 - FontManager.BoldPingFang.getStringWidth("[OF]" + entity.getOffHandStack().getName().getString(), 15) / 2;
                FontManager.BoldPingFang.drawGlowString(15, "[OF]" + entity.getOffHandStack().getName().getString(), x, screenY + screenH + 5F, new ColorPanel(1, 1, 1, 1), ColorPanel.createColorPanel(0f, 0f, 0f, 0.35f), false, 5F);
            }
        }
    }

    private static void onPotionEsp(Matrix4f matrix4f, LivingEntity entity, float screenX, float screenY, float screenW) {
        if (!potionEsp.get() || !player.get()) return;

        Collection<StatusEffectInstance> currentEffects = entity.getStatusEffects();
        if (currentEffects.isEmpty()) return;

        List<StatusEffectInstance> effectsToRender = new ArrayList<>();
        List<Float> textWidths = new ArrayList<>();

        for (StatusEffectInstance currentEffect : currentEffects) {
            if (currentEffect.getDuration() <= 0) continue;

            if (currentEffect.getEffectType() == StatusEffects.NIGHT_VISION) {
                continue;
            }

            String effectName = getEffectDisplayName(currentEffect) + " " + formatDuration(currentEffect.getDuration());
            float textWidth = FontManager.BoldPingFang.getStringWidth(effectName, 16);

            effectsToRender.add(currentEffect);
            textWidths.add(textWidth);
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < effectsToRender.size(); i++) {
            indices.add(i);
        }
        indices.sort((o1, o2) -> Float.compare(textWidths.get(o2), textWidths.get(o1)));

        float startX = screenX + screenW + 5f + (health.get() ? 4f : 0f);
        float startY = screenY + 4f;
        float effectHeight = 15f;
        float padding = 2f;

        if ((screenX + screenW) <= 0) return;

        for (int i = 0; i < indices.size(); i++) {
            int index = indices.get(i);
            StatusEffectInstance currentEffect = effectsToRender.get(index);
            float effectY = startY + (effectHeight + padding) * i;
            renderPotionEffect(matrix4f, currentEffect, startX, effectY);
        }
    }

    private static void renderPotionEffect(Matrix4f matrix4f, StatusEffectInstance effect, float x, float y) {
        String effectName = getEffectDisplayName(effect) + " " + formatDuration(effect.getDuration());
        String effectIcon = getEffectIcon(effect);
        ColorPanel effectColor = getEffectColor(effect);

        float textWidth = FontManager.BoldPingFang.getStringWidth(effectName, 16);
        renderBlur(matrix4f, x + 19f, y, textWidth + 4f);
        RenderUtils.drawRoundedRect(x + 19f, y, textWidth + 4f, 15f,
                ColorPanel.createColorPanel(0f, 0f, 0f, 0.35f), 4f);

        renderBlur(matrix4f, x, y, 15f);
        RenderUtils.drawRoundedRect(x, y, 15f, 15f, ColorPanel.createColorPanel(0f, 0f, 0f, 0.35f), 4f);

        FontManager.FilledMaterial.drawGlowString(22, effectIcon, x + 2f, y + 3f,
                effectColor, ColorPanel.createColorPanel(0f, 0f, 0f, 025f), false, 3f);

        FontManager.BoldPingFang.drawGlowString(16, effectName, x + 21f, y + 4f,
                ColorPanel.createColorPanel(1f, 1f, 1f, 0.75f),
                ColorPanel.createColorPanel(0f, 0f, 0f, 025f), false, 5f);
    }

    private static void renderBlur(Matrix4f matrix4f, float x, float y, float width) {
        BuiltBlur blur = Builder.blur()
                .size(new SizeState(width, (float) 15))
                .radius(new QuadRadiusState(4f))
                .blurRadius(PostProcessing.blurStrength.get().floatValue())
                .smoothness(5f)
                .color(QuadColorState.TRANSPARENT)
                .position(new PositionState(x, y))
                .matrix4f(matrix4f)
                .build();
        BlurTaskInstance.addTask(blur);
    }

    private static String formatDuration(int ticks) {
        int seconds = ticks / 20;
        if (seconds < 60) {
            return seconds + "s";
        } else {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + ":" + (remainingSeconds < 10 ? "0" : "") + remainingSeconds;
        }
    }

    private static String getEffectDisplayName(StatusEffectInstance effect) {
        String name = effect.getEffectType().value().getName().getString();
        int amplifier = effect.getAmplifier();

        if (amplifier > 0) {
            name += " " + getRomanNumber(amplifier + 1);
        }

        return name;
    }

    private static String getRomanNumber(int number) {
        switch (number) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            default:
                return String.valueOf(number);
        }
    }

    private static String getEffectIcon(StatusEffectInstance effect) {
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

    private static ColorPanel getEffectColor(StatusEffectInstance effect) {
        StatusEffect type = effect.getEffectType().value();
        int color = type.getColor();

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        return ColorPanel.createColorPanel(r, g, b, 0.85f);
    }
}
