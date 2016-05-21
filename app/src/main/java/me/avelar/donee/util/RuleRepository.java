package me.avelar.donee.util;

import android.content.Context;

import me.avelar.donee.R;
import me.avelar.donee.model.ValidationRule;

public class RuleRepository {

    public static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
            "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    public static final String PASSWORD_PATTERN = "^.{8,}$";

    private static RuleRepository instance;
    private Context context;

    public static RuleRepository getInstance(Context c) {
        if (instance == null) {
            instance = new RuleRepository();
        }
        instance.setContext(c);
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ValidationRule getRuleById(int viewId) {
        switch(viewId) {
            case R.id.welcome_email:
                return new ValidationRule(true, RuleRepository.EMAIL_PATTERN,
                           context.getResources().getString(R.string.welcome_email_error));
            case R.id.welcome_password:
                return new ValidationRule(true, RuleRepository.PASSWORD_PATTERN,
                           context.getResources().getString(R.string.welcome_password_error));
            default:
                return null;
        }
    }

}