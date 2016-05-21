package me.avelar.donee.controller;

import android.view.View;
import android.widget.EditText;

import java.util.List;

import me.avelar.donee.model.ValidationRule;
import me.avelar.donee.view.fields.FieldGroup;

public class Validator {

    public static final int NO_ERROR = -1;

    public static int test(List<? extends View> views) {
        for (int i = 0, len = views.size(); i < len; i++) {
            View v = views.get(i);
            if (v instanceof FieldGroup) {
                FieldGroup fg = (FieldGroup) v;
                if (!fg.validate()) return i;
            } else if (v.getTag() instanceof ValidationRule) {
                ValidationRule rule = (ValidationRule) v.getTag();
                if (v instanceof EditText) {
                    String textToMatch = ((EditText) v).getText().toString();
                    // checks: required and regular expression
                    if (rule.isRequired() && textToMatch.length() == 0 ||
                       !textToMatch.matches(rule.getRegexp())) return i;
                } else return i;
            }
        }
        return Validator.NO_ERROR;
    }

}