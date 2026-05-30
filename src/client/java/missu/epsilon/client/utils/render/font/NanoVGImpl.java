package missu.epsilon.client.utils.render.font;

import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;

public class NanoVGImpl {
    public static long context;
    public static void init() {
        context = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS | NanoVGGL3.NVG_STENCIL_STROKES);
        NanoVG.nvgShapeAntiAlias(context, true);
    }

}