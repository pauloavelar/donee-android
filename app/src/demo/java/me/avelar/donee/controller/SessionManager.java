package me.avelar.donee.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Date;

import me.avelar.donee.dao.DoneeDbHelper;
import me.avelar.donee.dao.SessionDAO;
import me.avelar.donee.model.Session;
import me.avelar.donee.model.User;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.view.Updatable;

public final class SessionManager {

    public static final String PREF_LAST_SESSION = "lastSession";

    public static void storeSession(Context context, Session session) {
        SessionDAO.insert(context, session);
        setLastSession(context, session, false);
    }

    private static void setLastSession(@NonNull Context context, @NonNull Session session) {
        setLastSession(context, session, true);
    }

    private static void setLastSession(@NonNull Context context,
                                       @NonNull Session session, boolean updateLastUsed) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(PREF_LAST_SESSION, session.getId());
        editor.apply();
        if (updateLastUsed) {
            session.setLastUsed(new Date());
            SessionDAO.update(context, session);
        }
    }

    public static Session getLastSession(Context context) {
        if (context == null) return null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return getSession(context, sp.getString(PREF_LAST_SESSION, null));
    }

    public static Session getSession(Context context, String sessionId) {
        return SessionDAO.find(context, DoneeDbHelper.C_SESSION_ID, sessionId);
    }

    public static boolean switchTo(@NonNull Context context, User user) {
        Session session = SessionDAO.find(context, DoneeDbHelper.C_SESSION_USER, user.getId());
        if (session != null) {
            setLastSession(context, session);
            return true;
        } else return false;
    }

    public static void logoutCurrentSession(@NonNull Context context) {
        logoutCurrentSession(context, false);
    }

    public static void logoutCurrentSession(@NonNull Context context, boolean forever) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SessionDAO.delete(context, sp.getString(PREF_LAST_SESSION, null));
        Session session = SessionDAO.findLastUsed(context);

        Intent nextAction;
        if (session != null) {
            setLastSession(context, session);
            nextAction = IntentFactory.create(IntentFactory.Type.MAIN_REFRESH_USER);
        } else {
            nextAction = IntentFactory.create(IntentFactory.Type.MAIN_GO_TO_LOGIN);
            if (forever) nextAction.putExtra(IntentFactory.EXTRA_DETAIL, true);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(nextAction);
    }

    public static void updateFragmentState(Updatable fragment) {
        if (fragment != null) fragment.updateSessionData();
    }

    public static String getCurrentUserName(@NonNull Context context) {
        Session session = getLastSession(context);
        if (session == null || session.getUser() == null) return null;
        return session.getUser().getName();
    }

    public static boolean hasCurrentUserEverSynced(@NonNull Context context) {
        Session session = getLastSession(context);
        return session != null && session.getUser() != null
               && session.getUser().getLastSynced() > 0;
    }

    // no need to do anything, but keeping here to keep compatibility with other flavors
    @SuppressWarnings("UnusedParameters")
    public static void validateCurrent(final Context context) { }

}
