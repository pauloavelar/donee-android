package me.avelar.donee.controller;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import me.avelar.donee.model.Session;
import me.avelar.donee.util.DummyJsonReader;
import me.avelar.donee.util.IntentFactory;

public final class WelcomeLogic {

    public enum LoginStatus { LOGIN_SUCCEDED, LOGIN_FAILED, NETWORK_ERROR, SERVER_ERROR, UNKNOWN }

    public static final String INTENT_RESPONSE = "response";
    public static final String INTENT_SESSION  = "session";
    public static final String INTENT_EMAIL    = "email";

    private static final String PAULOAVELAR_DOMAIN = "@pauloavelar.com";

    @SuppressWarnings("UnusedParameters")
    public static void tryLogin(final Context context, String email, String password) {
        LoginStatus loginStatus = LoginStatus.LOGIN_FAILED;
        Intent intent = IntentFactory.create(IntentFactory.Type.MAIN_LOGIN_PERFORMED);

        // DEMO version: accepts any email addresses ending with @pauloavelar.com
        // Other domains can be used to show implemented error messages
        if (email.endsWith(PAULOAVELAR_DOMAIN) && email.length() > PAULOAVELAR_DOMAIN.length()) {
            Session session = DummyJsonReader.loadSession(context);
            if (session != null && session.isValid()) {
                SessionManager.storeSession(context, session);
                intent.putExtra(WelcomeLogic.INTENT_SESSION, session);
                loginStatus = LoginStatus.LOGIN_SUCCEDED;
            }
        }
        intent.putExtra(WelcomeLogic.INTENT_RESPONSE, loginStatus.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}