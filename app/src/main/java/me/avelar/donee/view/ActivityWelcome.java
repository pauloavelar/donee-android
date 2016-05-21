package me.avelar.donee.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.avelar.donee.R;
import me.avelar.donee.controller.Validator;
import me.avelar.donee.controller.WelcomeLogic;
import me.avelar.donee.controller.WelcomeLogic.LoginStatus;
import me.avelar.donee.model.ValidationRule;
import me.avelar.donee.util.IntentFactory;
import me.avelar.donee.util.RuleRepository;
import me.avelar.donee.web.UrlRepository;

public class ActivityWelcome extends Activity
       implements View.OnClickListener, View.OnFocusChangeListener {

    private enum State { ERROR_HIDDEN, ERROR_VISIBLE, LOADING }

    private State mState = State.ERROR_HIDDEN;
    private int mErroredViewId = 0;
    private String mErrorMessage;
    private List<View> fields;

    private View mVwLogo, mVwBox, mVwLogin, mVwLoading;
    private EditText mEtEmail, mEtPassword;
    private TextView mVwError;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LoginStatus status = LoginStatus.valueOf(intent.getExtras()
                .getString(WelcomeLogic.INTENT_RESPONSE, LoginStatus.UNKNOWN.toString()));
            parseStatus(status, intent);
        }

        private void parseStatus(LoginStatus status, Intent intent) {
            switch (status) {
                case LOGIN_SUCCEDED:
                    Intent next = new Intent(ActivityWelcome.this, ActivityMain.class);
                    next.putExtras(intent.getExtras());
                    startActivity(next);
                    finish();
                    break;
                case LOGIN_FAILED:
                    showError(getResources().getString(R.string.login_failed));
                    break;
                case NETWORK_ERROR:
                    showError(getResources().getString(R.string.network_error));
                    break;
                case SERVER_ERROR:
                    showError(getResources().getString(R.string.server_error));
                    break;
                case UNKNOWN:
                    showError(getResources().getString(R.string.unknown_error));
                    break;
            }
        }

        private void showError(String message) {
            updateState(State.ERROR_VISIBLE, 0, message);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        fixActivityPadding();

        // assigning most used views to variables
        mVwLogo  = findViewById(R.id.welcome_logo);
        mVwBox   = findViewById(R.id.welcome_box);
        mVwLogin = findViewById(R.id.welcome_btn_login);
        mVwLoading = findViewById(R.id.welcome_loading);
        mEtEmail    = (EditText) findViewById(R.id.welcome_email);
        mEtPassword = (EditText) findViewById(R.id.welcome_password);
        mVwError    = (TextView) findViewById(R.id.welcome_error);

        // setting up click listeners for the views below
        int viewIds[] = { R.id.welcome_forgot, R.id.welcome_footer, R.id.welcome_error };
        for(int viewId : viewIds) findViewById(viewId).setOnClickListener(this);
        mVwLogin.setOnClickListener(this);

        // setting up focus change listeners for activity animation
        mEtEmail.setOnFocusChangeListener(this);
        mEtPassword.setOnFocusChangeListener(this);

        // setting up validation rules for the views
        RuleRepository ruleRepository = RuleRepository.getInstance(this);
        mEtEmail.setTag(ruleRepository.getRuleById(R.id.welcome_email));
        mEtPassword.setTag(ruleRepository.getRuleById(R.id.welcome_password));

        // setting up the fields list for data validation
        fields = new ArrayList<>();
        fields.add(mEtEmail);
        fields.add(mEtPassword);

        // syncing activity state
        if (savedInstanceState != null) {
            mState = State.valueOf(savedInstanceState
                        .getString("state", State.ERROR_HIDDEN.toString()));
            mErroredViewId = savedInstanceState.getInt("erroredViewId", 0);
            mErrorMessage = savedInstanceState.getString("message");
            syncState();
        } else {
            String email = getIntent().getStringExtra(WelcomeLogic.INTENT_EMAIL);
            if (email != null) mEtEmail.setText(email);
        }

        // registering BroadcastReceiver for async responses
        IntentFilter loginFilter = IntentFactory.createFilter(IntentFactory.ViewReference.LOGIN);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, loginFilter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // saving state and variables for activity recreation
        outState.putString("state", mState.toString());
        outState.putInt("erroredViewId", mErroredViewId);
        outState.putString("message", mErrorMessage);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.welcome_btn_login: clickLogin(); break;
            case R.id.welcome_forgot:   clickForgot(); break;
            case R.id.welcome_footer:  clickTwitter(); break;
            case R.id.welcome_error:   clickDismiss(); break;
        }
    }

    private void clickLogin() {
        resetFields();
        int result = Validator.test(fields);
        if (result != Validator.NO_ERROR) {
            View view = fields.get(result);
            ValidationRule rule = ValidationRule.fromView(view);
            updateState(State.ERROR_VISIBLE, view.getId(), rule.getMessage());
        } else {
            updateState(State.LOADING, 0, null);
            String email = mEtEmail.getText().toString();
            String password = mEtPassword.getText().toString();
            WelcomeLogic.using(this).tryLogin(email, password);
        }
    }

    private void clickForgot() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(UrlRepository.FORGOT_PASSWORD)));
    }

    private void clickTwitter() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(UrlRepository.DONEE_TWITTER)));
    }

    private void clickDismiss() {
        updateState(State.ERROR_HIDDEN, mErroredViewId, mErrorMessage);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        boolean mIsFocused  = mEtEmail.hasFocus() || mEtPassword.hasFocus();
        boolean mIsLogoGone = mVwLogo.getVisibility() == View.GONE;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mIsFocused != mIsLogoGone) {
                LayoutParams params;
                params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                params.gravity = (mIsFocused ? Gravity.TOP : Gravity.CENTER);
                mVwBox.setLayoutParams(params);
                mVwLogo.setVisibility(mIsFocused ? View.GONE : View.VISIBLE);
            }
        }
    }

    private void fixActivityPadding() {
        int padX = getResources().getDimensionPixelSize(R.dimen.welcome_padding_x);
        int padY = getResources().getDimensionPixelSize(R.dimen.welcome_padding_y);
        int navBarHeight = getResourceHeight("navigation_bar_height");
        int statusBarHeight = getResourceHeight("status_bar_height");

        View layout = findViewById(R.id.welcome_layout);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layout.setPadding(padX, statusBarHeight + padY, padX, navBarHeight + padY);
        } else {
            layout.setPadding(padX, statusBarHeight + padY, navBarHeight + padX, padY);
        }
    }

    private int getResourceHeight(String identifier) {
        int result = 0;
        int resourceId = getResources().getIdentifier(identifier, "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void updateState(State newState, int erroredId, String message) {
        mState = newState;
        mErroredViewId = erroredId;
        mErrorMessage = message;
        syncState();
    }

    private void syncState() {
        resetFields();
        boolean showError = false;
        switch (mState) {
            case ERROR_HIDDEN:
                mVwLogin.setVisibility(View.VISIBLE);
                mVwLoading.setVisibility(View.INVISIBLE);
                break;
            case ERROR_VISIBLE:
                showError = true;
                mVwError.setText(mErrorMessage);
                mVwLogin.setVisibility(View.VISIBLE);
                mVwLoading.setVisibility(View.INVISIBLE);
                break;
            case LOADING:
                mVwLogin.setVisibility(View.INVISIBLE);
                mVwLoading.setVisibility(View.VISIBLE);
                break;
        }
        if (showError) {
            if (mVwError.getVisibility() != View.VISIBLE) {
                Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
                mVwError.setVisibility(View.VISIBLE);
                mVwError.startAnimation(fadeIn);
            }
        } else mVwError.setVisibility(View.GONE);
        if (mErroredViewId != 0) {
            View view = findViewById(mErroredViewId);
            view.setBackgroundResource(R.drawable.edit_text_bg_error);
            view.requestFocus();
        }
    }

    private void resetFields() {
        for (View v : fields) {
            v.setBackgroundResource(R.drawable.edit_text_bg);
        }
    }

}