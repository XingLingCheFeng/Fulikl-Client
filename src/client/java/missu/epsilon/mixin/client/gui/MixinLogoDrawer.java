package missu.epsilon.mixin.client.gui;

import missu.epsilon.client.nanovg.gl.States;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.render.font.NanoVGImpl;
import missu.epsilon.client.utils.render.font.FontManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import org.lwjgl.nanovg.NanoVG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LogoDrawer.class)
public class MixinLogoDrawer {

    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IFI)V", at = @At("HEAD"), cancellable = true)
    public void draw(DrawContext context, int screenWidth, float alpha, int y, CallbackInfo ci) {
        float middleX = (float) MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;

        States.push();
        NanoVG.nvgBeginFrame(NanoVGImpl.context, MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight(), 1);
        NanoVG.nnvgBeginFrame(NanoVGImpl.context, MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight(), 1);
        FontManager.Quantum.drawGlowString(128, "epsilon", middleX - (FontManager.Quantum.getStringWidth("epsilon", 128) / 2), y, new ColorPanel(1f,1f,1f, 1f), new ColorPanel(1f,1f,1f,0.5f), true, 4);
        NanoVG.nvgEndFrame(NanoVGImpl.context);
        NanoVG.nnvgEndFrame(NanoVGImpl.context);
        States.pop();

        ci.cancel();
    }

}