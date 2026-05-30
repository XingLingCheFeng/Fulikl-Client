package missu.epsilon.mixin.client.render;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor
    float getCameraY();

    @Accessor
    float getLastCameraY();
}
