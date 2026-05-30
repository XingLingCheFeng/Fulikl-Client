package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.features.value.impl.TextValue;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "AutoGG",description = "Auto Speak something on the end of the game",category = ModuleCategory.PLAYER, hide = true)
public class AutoGG extends Module {

    public static ListValue server = new ListValue("Server", new String[]{"Hypixel"},"Hypixel");
    public static TextValue winWords = new TextValue("WinWords","L");
    public static TextValue loseWords = new TextValue("LoseWords","L");
    public static NumberValue delay = new NumberValue("Delay",100,0,3000, 100);

    public static boolean spoken = false;
    public boolean needSpeakWinWords = false;
    public boolean needSpeakLoseWords = false;
    private final TimerUtils timer = new TimerUtils();

    @SuppressWarnings("unused")
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (ClientUtils.isNull() || mc.getNetworkHandler() == null)
            return;

        if (needSpeakWinWords && timer.hasTimeElapsed(delay.get())) {
            mc.getNetworkHandler().sendChatMessage("/ac " + winWords.get());
            needSpeakWinWords = false;
            spoken = true;
        }

        if (needSpeakLoseWords && timer.hasTimeElapsed(delay.get())) {
            mc.getNetworkHandler().sendChatMessage("/ac " + loseWords.get());
            needSpeakLoseWords = false;
            spoken = true;
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!(event.packet instanceof TitleS2CPacket(Text text))) return;
        if (text == null) return;

        String title = text.getString();

        if (server.get().equals("Hypixel")) {
            if (title.startsWith("§6§l") && (title.endsWith("!") || title.endsWith("！"))) {
                needSpeakWinWords = true;
                timer.reset();
            } else if (title.startsWith("§c§l") && (title.endsWith("!") || title.endsWith("！"))) {
                needSpeakLoseWords = true;
                timer.reset();
            }
        }
    }
}
