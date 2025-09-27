package code.uz.util;

import java.util.Random;

public class RandomUtil {
    public static final Random random = new Random();

    public static String generateRandomCode() {
        return String.valueOf(random.nextInt(1000, 9999));
    }
}
