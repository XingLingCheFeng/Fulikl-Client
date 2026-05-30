package missu.epsilon.client.utils.render;


import missu.epsilon.client.utils.animations.basic.color.ColorPanel;
import missu.epsilon.client.utils.render.font.NanoVGImpl;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import static missu.epsilon.client.utils.Wrapper.mc;
import static org.lwjgl.nanovg.NanoVG.*;


public class RenderHelper extends NanoVGImpl {
    public static void scaleStart(float centerX, float centerY, float scale) {
        nvgSave(context);
        nvgTranslate(context, centerX, centerY);
        nvgScale(context, scale, scale);
        nvgTranslate(context, -centerX, -centerY);
    }

    public static void scaleEnd() {
        nvgRestore(context);
    }

    public static void scissorStart(float x, float y, float width, float height) {
        nvgSave(context);
        nvgScissor(context, x, y, width, height);
    }

    public static void scissorEnd() {
        nvgRestore(context);
    }

    public static void beginRender() {
        nvgBeginFrame(context, mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), 1f);
        nvgSave(context);
        nvgTranslate(context, 0f, 0f);
        nvgScale(context, (float) mc.getWindow().getScaleFactor(), (float) mc.getWindow().getScaleFactor());
        nvgTranslate(context, 0f, 0f);
    }

    public static void endRender() {
        nvgRestore(context);
        nvgEndFrame(context);
    }

    public static void fillColor(ColorPanel color) {
        NVGColor nvgColor = getColor(color);
        nvgFillColor(context, nvgColor);
        nvgColor.free();
    }

    public static NVGPaint createGradient(float startX, float startY, float endX, float endY, ColorPanel colorLeft, ColorPanel colorRight) {
        NVGPaint paint = NVGPaint.calloc();
        nvgLinearGradient(context, startX, startY, endX, endY, getColor(colorLeft), getColor(colorRight), paint);
        return paint;
    }

    public static NVGColor getColor(ColorPanel color) {
        NVGColor nvgColor = NVGColor.calloc();
        nvgColor.r(color.red).g(color.green).b(color.blue).a(color.alpha);
        return nvgColor;
    }

    public static NVGPaint createRadialGradient(float cx, float cy, float inr, float outr, ColorPanel innerColor, ColorPanel outerColor) {
        NVGPaint paint = NVGPaint.calloc();
        NVGColor icolor = getColor(innerColor);
        NVGColor ocolor = getColor(outerColor);

        nvgRadialGradient(context, cx, cy, inr, outr, icolor, ocolor, paint);

        icolor.free();
        ocolor.free();

        return paint;
    }

    public static void strokeColor(ColorPanel color) {
        NVGColor nvgColor = getColor(color);
        nvgStrokeColor(context, nvgColor);
        nvgColor.free();
    }
}
