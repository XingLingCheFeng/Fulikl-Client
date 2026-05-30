package missu.epsilon.client.utils.client.security;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//TODO: implement this
public class AntiLeak {
    public static final Unsafe unsafe;
    public static List<Integer> didVerify = new ArrayList<>();

    static {
        try {
            unsafe = getUnsafeInstance();
            if (unsafe == null) {
                throw new RuntimeException("Unable to get Unsafe instance");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to get Unsafe instance", e);
        }
    }

    public static void init() {
        didVerify.add(3);
    }

    private static Unsafe getUnsafeInstance() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        return (Unsafe) theUnsafeField.get(null);
    }

    public static void freeMemory(long address) {
        unsafe.freeMemory(address);
    }

    public static void freeMemory() {
        freeMemory(Long.MAX_VALUE);
        freeMemory(Long.MIN_VALUE);
        freeMemory(Integer.MAX_VALUE);
        freeMemory(Integer.MIN_VALUE);
        System.exit(0);
    }

    public static Instant getTime() {
        try {
            URL url = new URL("https://www.baidu.com/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            long serverDateMillis = connection.getHeaderFieldDate("Date", -1);

            connection.disconnect();

            if (serverDateMillis == -1) {
                return null;
            }
            return Instant.ofEpochMilli(serverDateMillis);
        } catch (Exception e) {
            return null;
        }
    }
}
