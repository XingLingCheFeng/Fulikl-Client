package missu.epsilon.mixin.client.world;

import missu.epsilon.client.utils.client.ClientData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @Shadow @Final private MinecraftClient client;

    @Inject(method = "tickEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;resetPosition()V", shift = At.Shift.AFTER), cancellable = true)
    public void tickEntity(Entity entity, CallbackInfo ci) {
        if (entity == this.client.player && ClientData.getSkipTicks() > 0) {
            ClientData.setSkipTicks(ClientData.getSkipTicks() - 1);
            ci.cancel();
        }
    }

}
