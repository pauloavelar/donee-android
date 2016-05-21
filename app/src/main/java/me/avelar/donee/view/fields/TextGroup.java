package me.avelar.donee.view.fields;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import me.avelar.donee.R;
import me.avelar.donee.model.Field;
import me.avelar.donee.model.ValidationRule;

@SuppressLint("ViewConstructor")
public class TextGroup extends FieldGroup {

    protected EditText mEdValue;

    public TextGroup(Context context, Field field) {
        super(context, field);
    }

    @Override
    protected void inflateLayout(Context context) {
        View v = getInflater(context).inflate(R.layout.field_text_group, this, true);
        // getting view references
        mTvLabel = (TextView) v.findViewById(R.id.text_field_label);
        mEdValue = (EditText) v.findViewById(R.id.text_field_value);
    }

    @Override
    public void setHint(String hint) {
        mEdValue.setHint(hint);
    }

    @Override
    public void setStarting(String[] values) {
        if (values.length == 0) return;
        mEdValue.setText(values[0]);
    }

    @Override
    public void setOptions(String[] value) {
        // not applicable to text fields
    }

    @Override
    public void setHeight(Context context, Integer heightInDp) {
        if (heightInDp == null || heightInDp < DEFAULT_HEIGHT) {
            heightInDp = DEFAULT_HEIGHT;
        }
        int pixels = convertDpToPixels(context, heightInDp);
        mEdValue.setHeight(pixels);
    }

    @Override
    protected void updateValue() {
        String value = getCollection().getValue(getField());
        mEdValue.setText(value != null ? value : "");
    }

    @Override
    public void setMultiline(boolean multiline) {
        mEdValue.setGravity(multiline ? Gravity.TOP : Gravity.CENTER_VERTICAL);
        mEdValue.setSingleLine(!multiline);
    }

    @Override
    public boolean validate() {
        // checks: required, regexp and minimum and maximum lengths
        ValidationRule rule = getField().getRule();
        if (rule != null) {
            String textToMatch = mEdValue.getText().toString();
            // non-required fields can always be empty
            if (!rule.isRequired() && textToMatch.length() == 0) return true;
            // required fields can never be empty
            if ( rule.isRequired() && textToMatch.length() == 0) return false;
            // checking for regular expression (ignores null and empty strings)
            if (rule.getRegexp() != null && !rule.getRegexp().equals("") &&
                !textToMatch.matches(rule.getRegexp())) return false;
            // checking for minimum and maximum length constraints
            if (rule.getMinValue() != null && textToMatch.length() < rule.getMinValue()) return false;
            if (rule.getMaxValue() != null && textToMatch.length() > rule.getMaxValue()) return false;
        }
        return true;
    }

    @Override
    public void showError(boolean show) {
        int res = show ? R.drawable.edit_text_bg_error : R.drawable.edit_text_bg;
        mEdValue.setBackgroundResource(res);
    }

    @Override
    public void commit() {
        getCollection().addValue(getField(), mEdValue.getText().toString());
    }

}
