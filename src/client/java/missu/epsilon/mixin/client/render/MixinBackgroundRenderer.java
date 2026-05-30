package missu.epsilon.mixin.client.render;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.render.AntiBlind;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.entity.effect.StatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tech.skidonion.obfuscator.annotations.Renamer;

import java.util.List;
import java.util.stream.Stream;

@Renamer(obfuscated = false)
@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {

    @Redirect(method = "getFogModifier", at = @At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"))
    private static Stream<BackgroundRenderer.StatusEffectFogModifier> injectAntiBlind(List<BackgroundRenderer.StatusEffectFogModifier> list) {
        return list.stream().filter(modifier -> {
            final var effect = modifier.getStatusEffect();
            if (!Client.moduleManager.getModule(AntiBlind.class).isEnabled()) {
                return true;
            }
            return !(StatusEffects.BLINDNESS == effect && AntiBlind.blind.get()) || (StatusEffects.DARKNESS == effect && AntiBlind.darkness.get());
        });
    }

}
