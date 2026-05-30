package missu.epsilon.client.event.events.game;

import missu.epsilon.client.event.impl.Event;
import net.minecraft.client.gui.screen.Screen;

public class ScreenEvent implements Event {

    public Screen guiScreen;

    public ScreenEvent(Screen guiScreen) {
        this.guiScreen = guiScreen;
    }

}
