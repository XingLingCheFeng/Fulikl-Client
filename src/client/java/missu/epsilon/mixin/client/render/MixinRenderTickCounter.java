package missu.epsilon.mixin.client.render;

import missu.epsilon.client.utils.client.ClientData;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTickCounter.Dynamic.class)
public class MixinRenderTickCounter {

    @Shadow private float lastFrameDuration;

    /**
     * Hook timer speed to modify frame duration
     */
    @Inject(method = "beginRenderTick(J)I", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/RenderTickCounter$Dynamic;lastFrameDuration:F", shift = At.Shift.AFTER))
    private void hookTimer(CallbackInfoReturnable<Integer> callback) {
        float customTimer = ClientData.timerSpeed;
        if (customTimer > 0) {
            lastFrameDuration *= customTimer;
        }
    }

}
