package missu.epsilon.mixin.client;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.player.KeyEvent;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.client.KeyCodeConverter;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

import static missu.epsilon.client.utils.Wrapper.mc;

@Renamer(obfuscated = false)
@Mixin(Keyboard.class)
public class MixinKeyboard {

    @SuppressWarnings("CallToPrintStackTrace")
    @Inject(at = @At("HEAD"), method = "onKey")
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo info) {
        if(Client.getInstance().getEventManager() != null && action == 1){
            KeyEvent keyevent = new KeyEvent(key, MinecraftClient.getInstance().currentScreen != null);
            Client.getInstance().getEventManager().call(keyevent);
        }
    }

}
