package me.avelar.donee.controller;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;

import me.avelar.donee.dao.FormDao;
import me.avelar.donee.model.Form;
import me.avelar.donee.model.Session;
import me.avelar.donee.util.ConnectivityHelper;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.web.DoneeService;
import me.avelar.donee.web.DoneeService.RequestStatus;
import me.avelar.donee.web.ServiceFactory;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public final class FormsLogic {

    public static final String EXTRA_FORM = "FORM";

    public static void loadCurrentUserForms(Context context) {
        loadCurrentUserForms(context, false);
    }

    public static void loadCurrentUserForms(Context context, boolean fromServer) {
        if (fromServer || !SessionManager.hasCurrentUserEverSynced(context)) {
            // user never synced forms -- force sync or show error
            if (ConnectivityHelper.isConnectedToInternet(context)) {
                SessionManager.validateCurrent(context);
                loadFormsFromServer(context);
            } else {
                sendBroadcast(context, RequestStatus.NO_CONNECTION);
            }
        } else {
            // user has previously synced forms -- show cached data
            Session currentSession = SessionManager.getLastSession(context);
            ArrayList<Form> forms = FormDao.find(context, currentSession.getUser());
            sendBroadcast(context, RequestStatus.SUCCEEDED, forms);
        }
    }

    private static void loadFormsFromServer(@NonNull final Context context) {
        DoneeService  service = ServiceFactory.getService(context);
        final Session session = SessionManager.getLastSession(context);
        service.listForms(session.getId(), new Callback<ArrayList<Form>>() {
            @Override
            public void success(ArrayList<Form> forms, Response response) {
                FormDao.removeAllFromUser(context, session.getUser());
                FormDao.insert(context, forms, session.getUser());
                sendBroadcast(context, RequestStatus.SUCCEEDED, forms);
            }

            @Override
            public void failure(RetrofitError error) {
                switch (error.getKind()) {
                    case NETWORK:
                        sendBroadcast(context, RequestStatus.NO_CONNECTION);
                        break;
                    case HTTP:
                        sendBroadcast(context, RequestStatus.SERVER_ERROR);
                        break;
                    default:
                        sendBroadcast(context, RequestStatus.UNKNOWN_ERROR);
                        break;
                }
            }
        });
    }

    private static void sendBroadcast(Context c, RequestStatus status) {
        sendBroadcast(c, status, null);
    }

    private static void sendBroadcast(Context c, RequestStatus status, ArrayList<Form> data) {
        Intent intent;
        switch (status) {
            case SUCCEEDED:
                intent = IntentFactory.create(IntentFactory.Type.FORMS_LOAD_FINISHED);
                intent.putParcelableArrayListExtra(IntentFactory.EXTRA_DATA, data);
                break;
            default: // so far no action
                intent = IntentFactory.create(IntentFactory.Type.FORMS_LOAD_ERROR);
                break;
        }
        intent.putExtra(IntentFactory.EXTRA_DETAIL, status.toString());
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }

}