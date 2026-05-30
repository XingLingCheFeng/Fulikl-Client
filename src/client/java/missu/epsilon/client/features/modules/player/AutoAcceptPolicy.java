package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.ingameui.notification.Notification;
import missu.epsilon.client.ingameui.notification.NotificationManager;
import missu.epsilon.client.ingameui.notification.NotificationType;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;

@ModuleInfo(name = "AutoAcceptPolicy",description = "Automatically accepts Hypixel's user policy.",category = ModuleCategory.PLAYER)
public class AutoAcceptPolicy extends Module {
    public boolean findClickable(BookScreen.Contents contents) {
        for (Text text : contents.pages()) {
            if (executeIfContains(text)) return true;
        }
        return false;
    }

    public boolean executeIfContains(Text text) {
        if (mc.player == null) return false;

        ClickEvent click = text.getStyle().getClickEvent();

        if (click != null
                && click.getAction() == ClickEvent.Action.RUN_COMMAND
                && text.getString() != null
                && text.getString().contains("I Agree")) {

            mc.player.networkHandler.sendChatCommand(
                    click.getValue().replaceFirst("/", "")
            );

            NotificationManager.post(new Notification(NotificationType.SUCCESS, "Successfully accepted Hypixel's policy."));
            return true;
        }

        for (Text sibling : text.getSiblings()) {
            if (executeIfContains(sibling)) return true;
        }
        return false;
    }

}
