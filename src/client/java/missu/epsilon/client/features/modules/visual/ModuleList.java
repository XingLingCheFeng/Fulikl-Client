package missu.epsilon.client.features.modules.visual;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.ingameui.Dragging;
import missu.epsilon.client.sxmurxy.builders.Builder;
import missu.epsilon.client.sxmurxy.builders.states.PositionState;
import missu.epsilon.client.sxmurxy.builders.states.QuadColorState;
import missu.epsilon.client.sxmurxy.builders.states.QuadRadiusState;
import missu.epsilon.client.sxmurxy.builders.states.SizeState;
import missu.epsilon.client.sxmurxy.instance.BlurTaskInstance;
import missu.epsilon.client.sxmurxy.renderers.impl.BuiltBlur;
import missu.epsilon.client.utils.animations.ContinualAnimation;
import missu.epsilon.client.utils.animations.Direction;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.render.RenderUtils;
import missu.epsilon.client.utils.render.font.FontManager;
import missu.epsilon.client.utils.render.font.Icon;

import java.util.*;

@ModuleInfo(name = "ModuleList", description = "Show module arraylist on your screen", category = ModuleCategory.VISUAL)
public class ModuleList extends Module {
    public static BoolValue onlyImportant = new BoolValue("Only Important", true);
    public static BoolValue useModuleColor = new BoolValue("UseModuleColor",true);
    private final Dragging dragging = Client.createDrag(this, "ModuleList", 5, 38);
    private final List<Module> modules = new ArrayList<>();
    private final Map<Module, Float> moduleTargetX = new HashMap<>();
    private final List<Module> pendingRemoval = new ArrayList<>();

    private final ContinualAnimation animation = new ContinualAnimation();

