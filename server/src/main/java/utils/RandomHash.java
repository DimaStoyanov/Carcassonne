package utils;

import java.util.Locale;
import java.util.Random;

/**
 * Created by Dmitrii Stoianov
 */


public final class RandomHash {

    private RandomHash() {
    }

    private static final Random random = new Random();

    private static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String lower = upper.toLowerCase(Locale.ROOT);

    private static final String digits = "0123456789";

    private static final String alphanum = upper + lower + digits;


    public static String nextHash(int length) {
        char buf[] = new char[length];
        char symbols[] = alphanum.toCharArray();
        for (int i = 0; i < length; i++) {
            buf[i] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }

}