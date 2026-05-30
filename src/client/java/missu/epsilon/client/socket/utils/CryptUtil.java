package missu.epsilon.client.socket.utils;

import tech.skidonion.obfuscator.annotations.NativeObfuscation;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@NativeObfuscation
public class CryptUtil {
    public static class Base64Crypt {

        @NativeObfuscation
        public static byte[] encrypt(byte[] message) {
            return Base64.getEncoder().encode(message);
        }

        @NativeObfuscation
        public static byte[] decrypt(byte[] message) {
            return Base64.getDecoder().decode(message);
        }

        @NativeObfuscation
        public static String encrypt(String message) {
            return Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static class Deflate { // 压缩

        @NativeObfuscation
        public static String decompress(byte[] compressed) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(compressed);
                InflaterInputStream inflaterStream = new InflaterInputStream(inputStream, new Inflater());
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inflaterStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }

                inflaterStream.close();
                return outputStream.toString(StandardCharsets.UTF_8);
            } catch (IOException ignored) {
            }
            return "ERROR";
        }
    }

    public static class RSA {
        private static final PublicKey PUBLIC_KEY;
        private static final PrivateKey PRIVATE_KEY;

        static {
            try {
                final byte[] pubKey = Base64Crypt.decrypt("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0ZhIhpsb8KaRnGtNW4EPC37GJGYKxvXHFSRzaLTgC+ZBoG8ub/s5hNfJ2w1YcQ/Czj4aakcaWMsSYARGLRmoXtLqZMZUFfDeNYfkwlsS/godxs2Yym3zWXu6KeGZ4BWwAoSC+cVc9SyH7TB7EZj8G9APU0M7pv5/XEx93ulaidD7i8R+EVmACcKrT6JCNWCF9N4Ond+lrL+Xm3L3fyWY7V+iRMe0EBDIJBYs58IgOYC/uAAg0VmRG3R5CSG7HjHVS/FPmd0Askp/Ftd+cI4STSkkjV2C3c0TzyugUo2kMG5Epy28YrRcokZ02bJbcbWJoy0ZJO3XTOkToORJcrd7PQIDAQAB".getBytes(StandardCharsets.UTF_8));
                final X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
                KeyFactory keyFactoryPub = KeyFactory.getInstance("RSA");
                PUBLIC_KEY = keyFactoryPub.generatePublic(x509KeySpec);

                final byte[] priKey = Base64Crypt.decrypt("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDRmEiGmxvwppGca01bgQ8LfsYkZgrG9ccVJHNotOAL5kGgby5v+zmE18nbDVhxD8LOPhpqRxpYyxJgBEYtGahe0upkxlQV8N41h+TCWxL+Ch3GzZjKbfNZe7op4ZngFbAChIL5xVz1LIftMHsRmPwb0A9TQzum/n9cTH3e6VqJ0PuLxH4RWYAJwqtPokI1YIX03g6d36Wsv5ebcvd/JZjtX6JEx7QQEMgkFiznwiA5gL+4ACDRWZEbdHkJIbseMdVL8U+Z3QCySn8W135wjhJNKSSNXYLdzRPPK6BSjaQwbkSnLbxitFyiRnTZsltxtYmjLRkk7ddM6ROg5Elyt3s9AgMBAAECggEAFhWnMrO+Tm2msyYlSQqAxNPPWf98jDlSiFpGZNaGMwrIiHiemoMMrQA1z0SJOKSSALxPeTpTvt5q6b4ZjOACuzcbJpI1QviV2WsfwhDpn+X9/DgmrNHIDfo5A/e3CIMQgDtpN5tBr73I9segeBq4GdAjBUF01CRF1DplGutYtGOlWSQHdBhDHDsnuXJTFuczMkUDQu6mGFZzybF6BBV9Jo+uV0JVP4WKhP5fY/2YecYngP3ra1E0iPzllZiygHhDny39Nij635vgj0PQvbb+zKR5bkHvivDOsBK1QWMstpiAx9ot3S5MkOc8HmwstIFqZbtwmB7lwZiW7l0vEak96wKBgQDVBrMHfrPqiO+PVlvlsH16VaGgSW/k1diFCp3Yp3ofabKGH2J/QZ+zc60v8kCsQnaXEgh59ogjTUTRpYNAYzWIzj5RkbWB9xlH9Tj1vnhp4c5XwhAEseBhiy5PpJ3FtfGO1Ez5r5f7CSk8R6BEUrnqZkduW5mH+C2TMB5M64+qawKBgQD74GF/dyslfaBqRAwLxJ8NRdkTvya9ObdM/FTJ0NkxtEihVD9uA4SY/1cWMQZuXzm2M7IzbDDgQfuVbZS50hJMLZBtQrPGHCA/q881itETCHSLL9P/QrE6b2LTCgtyYqRTfvLctfWYSknDFXrLj9NF2iAGVVApktCmdaAjdY2q9wKBgG+3KLI4hx/rGj7AWkc1bEh4md9WQ+DtKLR0odzZz5ijtKTT1rLH9nmEo4FueO4dmNWW9m0HldN1lS6grxlYLd4F0vayBE8ZB+ANwQZXEgztB2pMclwT8IkXFX1c5gSmavytZ4TVyk0zO7XKFkleXPOJGdLNJNICm9wBsxJBwsDrAoGAWYdtQICcXowj/AHKAljWbEJaodE9NN+SDxRWExJgUXGSq32lujYX6MZCL0onzg2dFu2TNwIK8XuisH+t8MsB5AACKUQfYJjYwakRUyfPnyNPQbYzdbVAB9RV3Uvhu7viSyeBrgAjlwk45NX6uuaZo2egIIAsqUpdTf3beR8sxwsCgYA7sF64CjAY5DLeN/kxy+M+r1bhkZZNptjYDQu0sbv0LQmDIspvWjXs24JdAs7DCCiH8sHuul4mDa49KPOjWeiIDQwllQvUvrfH4l1YFkEg8p/XoE+ZdVXGvYqtxyxbOYLb8R7CD+5n50W4uyW0zYx8ZIzXcqChUcoEiso2Mvl2Og==".getBytes(StandardCharsets.UTF_8));
                final PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
                KeyFactory keyFactoryPri = KeyFactory.getInstance("RSA");
                PRIVATE_KEY = keyFactoryPri.generatePrivate(pkcs8KeySpec);
            } catch (Throwable e) {
                throw new RuntimeException("初始化通讯密钥失败，请联系开发者");
            }
        }

        @SuppressWarnings("CallToPrintStackTrace")
        @NativeObfuscation
        public static byte[] encryptByPublicKey(byte[] data) {
            try {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, PUBLIC_KEY);
                return Base64Crypt.encrypt(cipher.doFinal(data));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            return new byte[]{};
        }

        @SuppressWarnings("CallToPrintStackTrace")
        @NativeObfuscation
        public static byte[] decryptByPrivateKey(byte[] data) {
            try {
                byte[] decodedData = Base64Crypt.decrypt(data);

                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY);
                return cipher.doFinal(decodedData);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            return new byte[]{};
        }
    }
}
