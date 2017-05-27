package me.avelar.donee.web;

public class UrlRepository {
    // Base URLs, change here if the server changes
    public static final String WEB_BASE = "https://donee.avelar.me";
    public static final String API_BASE = "https://api.donee.avelar.me/app";

    // Web-specific URLs
    public static final String FORGOT_PASSWORD = WEB_BASE + "/login#recovery";
    public static final String DONEE_TWITTER = "http://twitter.com/doneeforms";

    // API specific URLs (more on me.avelar.donee.web.DoneeService)
    public static final String ID_PLACEHOLDER = "%d";
    // DEMO version: the user photo (not implemented in full version) is my Stack Overflow photo
    public static final String USER_PHOTO = "https://i.stack.imgur.com/FkjHz.jpg?s=128&g=1";
    public static final String FORM_ICON  = API_BASE + "/form/" + ID_PLACEHOLDER + "/icon";

    public static String getFormIcon(String formId) {
        try {
            long longId = Long.parseLong(formId);
            return String.format(FORM_ICON, longId);
        } catch (Exception e) {
            return null;
        }

    }

}
