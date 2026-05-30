package missu.epsilon.client.utils;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.utils.render.font.FontManager;
import missu.epsilon.client.utils.render.font.FontRenderer;
import net.minecraft.client.MinecraftClient;

public interface Wrapper {

    MinecraftClient mc = MinecraftClient.getInstance();

    default <T extends Module> T getModule(Class<T> clazz) {
        return Client.moduleManager.getModule(clazz);
    }

}
