package me.avelar.donee.view.fields;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.avelar.donee.model.Collection;
import me.avelar.donee.model.Field;

public abstract class FieldGroup extends LinearLayout {

    public static final int DEFAULT_HEIGHT = 40;

    protected Context  mContext;
    private Field      mField;
    private Collection mCollection;

    protected TextView mTvLabel;

    public FieldGroup(Context context, Field field) {
        super(context);
        mContext = context;

        setField(field);
        inflateLayout(context);
        setHint(field.getHint());
        setLabel(field.getLabel());
        setMultiline(field.isMultiline());
        setHeight(context, field.getHeight());

        String[] options = field.getOptions();
        if (options != null && options.length > 0) {
            setOptions(options);
        }

        String[] starting = field.getStarting();
        if (starting != null && starting.length > 0) {
            setStarting(starting);
        }
    }

    public void setField(Field field) {
        mField = field;
    }

    public Field getField() {
        return mField;
    }

    public Collection getCollection() {
        return mCollection;
    }

    public void setCollection(Collection collection) {
        this.mCollection = collection;
        if (mCollection != null && mCollection.hasValue(mField)) {
            updateValue();
        }
    }

    public void setLabel(String label) {
        if (label == null || label.length() == 0) {
            mTvLabel.setVisibility(View.GONE);
        } else {
            mTvLabel.setText(label);
            mTvLabel.setVisibility(View.VISIBLE);
        }
    }

    protected static int convertDpToPixels(Context context, int valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float fpixels = metrics.density * valueInDp;
        return (int) (fpixels + 0.5f);
    }

    protected LayoutInflater getInflater(Context context) {
        return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public String getErrorMessage() {
        return mField.getRule() == null ? null : mField.getRule().getMessage();
    }

    protected abstract void inflateLayout(Context context);
    protected abstract void setHint(String hint);
    protected abstract void setStarting(String[] value);
    protected abstract void setOptions(String[] value);
    protected abstract void setMultiline(boolean multiline);
    protected abstract void setHeight(Context context, Integer heightInDp);
    protected abstract void updateValue();

    public abstract void commit();
    public abstract boolean validate();
    public abstract void showError(boolean show);

}
