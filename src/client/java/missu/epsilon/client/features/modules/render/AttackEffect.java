package missu.epsilon.client.features.modules.render;

import missu.epsilon.client.event.events.game.AttackEvent;
import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.utils.animations.basic.screen.ScreenPosition;
import missu.epsilon.client.utils.animations.basic.screen.ScreenPositionUtils;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.miscs.RandomUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import missu.epsilon.client.utils.render.RenderUtils;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ModuleInfo(name = "AttackEffect", category = ModuleCategory.RENDER)
public class AttackEffect extends Module {
    public Map<Long, ScreenPosition> entityMap = new HashMap<>();
    public Map<Long, Identifier> idMap = new HashMap<>();
    public TimerUtils timer = new TimerUtils();
    public List<String> list;

    @EventTarget
    public void onAttack(AttackEvent event) {
        Long attackTime = System.currentTimeMillis();
        ScreenPosition entityScreenPosition = ScreenPositionUtils.createScreenPosition(event.getAttackedEntity());
        entityMap.put(attackTime, entityScreenPosition);
        int randomNum = RandomUtils.getRandom(1, 13);
        Identifier id = Identifier.of("epsilon", "icons/" + randomNum + ".png");
        idMap.put(attackTime, id);
    }

    @EventTarget
    public void onRenderScreen(RenderNvgEvent event) {
        if (ClientUtils.isNull()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        entityMap.entrySet().removeIf(entry -> currentTime - entry.getKey() > 3000);
        idMap.entrySet().removeIf(entry -> currentTime - entry.getKey() > 3000);

        entityMap.forEach((time, entity) -> {
            if (!entity.viewable) {
                return;
            }

            float screenX = (float) entity.screenX;
            float screenY = (float) entity.screenY;

            RenderUtils.drawImageNative(event.drawContext().getMatrices(), screenX - 2, screenY + 1, 120, 35, idMap.get(time));
        });
    }
}

