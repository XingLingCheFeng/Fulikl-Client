/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package missu.epsilon.client.utils.render;

import missu.epsilon.client.event.events.render.RenderNvgEvent;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.math.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;
import java.util.regex.Pattern;

public class ColorUtils {
    public static Color intToRGB(int value) {
        int red = (value >> 16) & 0xFF;
        int green = (value >> 8) & 0xFF;
        int blue = value & 0xFF;
        return new Color(red, green, blue);
    }

    public static int colorSwitch(Color firstColor, Color secondColor, float time, int index, long timePerIndex, double speed, double alpha) {
        long now = (long) (speed * (double) System.currentTimeMillis() + (double) ((long) index * timePerIndex));

        float redDiff = (float) (firstColor.getRed() - secondColor.getRed()) / time;
        float greenDiff = (float) (firstColor.getGreen() - secondColor.getGreen()) / time;
        float blueDiff = (float) (firstColor.getBlue() - secondColor.getBlue()) / time;

        int red = Math.round((float) secondColor.getRed() + redDiff * (float) (now % (long) time));
        int green = Math.round((float) secondColor.getGreen() + greenDiff * (float) (now % (long) time));
        int blue = Math.round((float) secondColor.getBlue() + blueDiff * (float) (now % (long) time));

        float redInverseDiff = (float) (secondColor.getRed() - firstColor.getRed()) / time;
        float greenInverseDiff = (float) (secondColor.getGreen() - firstColor.getGreen()) / time;
        float blueInverseDiff = (float) (secondColor.getBlue() - firstColor.getBlue()) / time;

        int inverseRed = Math.round((float) firstColor.getRed() + redInverseDiff * (float) (now % (long) time));
        int inverseGreen = Math.round((float) firstColor.getGreen() + greenInverseDiff * (float) (now % (long) time));
        int inverseBlue = Math.round((float) firstColor.getBlue() + blueInverseDiff * (float) (now % (long) time));

        if (now % ((long) time * 2L) < (long) time) {
            return new Color(inverseRed, inverseGreen, inverseBlue, (int) alpha).getRGB();
        }

        return new Color(red, green, blue, (int) alpha).getRGB();
    }


    public static String stripColor(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }

    private static Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    public static void drawOutlineString(String s, Float x, Float y, int color, RenderNvgEvent event) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.push();
        event.drawContext().drawText(MinecraftClient.getInstance().textRenderer, stripColor(s), (int) (x * 2 - 1), (int) (y * 2), Color.BLACK.getRGB(), false);
        event.drawContext().drawText(MinecraftClient.getInstance().textRenderer, stripColor(s), (int) (x * 2 + 1), (int) (y * 2), Color.BLACK.getRGB(), false);
        event.drawContext().drawText(MinecraftClient.getInstance().textRenderer, stripColor(s), (int) (x * 2), (int) (y * 2 - 1), Color.BLACK.getRGB(), false);
        event.drawContext().drawText(MinecraftClient.getInstance().textRenderer, stripColor(s), (int) (x * 2), (int) (y * 2 + 1), Color.BLACK.getRGB(), false);
        event.drawContext().drawText(MinecraftClient.getInstance().textRenderer, s, (int) (x * 2), (int) (y * 2), color, false);
        matrixStack.pop();
    }

    private static float[] getRainbowHSB(int counter, float saturation, float brightness) {
        return new float[]{(float) (((Math.ceil(System.currentTimeMillis() - (long) counter * 20) / 8) % 360) / 360), saturation, brightness};
    }

    public static Color getRainbow(int counter, float saturation, float brightness) {
        var rainbowHSB = getRainbowHSB(counter, saturation, brightness);
        return new Color(Color.HSBtoRGB(rainbowHSB[0], rainbowHSB[1], rainbowHSB[2]));
    }

    public static Color fade(final int speed, final int index, final Color color, final float alpha) {
        final float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360L);
        angle = ((angle > 180) ? (360 - angle) : angle) + 180;
        final Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360.0f));
        return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int) (alpha * 255.0f))));
    }

    public static ColorPanel colorToColorPanel(Color color) {
        return ColorPanel.createColorPanel((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, (float) color.getAlpha() / 255);
    }

    public static Color reAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathUtils.clamp(alpha, 0, 255));
    }

    public static Color applyOpacity(Color color, float opacity) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * Math.min(1, Math.max(0, opacity))));
    }

    public static Color hsvToRgb(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255);
        } else {
            float h = hue * 6;
            int i = (int) Math.floor(h);
            float f = h - i;
            float p = brightness * (1 - saturation);
            float q = brightness * (1 - saturation * f);
            float t = brightness * (1 - saturation * (1 - f));

            switch (i) {
                case 0:
                    r = (int) (brightness * 255);
                    g = (int) (t * 255);
                    b = (int) (p * 255);
                    break;
                case 1:
                    r = (int) (q * 255);
                    g = (int) (brightness * 255);
                    b = (int) (p * 255);
                    break;
                case 2:
                    r = (int) (p * 255);
                    g = (int) (brightness * 255);
                    b = (int) (t * 255);
                    break;
                case 3:
                    r = (int) (p * 255);
                    g = (int) (q * 255);
                    b = (int) (brightness * 255);
                    break;
                case 4:
                    r = (int) (t * 255);
                    g = (int) (p * 255);
                    b = (int) (brightness * 255);
                    break;
                case 5:
                    r = (int) (brightness * 255);
                    g = (int) (p * 255);
                    b = (int) (q * 255);
                    break;
            }
        }
        return new Color(r, g, b);
    }

    public static float[] rgbToHsv(int r, int g, int b) {
        float hue, saturation, brightness;
        float[] hsv = new float[3];
        int cmax = Math.max(r, Math.max(g, b));
        int cmin = Math.min(r, Math.min(g, b));
        int delta = cmax - cmin;

        brightness = cmax / 255.0f;
        saturation = cmax != 0 ? (float) delta / cmax : 0;

        if (delta == 0) {
            hue = 0;
        } else {
            if (cmax == r) hue = ((float) (g - b) / delta) / 6f;
            else if (cmax == g) hue = ((float) (b - r) / delta + 2f) / 6f;
            else hue = ((float) (r - g) / delta + 4f) / 6f;

            if (hue < 0) hue += 1f;
        }

        hsv[0] = hue;
        hsv[1] = saturation;
        hsv[2] = brightness;
        return hsv;
    }
}
