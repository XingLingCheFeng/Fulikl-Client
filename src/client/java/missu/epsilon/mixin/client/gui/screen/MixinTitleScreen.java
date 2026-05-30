package missu.epsilon.mixin.client.gui.screen;

import missu.epsilon.client.Client;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 0)
    private String changeFinalText(String original) {
        return Client.CLIENT_NAME + " Client " + Client.CLIENT_VERSION + " | (" + Formatting.GRAY + Client.username + Formatting.RESET + ")";
    }

}
