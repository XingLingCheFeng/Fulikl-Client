package missu.epsilon.client.utils.render.font;

import missu.epsilon.client.Client;
import missu.epsilon.client.features.modules.player.NameProtect;
import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.render.RenderHelper;
import net.minecraft.util.Formatting;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static missu.epsilon.client.utils.Wrapper.mc;
import static org.lwjgl.nanovg.NanoVG.*;

public class FontRenderer extends NanoVGImpl {
    private final int fontInt;
    private final Map<String, Float> widthCache = new ConcurrentHashMap<>();
    private static final int[] colorCode = new int[32];

    static {
        for (int i = 0; i < 32; ++i) {
            final int base = (i >> 3 & 0x1) * 85;
            int r = (i >> 2 & 0x1) * 170 + base;
            int g = (i >> 1 & 0x1) * 170 + base;
            int b = (i & 0x1) * 170 + base;
            if (i == 6) {
                r += 85;
            }
            if (i >= 16) {
                r /= 4;
                g /= 4;
                b /= 4;
            }
            colorCode[i] = ((r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF));
        }
    }

    public FontRenderer(String name, String fileName) {
        try (InputStream inputStream = getResourceAsStream(fileName)) {
            byte[] data = toByteArray(Objects.requireNonNull(inputStream));
            ByteBuffer buffer = MemoryUtil.memAlloc(data.length);
            buffer.put(data).flip();

            fontInt = nvgCreateFontMem(context, name, buffer, false);
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public float getStringWidth(String text, float size) {
        if (text == null || Client.moduleManager == null) {
            return 0f;
        }
        if (Client.moduleManager.getModule(NameProtect.class).isEnabled()) {
            if (mc.player != null) {
                text = text.replaceAll(mc.player.getName().getString(), Formatting.LIGHT_PURPLE + NameProtect.nick + Formatting.RESET);
            }
        }
        text = text.replaceAll("§.", "");
        text = text.replaceAll("布吉岛", "吉吉岛");

        nvgFontFaceId(context, fontInt);
        nvgFontSize(context, size / 2f);

        float[] bounds = new float[4];

        nvgTextBounds(context, 0, 0, text, bounds);

        return (bounds[2] - bounds[0]);
    }

    public void drawString(float size, String text, float x, float y, ColorPanel color, boolean shadow) {
        if (Client.moduleManager == null) return;

        if (Client.moduleManager.getModule(NameProtect.class).isEnabled()) {
            if (mc.player != null) {
                text = text.replaceAll(mc.player.getName().getString(), Formatting.LIGHT_PURPLE + NameProtect.nick + Formatting.RESET);
            }
        }
        ////text = text.replaceAll("§.", "");

        if (shadow) {
            renderString(size, text, x + 0.5f, y + 0.5f, color.darken());
        }

        renderString(size, text, x, y, color);
    }

    public void drawStringmiddleY(float size, String text, float x, float y, ColorPanel color, boolean shadow) {
        if (Client.moduleManager == null) return;

        if (Client.moduleManager.getModule(NameProtect.class).isEnabled()) {
            if (mc.player != null) {
                text = text.replaceAll(mc.player.getName().getString(), Formatting.LIGHT_PURPLE + NameProtect.nick + Formatting.RESET);
            }
        }
        //text = text.replaceAll("§.", "");

        y -= getHeight(size) / 2;

        if (shadow) {
            renderString(size, text, x + 0.5f, y + 0.5f, color.darken());
        }

        renderString(size, text, x, y, color);
    }

    public void drawStringOpposite(float size, String text, float x, float y, ColorPanel color, boolean shadow) {
        if (Client.moduleManager == null) return;

        if (Client.moduleManager.getModule(NameProtect.class).isEnabled()) {
            if (mc.player != null) {
                text = text.replaceAll(mc.player.getName().getString(), Formatting.LIGHT_PURPLE + NameProtect.nick + Formatting.RESET);
            }
        }
        //text = text.replaceAll("§.", "");

        if (shadow) {
            renderStringOpposite(size, text, x + 0.5f, y + 0.5f, color.darken());
        }

        renderStringOpposite(size, text, x, y, color);
    }

    private void renderStringOpposite(float size, String text, float x, float y, ColorPanel color) {
        nvgSave(context);
        nvgTextAlign(context, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        RenderHelper.fillColor(color);
        nvgFontFaceId(context, fontInt);
        nvgFontSize(context, size / 2f);
        nvgTranslate(context, x + getStringWidth(text, size), y);
        nvgScale(context, -1f, 1f);
        nvgText(context, 0f, 0f, text);
        nvgRestore(context);
    }

    public float getHeight(float size) {
        nvgFontFaceId(context, fontInt);
        float[] ascender = new float[1];
        float[] descender = new float[1];
        float[] lineh = new float[1];
        nvgFontSize(context, size / 2f);
        nvgTextMetrics(context, ascender, descender, lineh);
        return (float) (lineh[0] / mc.getWindow().getScaleFactor());
    }


    private void renderString(float size, String text, float x, float y, ColorPanel color) {
        nvgTextAlign(context, NVG_ALIGN_LEFT | NVG_ALIGN_TOP);
        nvgFontFaceId(context, fontInt);
        nvgFontSize(context, size / 2f);

        List<TextSegment> segments = new ArrayList<>();
        ColorPanel currentColor = color;
        StringBuilder currentSegment = new StringBuilder();

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char chr = chars[i];

            if (chr == '§' && i + 1 < chars.length) {
                if (currentSegment.length() > 0) {
                    segments.add(new TextSegment(currentSegment.toString(), currentColor));
                    currentSegment.setLength(0);
                }

                char next = Character.toLowerCase(chars[++i]);
                int codeIndex = "0123456789abcdefklmnor".indexOf(next);

                if (codeIndex >= 0) {
                    if (codeIndex < 16) {
                        int colorValue = colorCode[codeIndex];

                        if (colorValue == 11184810) {
                            currentColor = ColorPanel.createColorPanel(1f, 1f, 1f, color.alpha);
                        } else {
                            int red = (colorValue >> 16) & 0xFF;
                            int green = (colorValue >> 8) & 0xFF;
                            int blue = colorValue & 0xFF;

                            currentColor = ColorPanel.createColorPanel(
                                    red / 255f,
                                    green / 255f,
                                    blue / 255f,
                                    color.alpha
                            );
                        }
                    } else if (next == 'r') {
                        // reset color
                        currentColor = ColorPanel.createColorPanel(
                                color.red,
                                color.green,
                                color.blue,
                                color.alpha
                        );
                    } else if (next == 'l') {
                        // bold
                    }
                }
            } else {
                currentSegment.append(chr);
            }
        }

        // Last list
        if (currentSegment.length() > 0) {
            segments.add(new TextSegment(currentSegment.toString(), currentColor));
        }

        // Width
        float totalWidth = 0;
        for (TextSegment segment : segments) {
            segment.width = getStringWidth(segment.text, size);
            totalWidth += segment.width;
        }

        // Render
        float currentX = x;
        for (TextSegment segment : segments) {
            RenderHelper.fillColor(segment.color);
            nvgText(context, currentX, y, segment.text);
            currentX += segment.width + 0.2F;
        }
    }


    // Segment
    private static class TextSegment {
        String text;
        ColorPanel color;
        float width;

        TextSegment(String text, ColorPanel color) {
            this.text = text;
            this.color = color;
        }
    }

    public void drawGlowString(float size, String text, float x, float y, ColorPanel color, ColorPanel glowColor, boolean shadow, float radius) {
        if (Client.moduleManager.getModule(NameProtect.class).isEnabled()) {
            if (mc.player != null) {
                text = text.replaceAll(mc.player.getName().getString(), Formatting.LIGHT_PURPLE + NameProtect.nick + Formatting.RESET);
            }
        }
        //text = text.replaceAll("§.", "");

        if (shadow) {
            renderGlowString(size, text, x + 0.5f, y + 0.5f, color.darken(), glowColor, radius);
        }

        renderGlowString(size, text, x, y, color, glowColor, radius);
    }

    private void renderGlowString(float size, String text, float x, float y, ColorPanel color, ColorPanel glowColor, float radius) {
        nvgFontBlur(context, radius);
        renderString(size, text, x, y, glowColor);
        nvgFontBlur(context, 0);
        renderString(size, text, x, y, color);
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;

        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }
    
    public static InputStream getResourceAsStream(String fileName) {
        try {
            String location = "/assets/epsilon/fonts/" + fileName + ".otf";

            return FontRenderer.class.getResourceAsStream(location);
        } catch (Exception exception) {
            return null;
        }
    }
}
