package me.avelar.donee.view.fields;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import me.avelar.donee.R;
import me.avelar.donee.model.Field;
import me.avelar.donee.model.ValidationRule;

@SuppressLint("ViewConstructor")
public class NumberGroup extends TextGroup implements View.OnClickListener {

    public NumberGroup(Context context, Field field) {
        super(context, field);
    }

    @Override
    protected void inflateLayout(Context context) {
        View v = getInflater(context).inflate(R.layout.field_number_group, this, true);
        // getting view references
        mTvLabel = (TextView) v.findViewById(R.id.number_field_label);
        mEdValue = (EditText) v.findViewById(R.id.number_field_value);
        v.findViewById(R.id.number_field_plus).setOnClickListener(this);
        v.findViewById(R.id.number_field_minus).setOnClickListener(this);
    }

    @Override
    public void commit() {
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        double value = 0;

        try {
            value = nf.parse(mEdValue.getText().toString()).doubleValue();
        } catch (Exception ignore) { }

        getCollection().addValue(getField(), value);
    }

    @Override
    public boolean validate() {
        // checks: required, min and max values
        ValidationRule rule = getField().getRule();
        if (rule != null) {
            // non-required fields can always be empty
            if (mEdValue.getText().length() == 0 && !rule.isRequired()) return true;
            // required fields can never be empty
            if (rule.isRequired() && mEdValue.getText().toString().length() == 0) return false;
            // checking for minimum and maximum constraints
            double value = getValue();
            if (rule.getMinValue() != null && value < rule.getMinValue()) return false;
            if (rule.getMaxValue() != null && value > rule.getMaxValue()) return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        double value = getValue();
        switch (v.getId()) {
            case R.id.number_field_plus:
                value++;
                break;
            case R.id.number_field_minus:
                value--;
                break;
        }
        setValue(value);
    }

    private double getValue() {
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        double value = 0;
        try {
            value = nf.parse(mEdValue.getText().toString()).doubleValue();
        } catch (Exception ignore) { }
        return value;
    }

    private void setValue(Double value) {
        if (value == null) {
            mEdValue.setText("");
            return;
        }
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.getDefault());
        df.applyPattern("#,###,###.######");
        mEdValue.setText(df.format(value));
    }

    @Override
    public void updateValue() {
        String stringValue = getCollection().getValue(getField());
        try {
            setValue(Double.parseDouble(stringValue));
        } catch (Exception ignore) { }
    }
}