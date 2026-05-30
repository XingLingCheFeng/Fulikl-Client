package missu.epsilon.mixin.network;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.render.Camera;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin(AbstractClientPlayerEntity.class)
public class MixinAbstractClientPlayerEntity {

    @ModifyReturnValue(method = "getFovMultiplier", at = @At("RETURN"))
    private float injectFovMultiplier(float original) {
        if (Client.moduleManager.getModule(Camera.class).getState() && Camera.modifyFov.get()) {
            return Camera.fov.get().floatValue();
        }
        return original;
    }

}
