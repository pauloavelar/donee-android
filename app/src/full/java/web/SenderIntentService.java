package me.avelar.donee.web;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.avelar.donee.R;
import me.avelar.donee.controller.SessionManager;
import me.avelar.donee.dao.CollectionDAO;
import me.avelar.donee.model.Collection;
import me.avelar.donee.model.Session;
import me.avelar.donee.util.ConnectivityHelper;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.util.NotificationFactory;
import me.avelar.donee.util.NotificationFactory.Type;
import retrofit.RetrofitError;

@SuppressWarnings("unused")
public class SenderIntentService extends IntentService {

    public static final String LAST_SYNC    = "LAST_SYNC";
    public static final String ONGOING_SYNC = "ONGOING_SYNC";
    public static final long   TWO_MINUTES  = 2 * 60 * 1000;

    public static final String ITEM_SENT    = "ITEM_SENT";
    public static final String SYNC_FAILED  = "SYNC_FAILED";

    private DoneeService mService;

    public SenderIntentService() {
        super("SenderIntentService");
    }

    public SenderIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        Session session = SessionManager.getLastSession(context);
        String of = context.getResources().getString(R.string.of);

        if (session == null || session.getUser() == null) {
            sendBroadcast(context, SYNC_FAILED);
            return;
        }

        mService = ServiceFactory.getService(context);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = NotificationFactory.create(context, Type.ONGOING_SYNC);

        List<Collection> collections = CollectionDAO.findOutbox(context, session.getUser());
        List<Collection> errored     = new ArrayList<>();

        // disabling the submit menu option
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(ONGOING_SYNC, true);
        editor.putLong(LAST_SYNC, new Date().getTime());
        editor.apply();

        int completed = 0, total = collections.size();
        for (int i = 0; i < total; i++) {
            // getting reference to the current item
            Collection collection = CollectionDAO.findComplete(context, collections.get(i));

            // updating the notification
            builder.setProgress(total, completed, false);
            builder.setContentInfo(completed + " " + of + " " + total);
            nm.notify(NotificationFactory.ONGOING_SYNC_ID, builder.build());

            // checking if lost internet or if failed more than 3 forms
            if (!ConnectivityHelper.isConnectedToInternet(context) || errored.size() >= 3) break;

            // try to send the collections
            boolean success = trySending(session, collection);

            if (success) {
                // update progress, delete local version and notify the UI
                completed++;
                CollectionDAO.delete(context, collection);
                sendBroadcast(context, ITEM_SENT, collection.getLocalId());
            } else {
                // add the item to the error array (for future retries)
                errored.add(collection);
            }
        }

        // retrying the failed forms
        for (Collection retry : errored) {
            if (!trySending(session, retry)) break;
            completed++;
            CollectionDAO.delete(context, retry);
            sendBroadcast(context, ITEM_SENT, retry.getLocalId());
            builder.setProgress(total, completed, false);
            builder.setContentInfo(completed + " " + of + " " + total);
            nm.notify(NotificationFactory.ONGOING_SYNC_ID, builder.build());
        }

        // sending the final notification and dismissing the old one
        nm.cancel(NotificationFactory.ONGOING_SYNC_ID);
        Type notificationType = (completed == total ? Type.FINISHED_SYNC_OK : Type.FINISHED_SYNC_ERROR);
        builder = NotificationFactory.create(context, notificationType);
        nm.notify(NotificationFactory.FINISHED_SYNC_ID, builder.build());

        // reenabling the submit menu option
        editor.putBoolean(ONGOING_SYNC, false);
        editor.apply();
    }

    private boolean trySending(Session session, Collection collection) {
        SenderResponse response;
        try {
            response = mService.submitForm(session.getId(), collection);
            return response.isSuccessful();
        } catch (RetrofitError error) { return false; }
    }

    private void sendBroadcast(Context c, String type) {
        sendBroadcast(c, type, null);
    }

    private void sendBroadcast(Context c, String type, String collectionId) {
        Intent intent;
        if (type.equals(ITEM_SENT)) {
            intent = IntentFactory.create(IntentFactory.Type.COLLECTION_SENT);
            intent.putExtra(IntentFactory.EXTRA_DATA, collectionId);
            LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
        }
    }

}
