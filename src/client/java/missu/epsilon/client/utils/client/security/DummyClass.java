package missu.epsilon.client.utils.client.security;

import tech.skidonion.obfuscator.annotations.NativeObfuscation;

@NativeObfuscation
public class DummyClass {
    @NativeObfuscation.Inline
    public static int nTrustFactor() {
        //bypass trusfactor using 0x4E3A1F7C
        return 0XEFFFFFF;
    }
}
