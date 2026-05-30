package missu.epsilon.mixin.client.gui.hud;

import missu.epsilon.client.features.modules.visual.ClientSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin(BossBarHud.class)
public class MixinBossBar {

    /**
     * @author DreamDev
     * @reason 禁用BossBar
     */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(DrawContext context, CallbackInfo ci) {
        if (ClientSettings.hideBossBar.get()) {
            ci.cancel();
        }
    }

}
