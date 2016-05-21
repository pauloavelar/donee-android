package me.avelar.donee.view.fields;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import me.avelar.donee.R;
import me.avelar.donee.model.Field;
import me.avelar.donee.model.ValidationRule;

@SuppressLint("ViewConstructor")
public class RadioGroup extends FieldGroup {

    private android.widget.RadioGroup mRgValue;

    public RadioGroup(Context context, Field field) {
        super(context, field);
    }

    @Override
    protected void inflateLayout(Context context) {
        View v = getInflater(context).inflate(R.layout.field_radio_group, this, true);
        // getting view references
        mTvLabel = (TextView) v.findViewById(R.id.radio_field_label);
        mRgValue = (android.widget.RadioGroup) v.findViewById(R.id.radio_field_value);
    }

    @Override
    protected void setHint(String hint) {
        // not applicable to RadioGroups
    }

    @Override
    protected void setStarting(String[] values) {
        if (values == null || values.length == 0) {
            mRgValue.clearCheck();
            return;
        }
        String[] options = getField().getOptions();
        int childCount   = mRgValue.getChildCount();
        for (int i = 0; i < options.length; i++) {
            if (i >= childCount) break;
            if (!values[0].equals(options[i])) continue;

            View v = mRgValue.getChildAt(i);
            if (v instanceof RadioButton) {
                ((RadioButton) v).setChecked(true);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void setOptions(String[] values) {
        for (String value : values) {
            RadioButton rb = new RadioButton(mContext);
            rb.setTextColor(getResources().getColor(R.color.darkish_gray));
            rb.setText(value);
            mRgValue.addView(rb);
        }
    }

    @Override
    protected void setHeight(Context context, Integer heightInDp) {
        // not applicable to RadioGroups
    }

    @Override
    protected void updateValue() {
        String stringValue = getCollection().getValue(getField());
        if (stringValue != null) {
            String[] values = stringValue.split(Field.SEPARATOR);
            setStarting(values);
        }
    }

    @Override
    protected void setMultiline(boolean multiline) {
        // not applicable to RadioGroups
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean validate() {
        ValidationRule rule = getField().getRule();
        if (rule != null && rule.isRequired()) {
            return mRgValue.getCheckedRadioButtonId() != -1;
        }
        return true;
    }

    @Override
    public void showError(boolean show) {
        int res = show ? R.drawable.image_bg_error : R.drawable.image_bg_default;
        mRgValue.setBackgroundResource(res);
    }

    @Override
    public void commit() {
        int viewCount = mRgValue.getChildCount();

        for (int i = 0; i < viewCount; i++) {
            View v = mRgValue.getChildAt(i);
            if (v instanceof RadioButton) {
                RadioButton rb = (RadioButton) v;
                if (rb.isChecked()) {
                    getCollection().addValue(getField(), rb.getText().toString());
                }
            }
        }
    }

}