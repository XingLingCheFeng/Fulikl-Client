package missu.epsilon.client.utils.render.font;

public class FontManager extends NanoVGImpl {

    public static FontRenderer PingFang;
    public static FontRenderer BoldPingFang;
    public static FontRenderer FilledMaterial;
    public static FontRenderer Quantum;


    public static void registerFonts() {
        PingFang = new FontRenderer("PingFang", "PingFang");
        BoldPingFang = new FontRenderer("BoldPingFang", "BoldPingFang");
        FilledMaterial = new FontRenderer("FilledMaterial", "FilledMaterial");
        Quantum = new FontRenderer("Quantum", "Quantum");
    }
}
