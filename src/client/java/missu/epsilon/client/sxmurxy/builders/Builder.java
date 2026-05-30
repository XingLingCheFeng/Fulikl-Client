package missu.epsilon.client.sxmurxy.builders;

import missu.epsilon.client.sxmurxy.builders.impl.BlurBuilder;
import missu.epsilon.client.sxmurxy.builders.impl.TextureBuilder;

public final class Builder {

    private static final TextureBuilder TEXTURE_BUILDER = new TextureBuilder();
    private static final BlurBuilder BLUR_BUILDER = new BlurBuilder();

    public static TextureBuilder texture() {
        return TEXTURE_BUILDER;
    }

    public static BlurBuilder blur() {
        return BLUR_BUILDER;
    }

}