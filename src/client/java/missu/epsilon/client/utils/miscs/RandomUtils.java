package missu.epsilon.client.utils.miscs;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {
    private static final Random random = new Random();

    public static boolean nextBoolean() {
        return new Random().nextBoolean();
    }

    public static int nextInt(int startInclusive, int endExclusive) {
        return endExclusive - startInclusive <= 0 ? startInclusive : startInclusive + random.nextInt(endExclusive - startInclusive);
    }

    public static double nextDouble(double startInclusive, double endInclusive) {
        return startInclusive == endInclusive || endInclusive - startInclusive <= 0.0 ? startInclusive : startInclusive + (endInclusive - startInclusive) * Math.random();
    }

    public static float nextFloat(float startInclusive, float endInclusive) {
        return startInclusive == endInclusive || endInclusive - startInclusive <= 0f ? startInclusive : (float) (startInclusive + (endInclusive - startInclusive) * Math.random());
    }

    public static String random(int length, String chars) {
        return random(length, chars.toCharArray());
    }

    public static String random(int length, char[] chars) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(chars[new Random().nextInt(chars.length)]);
        }
        return stringBuilder.toString();
    }

    public static String randomNumber(int length) {
        return randomString(length, "123456789");
    }

    public static String randomString(int length) {
        return randomString(length, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    }

    public static String randomString(int length, String chars) {
        return randomString(length, chars.toCharArray());
    }

    public static String randomString(int length, char[] chars) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(chars[random.nextInt(chars.length)]);
        }
        return stringBuilder.toString();
    }

    public static double getRandom(double min, double max) {
        if (min == max) {
            return min;
        }

        if (min > max) {
            final double temp = min;
            min = max;
            max = temp;
        }

        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static float getRandom(float min, float max) {
        if (min == max) {
            return min;
        }

        if (min > max) {
            final float temp = min;
            min = max;
            max = temp;
        }

        return ThreadLocalRandom.current().nextFloat(min, max);
    }

    public static int getRandom(int min, int max) {
        if (min == max) {
            return min;
        }

        if (min > max) {
            final int temp = min;
            min = max;
            max = temp;
        }

        return ThreadLocalRandom.current().nextInt(min, max);
    }
}