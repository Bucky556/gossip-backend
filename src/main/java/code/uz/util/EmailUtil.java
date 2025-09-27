package code.uz.util;

import java.util.regex.Pattern;

public class EmailUtil {
    public static boolean isEmail(String email) {
        return Pattern.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", email);
    }
}
