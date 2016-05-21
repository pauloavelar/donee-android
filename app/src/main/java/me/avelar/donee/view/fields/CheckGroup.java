package me.avelar.donee.view.fields;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import me.avelar.donee.R;
import me.avelar.donee.model.Field;
import me.avelar.donee.model.ValidationRule;

@SuppressLint("ViewConstructor")
public class CheckGroup extends FieldGroup {

    private LinearLayout mLlValue;

    public CheckGroup(Context context, Field field) {
        super(context, field);
    }

    @Override
    protected void inflateLayout(Context context) {
        View v = getInflater(context).inflate(R.layout.field_check_group, this, true);
        // getting view references
        mTvLabel = (TextView)     v.findViewById(R.id.check_field_label);
        mLlValue = (LinearLayout) v.findViewById(R.id.check_field_value);
    }

    @Override
    protected void setHint(String hint) {
        // not applicable to check boxes
    }

    @Override
    protected void setStarting(String[] values) {
        String[] options = getField().getOptions();
        uncheckAll();
        for (String value : values) {
            for (int j = 0; j < options.length; j++) {
                if (!options[j].equals(value)) continue;
                if (j >= mLlValue.getChildCount()) continue;

                View v = mLlValue.getChildAt(j);
                if (v instanceof CheckBox) {
                    ((CheckBox)v).setChecked(true);
                }
            }
        }
    }

    private void uncheckAll() {
        for (int i = 0, len = mLlValue.getChildCount(); i < len; i++) {
            View v = mLlValue.getChildAt(i);
            if (v instanceof CheckBox) {
                ((CheckBox)v).setChecked(false);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void setOptions(String[] values) {
        for (String value : values) {
            CheckBox cb = new CheckBox(mContext);
            cb.setTextColor(getResources().getColor(R.color.darkish_gray));
            cb.setText(value);
            mLlValue.addView(cb);
        }
    }

    @Override
    protected void setHeight(Context context, Integer heightInDp) {
        // not applicable to check boxes
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
        // not applicable to check boxes
    }

    @Override
    public boolean validate() {
        // checks: required, min and max selections
        ValidationRule rule = getField().getRule();
        if (rule != null) {
            int selectedItems = getSelectedItemsCount();
            // non-required fields can always be empty
            if (!rule.isRequired() && selectedItems == 0) return true;
            // required fields can never be empty
            if ( rule.isRequired() && selectedItems == 0) return false;
            // checking for minimum and maximum constraints
            if (rule.getMinValue() != null && selectedItems < rule.getMinValue()) return false;
            if (rule.getMaxValue() != null && selectedItems > rule.getMaxValue()) return false;
        }
        return true;
    }

    @Override
    public void showError(boolean show) {
        int res = show ? R.drawable.image_bg_error : R.drawable.image_bg_default;
        mLlValue.setBackgroundResource(res);
    }

    @Override
    public void commit() {
        ArrayList<String> list = new ArrayList<>();

        int viewCount = mLlValue.getChildCount();
        for (int i = 0; i < viewCount; i++) {
            View v = mLlValue.getChildAt(i);
            if (v instanceof CheckBox) {
                CheckBox cb = (CheckBox) v;
                if (cb.isChecked()) list.add(cb.getText().toString());
            }
        }

        String[] array = new String[list.size()];
        getCollection().addValue(getField(), Field.convertToString(list.toArray(array)));
    }

    public int getSelectedItemsCount() {
        int selectedCount = 0;
        int viewCount = mLlValue.getChildCount();

        for (int i = 0; i < viewCount; i++) {
            View v = mLlValue.getChildAt(i);
            if (v instanceof CheckBox) {
                if (((CheckBox)v).isChecked()) selectedCount++;
            }
        }

        return selectedCount;
    }

}