    @SuppressWarnings("unused")
    @EventTarget
    public void onRenderScreen(RenderNvgEvent event) {
        float x = dragging.getXPos(), y = dragging.getYPos();
        dragging.setWidth(50);
        dragging.setHeight(50);
        float screenMiddle = (float) mc.getWindow().getScaledWidth() / 2;

        boolean isOnRightSide = x > screenMiddle;

        float smoothness = 5f;

        float titleWidth = (FontManager.BoldPingFang.getStringWidth("Module List", 16) + 6f);

        SizeState size = new SizeState(15 + smoothness, 15 + smoothness);
        if (isOnRightSide) {
            BuiltBlur blur = Builder.blur().size(size).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).color(QuadColorState.TRANSPARENT).position(new PositionState(x + titleWidth - smoothness / 2f + 2f, y - smoothness / 2f)).matrix4f(event.matrix4f()).build();
            BlurTaskInstance.addTask(blur);
            RenderUtils.drawAppleRoundedRect(x + titleWidth + 2f, y, 15, 15, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
            animation.animate(titleWidth, 60);
            BuiltBlur blur2 = Builder.blur().size(new SizeState(animation.getOutput() + smoothness, 15 + smoothness)).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).color(QuadColorState.TRANSPARENT).position(new PositionState((x + titleWidth) - animation.getOutput() - smoothness / 2f, y - smoothness / 2f)).matrix4f(event.matrix4f()).build();
            BlurTaskInstance.addTask(blur2);
            RenderUtils.drawAppleRoundedRect((x + titleWidth) - animation.getOutput(), y, animation.getOutput(), 15, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
            FontManager.FilledMaterial.drawGlowString(22, Icon.SAVE, x + titleWidth + 4f, y + 2.5f, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
            FontManager.BoldPingFang.drawGlowString(16, "Module List", x + titleWidth - FontManager.BoldPingFang.getStringWidth("Module List", 16) - 3f, y + 4f, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);

            float offset = 17;
            for (final Module module : this.modules) {
                if (module.moduleListAnimation.getDirection() == Direction.BACKWARDS && module.moduleListAnimation.isDone()) {
                    continue;
                }

                float moduleX;
                if (module.moduleListAnimation.getDirection() == Direction.FORWARDS) {
                    float startX = mc.getWindow().getScaledWidth();
                    moduleX = (float) (startX + (x - startX) * module.moduleListAnimation.getOutput());
                } else {
                    float currentX = moduleTargetX.getOrDefault(module, x);
                    float endX = mc.getWindow().getScaledWidth();
                    moduleX = (float) (currentX + (endX - currentX) * (1 - module.moduleListAnimation.getOutput()));
                }

                if (module.moduleListAnimation.getDirection() == Direction.FORWARDS) {
                    moduleTargetX.put(module, x);
                }

                BuiltBlur blur3 = Builder.blur().size(size).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).color(QuadColorState.TRANSPARENT).position(new PositionState((moduleX + titleWidth + 2) - smoothness / 2f, (y + offset) - smoothness / 2f)).matrix4f(event.matrix4f()).build();
                BlurTaskInstance.addTask(blur3);
                RenderUtils.drawAppleRoundedRect(moduleX + titleWidth + 2, y + offset, 15, 15, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);

                BuiltBlur blur4 = Builder.blur().size(new SizeState((FontManager.BoldPingFang.getStringWidth(module.getName(), 16) + 6f) + smoothness, 15 + smoothness)).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).color(QuadColorState.TRANSPARENT).position(new PositionState((moduleX + titleWidth - FontManager.BoldPingFang.getStringWidth(module.getName(), 16) - 6f) - smoothness / 2f, (y + offset) - smoothness / 2f)).matrix4f(event.matrix4f()).build();
                BlurTaskInstance.addTask(blur4);

                RenderUtils.drawAppleRoundedRect(moduleX + titleWidth - FontManager.BoldPingFang.getStringWidth(module.getName(), 16) - 6f, y + offset, FontManager.BoldPingFang.getStringWidth(module.getName(), 16) + 6f, 15, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);

                FontManager.FilledMaterial.drawGlowString(22, module.getCategory().getHtmlIcon(), moduleX + titleWidth + 4f, y + 3f + (offset), useModuleColor.get() ? module.getCategory().getColor() : ColorPanel.createColorPanel(1f, 1f, 1f, 1f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);

                FontManager.BoldPingFang.drawGlowString(16, module.getName(), moduleX + titleWidth - FontManager.BoldPingFang.getStringWidth(module.getName(), 16) - 3f, y + 4f + offset, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);

                float moduleHeight = (float) (17 * module.moduleListAnimation.getOutput());
                offset += moduleHeight;
            }
        } else {
            BuiltBlur blur = Builder.blur().size(size).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).color(QuadColorState.TRANSPARENT).position(new PositionState(x - smoothness / 2f, y - smoothness / 2f)).matrix4f(event.matrix4f()).build();
            BlurTaskInstance.addTask(blur);
            RenderUtils.drawAppleRoundedRect(x, y, 15, 15, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);

            animation.animate(titleWidth, 60);

            BuiltBlur blur2 = Builder.blur().size(new SizeState(animation.getOutput() + smoothness, 15 + smoothness)).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).color(QuadColorState.TRANSPARENT).position(new PositionState(x + 17 - smoothness / 2f, y - smoothness / 2f)).matrix4f(event.matrix4f()).build();
            BlurTaskInstance.addTask(blur2);

