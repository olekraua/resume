package net.devstudy.resume;

import java.util.Locale;

/**
 * @author devstudy
 * @see http://devstudy.net
 */

public final class Constants {
    private Constants() {}

    public static final String USER = "USER";
    public static final String[] EMPTY_ARRAY = new String[0];

    /**
     * @author devstudy
     * @see http://devstudy.net
     */
    public enum UIImageType {
        AVATARS(110, 110, 400, 400),
        CERTIFICATES(142, 100, 900, 400);

        private final int smallWidth;
        private final int smallHeight;
        private final int largeWidth;
        private final int largeHeight;

        UIImageType(int smallWidth, int smallHeight, int largeWidth, int largeHeight) {
            this.smallWidth = smallWidth;
            this.smallHeight = smallHeight;
            this.largeWidth = largeWidth;
            this.largeHeight = largeHeight;
        }

        public String getFolderName() {
            return name().toLowerCase(Locale.ROOT); // locale-sicher
        }
        public int getSmallWidth()  { return smallWidth; }
        public int getSmallHeight() { return smallHeight; }
        public int getLargeWidth()  { return largeWidth; }
        public int getLargeHeight() { return largeHeight; }
    }

    public static final class UI {
        private UI() {}
        public static final int MAX_PROFILES_PER_PAGE = 10;
    }
}
