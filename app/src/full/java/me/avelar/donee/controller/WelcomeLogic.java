package me.avelar.donee.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;

import me.avelar.donee.model.LoginRequest;
import me.avelar.donee.model.Session;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.web.DoneeService;
import me.avelar.donee.web.ServiceFactory;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public final class WelcomeLogic {

    public enum LoginStatus { LOGIN_SUCCEDED, LOGIN_FAILED, NETWORK_ERROR, SERVER_ERROR, UNKNOWN }

    public static final String INTENT_RESPONSE = "response";
    public static final String INTENT_SESSION  = "session";
    public static final String INTENT_EMAIL    = "email";

    public static void tryLogin(final Context context, String email, String password) {
        DoneeService service = ServiceFactory.getService(context);
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceAlias = (Build.MANUFACTURER + " " + Build.MODEL).trim();

        LoginRequest request = new LoginRequest(email, password, tm.getDeviceId(), deviceAlias);
        service.performLogin(request, new Callback<Session>() {
            @Override
            public void success(Session session, Response response) {
                Intent intent = IntentFactory.create(IntentFactory.Type.MAIN_LOGIN_PERFORMED);
                LoginStatus loginStatus;
                if (session.isValid()) {
                    SessionManager.storeSession(context, session);
                    loginStatus = LoginStatus.LOGIN_SUCCEDED;
                    intent.putExtra(WelcomeLogic.INTENT_SESSION, session);
                } else {
                    loginStatus = LoginStatus.LOGIN_FAILED;
                }
                intent.putExtra(WelcomeLogic.INTENT_RESPONSE, loginStatus.toString());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void failure(RetrofitError error) {
                Intent intent = IntentFactory.create(IntentFactory.Type.MAIN_LOGIN_PERFORMED);
                LoginStatus loginStatus;
                switch (error.getKind()) {
                    case NETWORK:
                        loginStatus = LoginStatus.NETWORK_ERROR;
                        break;
                    case HTTP:
                        loginStatus = LoginStatus.SERVER_ERROR;
                        break;
                    default:
                        loginStatus = LoginStatus.UNKNOWN;
                        break;
                }
                intent.putExtra(WelcomeLogic.INTENT_RESPONSE, loginStatus.toString());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
    }

}