package me.avelar.donee.controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.Date;

import me.avelar.donee.R;
import me.avelar.donee.dao.DoneeDbHelper;
import me.avelar.donee.dao.SessionDAO;
import me.avelar.donee.model.Session;
import me.avelar.donee.model.User;
import me.avelar.donee.util.ConnectivityHelper;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.view.Updatable;
import me.avelar.donee.web.DoneeService;
import me.avelar.donee.web.ServiceFactory;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

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

    public static void validateCurrent(final Context context) {
        if (!ConnectivityHelper.isConnectedToInternet(context)) return;

        Session session = getLastSession(context);
        if (session == null) return;

        DoneeService service = ServiceFactory.getService(context);
        service.validateSession(session.getId(), new Callback<Session>() {
            @Override
            public void success(Session session, Response response) {
                if (!session.isValid()) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, R.string.session_expired, Toast.LENGTH_LONG).show();
                        }
                    });
                    logoutCurrentSession(context);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                // the app will not log the user out unless it is 100% sure the session is invalid
            }
        });
    }

}
