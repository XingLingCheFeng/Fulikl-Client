package missu.epsilon.client.features.modules.world;

import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.KeyBindValue;
import net.minecraft.client.util.InputUtil;

import java.util.Objects;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "QuickPlay", description = "Automatically queues a game in Hypixel when you press a bound key", category = ModuleCategory.WORLD)
public class QuickPlay extends Module {
    public static KeyBindValue bw44 = new KeyBindValue("BedWars 44", 0);
    public static KeyBindValue bwSolo = new KeyBindValue("BedWars Solo", 0);
    public static KeyBindValue swSolo = new KeyBindValue("SkyWars Solo", 0);
    public static KeyBindValue bwPractice = new KeyBindValue("BedWars Practice", 0);
    public static KeyBindValue hub = new KeyBindValue("Hub", 0);

    private boolean bw44Pressed = false;
    private boolean bwSoloPressed = false;
    private boolean swSoloPressed = false;
    private boolean bwPracticePressed = false;
    private boolean hubPressed = false;

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.player != null && mc.getNetworkHandler() != null && mc.currentScreen == null) {
            // 检测并处理每个按键绑定
            boolean currentBw44Pressed = bw44.getKey() != 0 && InputUtil.isKeyPressed(mc.getWindow().getHandle(), bw44.getKey());
            if (currentBw44Pressed && !bw44Pressed) {
                Objects.requireNonNull(mc.getNetworkHandler()).sendChatMessage("/play bedwars_four_four");
            }
            bw44Pressed = currentBw44Pressed;
            
            boolean currentBwSoloPressed = bwSolo.getKey() != 0 && InputUtil.isKeyPressed(mc.getWindow().getHandle(), bwSolo.getKey());
            if (currentBwSoloPressed && !bwSoloPressed) {
                Objects.requireNonNull(mc.getNetworkHandler()).sendChatMessage("/play bedwars_eight_one");
            }
            bwSoloPressed = currentBwSoloPressed;
            
            boolean currentSwSoloPressed = swSolo.getKey() != 0 && InputUtil.isKeyPressed(mc.getWindow().getHandle(), swSolo.getKey());
            if (currentSwSoloPressed && !swSoloPressed) {
                Objects.requireNonNull(mc.getNetworkHandler()).sendChatMessage("/play solo_normal");
            }
            swSoloPressed = currentSwSoloPressed;
            
            boolean currentBwPracticePressed = bwPractice.getKey() != 0 && InputUtil.isKeyPressed(mc.getWindow().getHandle(), bwPractice.getKey());
            if (currentBwPracticePressed && !bwPracticePressed) {
                Objects.requireNonNull(mc.getNetworkHandler()).sendChatMessage("/play bedwars_practice");
            }
            bwPracticePressed = currentBwPracticePressed;
            
            boolean currentHubPressed = hub.getKey() != 0 && InputUtil.isKeyPressed(mc.getWindow().getHandle(), hub.getKey());
            if (currentHubPressed && !hubPressed) {
                Objects.requireNonNull(mc.getNetworkHandler()).sendChatMessage("/hub");
            }
            hubPressed = currentHubPressed;
        }
    }
}
