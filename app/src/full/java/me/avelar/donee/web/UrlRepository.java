package me.avelar.donee.web;

import java.util.Locale;

public class UrlRepository {
    // Base URLs, change here if the server changes
    private static final String WEB_BASE = "https://donee.avelar.me";
    private static final String API_BASE = "https://api.donee.avelar.me/app";

    // Web-specific URLs
    public static final String FORGOT_PASSWORD = WEB_BASE + "/login#recovery";
    public static final String DONEE_TWITTER = "http://twitter.com/doneeforms";

    // API specific URLs (more on me.avelar.donee.web.DoneeService)
    private static final String ID_PLACEHOLDER = "%s";
    private static final String USER_PHOTO = API_BASE + "/user/" + ID_PLACEHOLDER + "/photo";
    private static final String FORM_ICON  = API_BASE + "/form/" + ID_PLACEHOLDER + "/icon";

    public static String getFormIconUrl(String formId) {
        return String.format(Locale.US, FORM_ICON, formId);
    }

    public static String getUserPhotoUrl(String userId) {
        return String.format(Locale.US, USER_PHOTO, userId);
    }
}
