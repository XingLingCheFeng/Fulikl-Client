package missu.epsilon.mixin.client.gui.hud;

import com.llamalad7.mixinextras.sugar.Local;
import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.render.AntiBlind;
import missu.epsilon.client.features.modules.visual.ClientSettings;
import missu.epsilon.client.features.modules.visual.Scoreboard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin({InGameHud.class})
public class MixinInGameHud {

    @Final @Unique private static final Identifier liquid_bounce$PUMPKIN_BLUR = Identifier.ofVanilla("misc/pumpkinblur");

    /**
     * @author DreamDev
     * @reason 害死原版的药水显示
     */
    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"),cancellable = true)
    private void renderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ClientSettings.hidePotion.get()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void injectPumpkinBlur(DrawContext context, Identifier texture, float opacity, CallbackInfo callback) {
        if (!Client.moduleManager.getModule(AntiBlind.class).isEnabled()) {
            return;
        }

        if (!AntiBlind.pumpkin.get() && liquid_bounce$PUMPKIN_BLUR.equals(texture)) {
            callback.cancel();
        }
    }

    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
    private void hookNauseaOverlay(DrawContext context, float distortionStrength, CallbackInfo ci) {
        if (!(AntiBlind.nausea.get() && Client.moduleManager.getModule(AntiBlind.class).isEnabled())) {
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"), cancellable = true)
    private void renderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 1) ScoreboardObjective scoreboardObjective) {
        if (scoreboardObjective != null) {
            Scoreboard scoreboard = Client.moduleManager.getModule(Scoreboard.class);
            if (scoreboard != null && scoreboard.isEnabled()) {
                scoreboard.setObjective(scoreboardObjective);
                ci.cancel();
            }
        }
    }

}
