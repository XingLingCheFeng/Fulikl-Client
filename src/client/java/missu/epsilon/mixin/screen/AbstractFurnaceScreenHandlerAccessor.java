package missu.epsilon.mixin.screen;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import tech.skidonion.obfuscator.annotations.Renamer;

@Mixin(AbstractFurnaceScreenHandler.class)
@Renamer(obfuscated = false)
public interface AbstractFurnaceScreenHandlerAccessor {

    @Renamer(obfuscated = false)
    @Accessor("inventory")
    Inventory getInventory();

}
