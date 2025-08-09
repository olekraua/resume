package net.devstudy.resume;

public final class Constants {
    private Constants() {}

    /** Behalte "USER", falls du mit hasAuthority("USER") arbeitest.
     *  Empfehlung: mittelfristig auf ROLE_USER + hasRole("USER") umstellen. */
    public static final String USER = "USER";

    public static final String[] EMPTY_ARRAY = {};

    /** Bildtypen mit typsicheren Größen (klein/gross) */
    public enum UiImageType {
        AVATARS(Size.of(110, 110), Size.of(400, 400)),
        CERTIFICATES(Size.of(142, 100), Size.of(900, 400));

        public record Size(int width, int height) {
            public static Size of(int w, int h) { return new Size(w, h); }
        }

        private final Size small;
        private final Size large;

        UiImageType(Size small, Size large) {
            this.small = small;
            this.large = large;
        }

        public String folderName() { return name().toLowerCase(); }
        public Size small()        { return small; }
        public Size large()        { return large; }
    }

    /** Übergangsweise beibehalten; später per Properties überschreiben. */
    @Deprecated(forRemoval = true)
    public static final class UI {
        public static final int MAX_PROFILES_PER_PAGE = 10;
        private UI() {}
    }
}
