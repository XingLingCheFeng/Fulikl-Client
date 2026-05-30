package missu.epsilon.mixin.client.font;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.player.NameProtect;
import missu.epsilon.client.features.modules.visual.ClientSettings;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.skidonion.obfuscator.annotations.Renamer;

import java.util.Optional;

import static missu.epsilon.client.utils.Wrapper.mc;

@Renamer(obfuscated = false)
@Mixin({TextRenderer.class})
public class MixinTextRenderer {

    @Inject(method = "getWidth(Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true)
    public void getWidthString(String text, CallbackInfoReturnable<Integer> cir) {
        if (text == null) {
            cir.setReturnValue(0);
            return;
        }
        if (Client.moduleManager == null) {
            return;
        }
        if (!Client.moduleManager.getModule(NameProtect.class).isEnabled() && !text.contains("布吉岛")) {
            return;
        }
        String modifiedText = text;
        if (modifiedText.contains("布吉岛")) {
            modifiedText = modifiedText.replace("布吉岛", ClientSettings.scoreboard.get());
        }
        if (Client.moduleManager.getModule(NameProtect.class).isEnabled() && mc.player != null) {
            String playerName = mc.player.getName().getString();
            if (modifiedText.contains(playerName)) {
                modifiedText = modifiedText.replace(playerName, NameProtect.nick);
            }
        }
        if (!modifiedText.equals(text)) {
            int width = ((TextRenderer)(Object)this).getWidth(modifiedText);
            cir.setReturnValue(width);
            cir.cancel();
        }
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/StringVisitable;)I", at = @At("HEAD"), cancellable = true)
    public void getWidthStringVisitable(StringVisitable text, CallbackInfoReturnable<Integer> cir) {
        StringBuilder sb = new StringBuilder();
        text.visit((style, string) -> {
            sb.append(string);
            return Optional.empty();
        }, Style.EMPTY);
        String originalText = sb.toString();
        if (Client.moduleManager == null) {
            return;
        }
        if (!Client.moduleManager.getModule(NameProtect.class).isEnabled() && !originalText.contains("布吉岛")) {
            return;
        }
        String modifiedText = originalText;
        if (modifiedText.contains("布吉岛")) {
            modifiedText = modifiedText.replace("布吉岛", ClientSettings.scoreboard.get());
        }
        if (Client.moduleManager.getModule(NameProtect.class).isEnabled() && mc.player != null) {
            String playerName = mc.player.getName().getString();
            if (modifiedText.contains(playerName)) {
                modifiedText = modifiedText.replace(playerName, NameProtect.nick);
            }
        }
        if (!modifiedText.equals(originalText)) {
            int width = ((TextRenderer)(Object)this).getWidth(modifiedText);
            cir.setReturnValue(width);
            cir.cancel();
        }
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/OrderedText;)I", at = @At("HEAD"), cancellable = true)
    public void getWidthOrderedText(OrderedText text, CallbackInfoReturnable<Integer> cir) {
        StringBuilder sb = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        String originalText = sb.toString();
        if (Client.moduleManager == null) {
            return;
        }
        if (!Client.moduleManager.getModule(NameProtect.class).isEnabled() && !originalText.contains("布吉岛")) {
            return;
        }
        String modifiedText = originalText;
        if (modifiedText.contains("布吉岛")) {
            modifiedText = modifiedText.replace("布吉岛", ClientSettings.scoreboard.get());
        }
        if (Client.moduleManager.getModule(NameProtect.class).isEnabled() && mc.player != null) {
            String playerName = mc.player.getName().getString();
            if (modifiedText.contains(playerName)) {
                modifiedText = modifiedText.replace(playerName, NameProtect.nick);
            }
        }
        if (!modifiedText.equals(originalText)) {
            int width = ((TextRenderer)(Object)this).getWidth(modifiedText);
            cir.setReturnValue(width);
            cir.cancel();
        }
    }

}

