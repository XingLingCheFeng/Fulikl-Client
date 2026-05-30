package missu.epsilon.mixin.screen;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.BrewingStandScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import tech.skidonion.obfuscator.annotations.Renamer;

@Mixin(BrewingStandScreenHandler.class)
@Renamer(obfuscated = false)
public interface BrewingStandScreenHandlerAccessor {

    @Renamer(obfuscated = false)
    @Accessor("inventory")
    Inventory getInventory();

}
