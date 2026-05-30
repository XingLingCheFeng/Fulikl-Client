package missu.epsilon.mixin.client.world;


import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin({ClientWorld.class})
public interface ClientWorldAccessor {

   @Accessor("pendingUpdateManager")
   PendingUpdateManager getBlockStatePredictionHandler();

}
