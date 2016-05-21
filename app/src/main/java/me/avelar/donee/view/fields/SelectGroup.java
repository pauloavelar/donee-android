package me.avelar.donee.view.fields;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import me.avelar.donee.R;
import me.avelar.donee.model.Field;
import me.avelar.donee.model.ValidationRule;

@SuppressLint("ViewConstructor")
public class SelectGroup extends FieldGroup {

    private Spinner  mSpValue;
    private String   mHint;
    private String[] mOptions;

    private ArrayAdapter<String> mAdapter;

    public SelectGroup(Context context, Field field) {
        super(context, field);
    }

    @Override
    protected void inflateLayout(Context context) {
        View v = getInflater(context).inflate(R.layout.field_select_group, this, true);

        // getting view references
        mTvLabel = (TextView) v.findViewById(R.id.select_field_label);
        mSpValue = (Spinner)  v.findViewById(R.id.select_field_value);

        // Spinner setup
        mAdapter = new ArrayAdapter<>(context, R.layout.dropdown_item);
        mSpValue.setAdapter(mAdapter);
    }

    @Override
    protected void setHint(String hint) {
        mHint = hint;
        updateItems();
    }

    @Override
    protected void setHeight(Context context, Integer heightInDp) {
        // not applicable to Spinners
    }

    @Override
    protected void updateValue() {
        String value = getCollection().getValue(getField());
        if (value != null) {
            String[] valueArray = { value };
            setStarting(valueArray);
        }
    }

    @Override
    protected void setMultiline(boolean multiline) {
        // not applicable to Spinners
    }

    @Override
    protected void setStarting(String[] value) {
        if (value == null || value.length == 0) return;
        List<String> temp = Arrays.asList(mOptions);
        mSpValue.setSelection(temp.indexOf(value[0]) + 1);
    }

    @Override
    protected void setOptions(String[] values) {
        mOptions = values;
        updateItems();
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean validate() {
        ValidationRule rule = getField().getRule();
        // checks: required only
        if (rule != null && rule.isRequired()) {
            return mSpValue.getSelectedItemPosition() > 0;
        }
        return true;
    }

    @Override
    public void showError(boolean show) {
        int res = show ? R.drawable.spinner_bg_error : R.drawable.spinner_bg;
        mSpValue.setBackgroundResource(res);
    }

    @Override
    public void commit() {
        getCollection().addValue(getField(), (String) mSpValue.getSelectedItem());
    }

    private void updateItems() {
        if (mOptions == null) return;
        mAdapter.clear();
        mAdapter.add(mHint != null ? mHint : getResources().getString(R.string.default_spinner_hint));
        mAdapter.addAll(mOptions);
        mAdapter.notifyDataSetChanged();
        mSpValue.setSelection(0);
    }

}
