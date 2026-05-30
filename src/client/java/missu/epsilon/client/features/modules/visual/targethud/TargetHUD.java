package missu.epsilon.client.features.modules.visual.targethud;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.combat.KillAura;
import missu.epsilon.client.features.modules.visual.targethud.renderer.TargetHudRenderer;
import missu.epsilon.client.ingameui.Dragging;
import missu.epsilon.client.utils.animations.Animation;
import missu.epsilon.client.utils.animations.Direction;
import missu.epsilon.client.utils.animations.impl.SmoothStepAnimation;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.LivingEntity;
import org.joml.Matrix4f;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "TargetHUD", description = "Display enemy's information and health existed", category = ModuleCategory.VISUAL)
public class TargetHUD extends Module {

    private final Dragging dragging = Client.createDrag(this, "TargetHud", 20, 55);
    public static Animation anim = new SmoothStepAnimation(250, 1f);

    private boolean lastDrawState = false;
    private LivingEntity lastTarget = null;

    @SuppressWarnings("unused")
    @EventTarget
    public void onRenderScreen(RenderNvgEvent event) {
        float x = dragging.getXPos(), y = dragging.getYPos();
        LivingEntity target = null;

        if (KillAura.currentTarget != null) {
            target = KillAura.currentTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            target = mc.player;
        }

        LivingEntity renderTarget = target != null ? target : lastTarget;

        if (renderTarget == null) return;

        boolean canDraw = KillAura.currentTarget != null || mc.currentScreen instanceof ChatScreen;
        float scaleValue = anim.getOutput().floatValue();

        doRender(scaleValue, x, y, event.matrix4f(), renderTarget, dragging);
    }

    public void doRender(float scaleValue, float x, float y, Matrix4f matrix4f, LivingEntity renderTarget, Dragging dragging) {
        TargetHudRenderer.render(scaleValue, x, y,matrix4f, renderTarget, dragging);
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onTick(TickEvent tickEvent) {
        boolean canDraw = KillAura.currentTarget != null || mc.currentScreen instanceof ChatScreen;
        LivingEntity currentTarget = null;

        if (KillAura.currentTarget != null) {
            currentTarget = KillAura.currentTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            currentTarget = mc.player;
        }

        boolean stateChanged = canDraw != lastDrawState;

        if (currentTarget != null) {
            lastTarget = currentTarget;
        }

        if (stateChanged) {
            anim.setDirection(canDraw ? Direction.FORWARDS : Direction.BACKWARDS);
        }

        lastDrawState = canDraw;
    }

}
