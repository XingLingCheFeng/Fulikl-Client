package missu.epsilon.mixin.client.gui;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.player.NameProtect;
import missu.epsilon.client.features.modules.visual.ClientSettings;
import missu.epsilon.client.utils.client.TextSegment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tech.skidonion.obfuscator.annotations.Renamer;

import java.util.ArrayList;
import java.util.List;

import static missu.epsilon.client.utils.Wrapper.mc;

@Renamer(obfuscated = false)
@Mixin(DrawContext.class)
public class MixinDrawContext {

    @Final @Shadow private MatrixStack matrices;

    @Final @Shadow public VertexConsumerProvider.Immediate vertexConsumers;

    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;IIIZ)I", at = @At("HEAD"), cancellable = true)
    public void drawOrderedText(TextRenderer textRenderer, OrderedText text, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        List<TextSegment> segments = new ArrayList<>();
        final StringBuilder[] currentSegment = {new StringBuilder()};
        Style[] currentStyle = {null};
        text.accept((index, style, codePoint) -> {
            if (currentStyle[0] != null && !currentStyle[0].equals(style)) {
                if (!currentSegment[0].isEmpty()) {
                    segments.add(new TextSegment(currentSegment[0].toString(), currentStyle[0]));
                    currentSegment[0] = new StringBuilder();
                }
            }
            currentStyle[0] = style;
            currentSegment[0].appendCodePoint(codePoint);
            return true;
        });
        if (!currentSegment[0].isEmpty() && currentStyle[0] != null) {
            segments.add(new TextSegment(currentSegment[0].toString(), currentStyle[0]));
        }
        boolean needsModification = false;
        for (TextSegment segment : segments) {
            if (segment.content().contains("布吉岛") ||
                (mc.player != null && segment.content().contains(mc.player.getName().getString()))) {
                needsModification = true;
                break;
            }
        }
        if (!needsModification) {
            return;
        }
        MutableText newText = Text.empty();
        for (TextSegment segment : segments) {
            String content = segment.content();
            if (content.contains("布吉岛")) {
                content = content.replace("布吉岛", ClientSettings.scoreboard.get());
            }
            if (mc.player != null
                    && content.contains(mc.player.getName().getString())
                    && Client.moduleManager.getModule(NameProtect.class).isEnabled()
                    && !"np-bypass".equals(segment.style().getInsertion())
            ) {
                String playerName = mc.player.getName().getString();
                MutableText replacement = Text.literal(NameProtect.nick).setStyle(segment.style().withColor(Formatting.LIGHT_PURPLE));
                int index = content.indexOf(playerName);
                if (index > 0) {
                    newText.append(Text.literal(content.substring(0, index)).setStyle(segment.style()));
                }
                newText.append(replacement);
                if (index + playerName.length() < content.length()) {
                    newText.append(Text.literal(content.substring(index + playerName.length())).setStyle(segment.style()));
                }
                continue;
            }
            newText.append(Text.literal(content).setStyle(segment.style()));
        }
        OrderedText newOrderedText = newText.asOrderedText();
        int result = textRenderer.draw(newOrderedText, (float)x, (float)y, color, shadow, this.matrices.peek().getPositionMatrix(), this.vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        cir.setReturnValue(result);
        cir.cancel();
    }

    @Inject(method = "drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I", at = @At("HEAD"), cancellable = true)
    public void drawStringText(TextRenderer textRenderer, String originalText, int x, int y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        if (originalText == null) {
            cir.setReturnValue(0);
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
                String replacement = "§d" + NameProtect.nick + "§r";
                modifiedText = modifiedText.replace(playerName, replacement);
            }
        }
        if (!modifiedText.equals(originalText)) {
            int i = textRenderer.draw(modifiedText, (float)x, (float)y, color, shadow, this.matrices.peek().getPositionMatrix(), this.vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
            cir.setReturnValue(i);
            cir.cancel();
        }
    }

}