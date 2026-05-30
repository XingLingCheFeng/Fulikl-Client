package missu.epsilon.mixin.client.gui.screen.ingame;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.world.ContainerStealer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static missu.epsilon.client.utils.Wrapper.mc;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void drawScreenHead(CallbackInfo callbackInfo) {
        ContainerStealer containerStealer = Client.moduleManager.getModule(ContainerStealer.class);
        Screen screen = mc.currentScreen;
        if (containerStealer.getState() && ContainerStealer.isVanillaChest(screen) && ContainerStealer.cancelContainerGui.get() && ContainerStealer.container.get("Chest") && screen instanceof GenericContainerScreen) {
            mc.currentScreen = screen;
            callbackInfo.cancel();
        }
        if (containerStealer.getState() && ContainerStealer.cancelContainerGui.get() && ContainerStealer.container.get("Furnace") && screen instanceof FurnaceScreen) {
            mc.currentScreen = screen;
            callbackInfo.cancel();
        }
        if (containerStealer.getState() && ContainerStealer.cancelContainerGui.get() && ContainerStealer.container.get("BlastFurnace") && screen instanceof BlastFurnaceScreen) {
            mc.currentScreen = screen;
            callbackInfo.cancel();
        }
        if (containerStealer.getState() && ContainerStealer.cancelContainerGui.get() && ContainerStealer.container.get("SmokerFurnace") && screen instanceof SmokerScreen) {
            mc.currentScreen = screen;
            callbackInfo.cancel();
        }
        if (containerStealer.getState() && ContainerStealer.cancelContainerGui.get() && ContainerStealer.container.get("BrewingStand") && screen instanceof BrewingStandScreen) {
            mc.currentScreen = screen;
            callbackInfo.cancel();
        }
    }

}
