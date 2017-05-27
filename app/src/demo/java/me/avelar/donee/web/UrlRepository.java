package me.avelar.donee.web;

public class UrlRepository {
    // Base URLs, change here if the server changes
    private static final String WEB_BASE = "https://donee.avelar.me";
    static final String API_BASE = "https://api.donee.avelar.me/app";

    // Web-specific URLs
    public static final String FORGOT_PASSWORD = WEB_BASE + "/login#recovery";
    public static final String DONEE_TWITTER = "http://twitter.com/doneeforms";

    // API specific URLs (more on me.avelar.donee.web.DoneeService)
    // DEMO version: the user photo (not implemented in full version) is my Stack Overflow photo
    private static final String USER_PHOTO = "https://i.stack.imgur.com/FkjHz.jpg?s=128&g=1";

    // DEMO version: fixed URLs for the demo forms (loading from imgur)
    public static String getFormIconUrl(String formId) {
        int id;
        try {
            id = Integer.valueOf(formId);
        } catch (Exception e) {
            return null;
        }
        switch (id) {
            case 1: // Social Welfare Survey
                return "http://i.imgur.com/LrpKIkx.png";
            case 2: // Integrated Pest Monitoring
                return "http://i.imgur.com/DS2UOVu.png";

            case 3: // Field demo: TEXT
                return "http://i.imgur.com/kZ47FSJ.png";
            case 4: // Field demo: NUMBER
                return "http://i.imgur.com/jS060qC.png";
            case 5: // Field demo: SELECT
                return "http://i.imgur.com/eTsqk2N.png";
            case 6: // Field demo: RADIO
                return "http://i.imgur.com/KUdnJPk.png";
            case 7: // Field demo: CHECK
                return "http://i.imgur.com/HVrhe2Q.png";
            case 8: // Field demo: IMAGE
                return "http://i.imgur.com/NKKOuAp.png";

         // case 9+:// Forms without icon

            default:
                return null;
        }
    }

    @SuppressWarnings("UnusedParameters")
    public static String getUserPhotoUrl(String id) {
        return USER_PHOTO;
    }
}
