package me.avelar.donee.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class ValidationRule implements Parcelable {

    @Expose private boolean required;
    @Expose private Double  minValue;
    @Expose private Double  maxValue;
    @Expose private String  message;
    @Expose private String  regexp;

    public ValidationRule(boolean required, String regexp, String errorMessage) {
        this(required, regexp, null, null, errorMessage);
    }

    public ValidationRule(boolean required, String regexp, Double minValue,
                          Double maxValue, String errorMessage) {
        this.required = required;
        this.regexp = regexp;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.message  = errorMessage;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public String getRegexp()  {
        return regexp;
    }

    public String getMessage() {
        return message;
    }

    public static ValidationRule fromView(View view) {
        ValidationRule rule = null;
        if (view.getTag() instanceof ValidationRule) {
            rule = (ValidationRule) view.getTag();
        }
        return rule;
    }

    protected ValidationRule(Parcel in) {
        regexp = in.readString();
        required = in.readByte() != 0;
        minValue = (Double) in.readValue(Double.class.getClassLoader());
        maxValue = (Double) in.readValue(Double.class.getClassLoader());
        message  = in.readString();
    }

    public static final Creator<ValidationRule> CREATOR = new Creator<ValidationRule>() {
        @Override
        public ValidationRule createFromParcel(Parcel in) {
            return new ValidationRule(in);
        }

        @Override
        public ValidationRule[] newArray(int size) {
            return new ValidationRule[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(regexp);
        dest.writeByte((byte) (required ? 1 : 0));
        dest.writeValue(minValue);
        dest.writeValue(maxValue);
        dest.writeString(message);
    }

}