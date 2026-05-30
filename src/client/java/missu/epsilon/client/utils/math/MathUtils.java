package missu.epsilon.client.utils.math;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;

public class MathUtils {
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    public static float toRadians(float angle) {
        return angle * (float) Math.PI / 180F;
    }

    public static double getRandomInRange(double min, double max) {
        SecureRandom random = new SecureRandom();
        return min == max ? min : random.nextDouble() * (max - min) + min;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
    /**
     * @param value value.
     * @return rounded value.
     */
    private static float roundToNearestTenth(float value) {
        return Math.round(value * 10f) / 10f;
    }

    /**
     * @param value value.
     * @return rounded value.
     */
    public static float roundToPrecisionTen(float value) {
        return roundToNearestTen(value);
    }

    /**
     * @param value value.
     * @return rounded value.
     */
    private static float roundToNearestTen(float value) {
        BigDecimal bigDecimal = new BigDecimal(Float.toString(value));
        BigDecimal step = new BigDecimal("0.1");
        BigDecimal divided = bigDecimal.divide(step, 0, RoundingMode.HALF_UP);
        BigDecimal result = divided.multiply(step);

        return result.floatValue();
    }

    /**
     * @param value value.
     * @return rounded value.
     */
    public static float roundToPrecisionHundred(float value) {
        return roundToNearestHundred(value);
    }

    public static float roundToPrecisionHundred(double value) {
        return roundToNearestHundred((float)value);
    }

    /**
     * @param value value.
     * @return rounded value.
     */
    private static float roundToNearestHundred(float value) {
        BigDecimal bigDecimal = new BigDecimal(Float.toString(value));
        BigDecimal step = new BigDecimal("0.01");
        BigDecimal divided = bigDecimal.divide(step, 0, RoundingMode.HALF_UP);
        BigDecimal result = divided.multiply(step);

        return result.floatValue();
    }

    /**
     * @param value value.
     * @return rounded value.
     */
    public static float roundToPrecisionThousand(float value) {
        return roundToNearestThousand(value);
    }

    public static float roundToPrecisionThousand(double value) {
        return roundToNearestThousand((float)value);
    }

    private static float roundToNearestThousand(float value) {
        BigDecimal bigDecimal = new BigDecimal(Float.toString(value));
        BigDecimal step = new BigDecimal("0.005");
        BigDecimal divided = bigDecimal.divide(step, 0, RoundingMode.HALF_UP);
        BigDecimal result = divided.multiply(step);

        return result.floatValue();
    }
    public static int floor_double(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }
}
