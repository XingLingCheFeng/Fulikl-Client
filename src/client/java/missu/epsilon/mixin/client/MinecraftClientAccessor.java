package missu.epsilon.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import tech.skidonion.obfuscator.annotations.Renamer;

@Renamer(obfuscated = false)
@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {

    @Renamer(obfuscated = false)
    @Accessor("itemUseCooldown")
    int getItemUseCooldown();

    @Renamer(obfuscated = false)
    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int cooldown);

}