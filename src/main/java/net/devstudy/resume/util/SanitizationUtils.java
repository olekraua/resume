package net.devstudy.resume.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public final class SanitizationUtils {
    private SanitizationUtils() {}
    public static String cleanToPlainText(String input) {
        if (input == null) return null;
        return Jsoup.clean(input, Safelist.none());
    }
}