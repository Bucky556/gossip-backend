package code.uz.util;

import java.util.regex.Pattern;

public class PhoneUtil {
    public static boolean isPhone(String phone) {
        return Pattern.matches("^998\\d{9}$", phone);
    }
}
