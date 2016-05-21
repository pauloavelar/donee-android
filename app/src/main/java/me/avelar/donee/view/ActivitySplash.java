package me.avelar.donee.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import me.avelar.donee.controller.SessionManager;
import me.avelar.donee.controller.WelcomeLogic;
import me.avelar.donee.model.Session;

public class ActivitySplash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long startTime = SystemClock.currentThreadTimeMillis();
        final Class<?> nextActivity;
        final Session session = SessionManager.getLastSession(this);
        if (session == null) {
            nextActivity = ActivityWelcome.class;
        } else {
            nextActivity = ActivityMain.class;
        }
        long ellapsedTime = SystemClock.currentThreadTimeMillis() - startTime;
        int delay = (int)(1000 - ellapsedTime > 0 ? 1000 - ellapsedTime : 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ActivitySplash.this, nextActivity);
                if (session != null) {
                    intent.putExtra(WelcomeLogic.INTENT_SESSION, session);
                }
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, delay);
    }

}