            RenderUtils.drawAppleRoundedRect(x + 17, y, animation.getOutput(), 15, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
            FontManager.FilledMaterial.drawGlowString(22, Icon.SAVE, x + 2f, y + 2.5f, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);
            FontManager.BoldPingFang.drawGlowString(16, "Module List", x + 20, y + 4f, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);

            float offset = 17;
            for (final Module module : this.modules) {
                if (module.moduleListAnimation.getDirection() == Direction.BACKWARDS && module.moduleListAnimation.isDone()) {
                    continue;
                }

                float moduleX;
                if (module.moduleListAnimation.getDirection() == Direction.FORWARDS) {
                    float startX = -titleWidth - 50;
                    moduleX = (float) (startX + (x - startX) * module.moduleListAnimation.getOutput());
                } else {
                    float currentX = moduleTargetX.getOrDefault(module, x);
                    float endX = -titleWidth - 50;
                    moduleX = (float) (currentX + (endX - currentX) * (1 - module.moduleListAnimation.getOutput()));
                }

                if (module.moduleListAnimation.getDirection() == Direction.FORWARDS) {
                    moduleTargetX.put(module, x);
                }

                BuiltBlur blur3 = Builder.blur().size(size).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).color(QuadColorState.TRANSPARENT).position(new PositionState(moduleX - smoothness / 2f, (y + offset) - smoothness / 2f)).matrix4f(event.matrix4f()).build();
                BlurTaskInstance.addTask(blur3);

                RenderUtils.drawAppleRoundedRect(moduleX, y + offset, 15, 15, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
                FontManager.FilledMaterial.drawGlowString(22, module.getCategory().getHtmlIcon(), moduleX + 2f, y + 3f + (offset), useModuleColor.get() ? module.getCategory().getColor() : ColorPanel.createColorPanel(1f, 1f, 1f, 1f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);

                BuiltBlur blur4 = Builder.blur().size(new SizeState((FontManager.BoldPingFang.getStringWidth(module.getName(), 16) + 6f) + smoothness, 15 + smoothness)).radius(new QuadRadiusState(5f)).blurRadius(PostProcessing.blurStrength.get().floatValue()).smoothness(smoothness).color(QuadColorState.TRANSPARENT).position(new PositionState(moduleX + 17f - smoothness / 2f, (y + offset) - smoothness / 2f)).matrix4f(event.matrix4f()).build();
                BlurTaskInstance.addTask(blur4);

                RenderUtils.drawAppleRoundedRect(moduleX + 17f, y + offset, FontManager.BoldPingFang.getStringWidth(module.getName(), 16) + 6f, 15, ColorPanel.createColorPanel(0f, 0f, 0f, 0.3f), 5f);
                FontManager.BoldPingFang.drawGlowString(16, module.getName(), moduleX + 20f, y + 4f + offset, ColorPanel.createColorPanel(1f, 1f, 1f, 0.9f), ColorPanel.createColorPanel(0f, 0f, 0f, 0.1f), false, 5f);

                float moduleHeight = (float) (17 * module.moduleListAnimation.getOutput());
                offset += moduleHeight;
            }
        }
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onTick(TickEvent event) {
        Iterator<Module> removalIterator = pendingRemoval.iterator();
        while (removalIterator.hasNext()) {
            Module module = removalIterator.next();
            if (module.moduleListAnimation.isDone()) {
                modules.remove(module);
                moduleTargetX.remove(module);
                removalIterator.remove();
            }
        }

        List<Module> updatedModules = Client.moduleManager.getModules().stream().filter(Module::isEnabled).filter(module -> !module.isHide()).filter(module -> !onlyImportant.get() || module.getCategory() == ModuleCategory.COMBAT || module.getCategory() == ModuleCategory.MOVEMENT || module.getCategory() == ModuleCategory.WORLD || module.getCategory() == ModuleCategory.EXPLOIT || module.getCategory() == ModuleCategory.PLAYER).filter(module -> !(module.getName().equalsIgnoreCase("ClickGUI"))).sorted(Comparator.comparing((Module module) -> FontManager.BoldPingFang.getStringWidth(module.getName(), 16)).reversed()).toList();

        for (Module module : updatedModules) {
            if (!this.modules.contains(module) && !pendingRemoval.contains(module)) {
                module.moduleListAnimation.setDirection(Direction.FORWARDS).reset();
                this.modules.add(module);
            }
        }

        for (Module module : this.modules) {
            if ((!module.isEnabled() || module.isHide()) && module.moduleListAnimation.getDirection() != Direction.BACKWARDS) {
                module.moduleListAnimation.setDirection(Direction.BACKWARDS).reset();
                if (!pendingRemoval.contains(module)) {
                    pendingRemoval.add(module);
                }
            }
        }

        modules.sort((m1, m2) -> {
            boolean m1Enabled = m1.isEnabled() && !m1.isHide();
            boolean m2Enabled = m2.isEnabled() && m2.isHide();

            if (!m1Enabled && m2Enabled) return 1;

            return Float.compare(
                    FontManager.BoldPingFang.getStringWidth(m2.getName(), 16),
                    FontManager.BoldPingFang.getStringWidth(m1.getName(), 16)
            );
        });
    }

}