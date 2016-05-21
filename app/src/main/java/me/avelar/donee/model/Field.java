package me.avelar.donee.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

@SuppressWarnings("unused")
public class Field implements Parcelable {

    public enum Type { TEXT, SELECT, RADIO, CHECK, NUMBER, IMAGE }

    public static final String SEPARATOR = ";;;";

    @Expose private String   id;
    @Expose private Type     type;
    @Expose private String   label;
    @Expose private String   hint;
    @Expose private Integer  height;
    @Expose private boolean  multiline;
    @Expose private String[] options;
    @Expose private String[] starting;
    @Expose private ValidationRule rule;

    public Field(String id, String type, String label, String hint, Integer height,
                 boolean multiline, String options, String starting) {
        this.id = id;
        if (type != null) {
            try {
                this.type = Type.valueOf(type);
            } catch (Exception ignore) { }
        }
        this.label  = label;
        this.hint   = hint;
        this.height = height;
        this.multiline = multiline;
        if (options != null) {
            this.options = options.split(Field.SEPARATOR);
        }
        if (starting != null) {
            this.starting = starting.split(Field.SEPARATOR);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public String[] getOptions() {
        return options;
    }

    public String getOptionsAsString() {
        return convertToString(options);
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String[] getChecked() {
        return starting;
    }

    public void setStarting(String[] checked) {
        this.starting = checked;
    }

    public String[] getStarting() {
        return starting;
    }

    public String getStartingAsString() {
        return convertToString(starting);
    }

    public ValidationRule getRule() {
        return rule;
    }

    public void setRule(ValidationRule rule) {
        this.rule = rule;
    }

    protected Field(Parcel in) {
        id        = in.readString();
        type      = Type.valueOf(in.readString());
        label     = in.readString();
        hint      = in.readString();
        height    = (Integer) in.readValue(Integer.class.getClassLoader());
        multiline = in.readByte() != 0;
        options   = in.createStringArray();
        starting  = in.createStringArray();
        rule      = in.readParcelable(ValidationRule.class.getClassLoader());
    }

    public static String convertToString(String[] array) {
        if (array == null || array.length == 0) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) sb.append(Field.SEPARATOR);
        }
        return sb.toString();
    }

    public static final Creator<Field> CREATOR = new Creator<Field>() {
        @Override
        public Field createFromParcel(Parcel in) {
            return new Field(in);
        }

        @Override
        public Field[] newArray(int size) {
            return new Field[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(type.toString());
        dest.writeString(label);
        dest.writeString(hint);
        dest.writeValue(height);
        dest.writeByte((byte) (multiline ? 1 : 0));
        dest.writeStringArray(options);
        dest.writeStringArray(starting);
        dest.writeParcelable(rule, 0);
    }

}