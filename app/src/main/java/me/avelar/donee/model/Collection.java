package me.avelar.donee.model;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.ArrayMap;
import android.util.Base64;

import com.google.gson.annotations.Expose;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Collection implements Parcelable {

    private String  localId;
    private boolean submitted;

    @Expose private Map<String, String> values;
    @Expose private Date submittedTime;
    @Expose private Form relatedForm;
    @Expose private Double latitude;
    @Expose private Double longitude;

    public Collection() {
        values = new HashMap<>();
    }

    public Collection(String localId, Long submittedTime, Form relatedForm) {
        this(localId, new Date(submittedTime), relatedForm);
    }

    public Collection(String localId, Date submittedTime, Form relatedForm) {
        this.localId = localId;
        this.submittedTime = submittedTime;
        this.relatedForm = relatedForm;
        this.values = new HashMap<>();
    }

    public String getLocalId() {
        return localId;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public void addValue(Field field, String value) {
        values.put(field.getId(), value);
    }

    public void addValue(Field field, double value) {
        addValue(field, Double.toString(value));
    }

    public void addValue(Field field, Bitmap image) {
        if (image == null) {
            values.remove(field.getId());
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        image.compress(Bitmap.CompressFormat.JPEG, 80, baos);

        String base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        addValue(field, base64);
    }

    public boolean hasValue(Field field) {
        return values.containsKey(field.getId());
    }

    public String getValue(Field field) {
        return values.get(field.getId());
    }

    protected Collection(Parcel in) {
        String key, value;

        localId = in.readString();
        relatedForm = in.readParcelable(Form.class.getClassLoader());
        submitted = in.readByte() != 0;
        submittedTime = new Date(in.readLong());
        latitude  = (Double) in.readValue(Double.class.getClassLoader());
        longitude = (Double) in.readValue(Double.class.getClassLoader());
        values = new ArrayMap<>();

        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            key   = in.readString();
            value = in.readString();
            values.put(key, value);
        }
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Date getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(Date submittedTime) {
        this.submittedTime = submittedTime;
    }

    public Form getRelatedForm() {
        return relatedForm;
    }

    public void setRelatedForm(Form relatedForm) {
        this.relatedForm = relatedForm;
    }


    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(localId);
        dest.writeParcelable(relatedForm, 0);
        dest.writeByte((byte) (submitted ? 1 : 0));
        dest.writeLong(submittedTime == null ? 0L : submittedTime.getTime());
        dest.writeValue(latitude);
        dest.writeValue(longitude);
        dest.writeInt(values.size());
        for(Map.Entry<String,String> entry : values.entrySet()){
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    public static final Creator<Collection> CREATOR = new Creator<Collection>() {
        @Override
        public Collection createFromParcel(Parcel in) {
            return new Collection(in);
        }

        @Override
        public Collection[] newArray(int size) {
            return new Collection[size];
        }
    };

    public void addLocation(Location location) {
        if (location == null) return;
        setLocation(location.getLatitude(), location.getLongitude());
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude  = latitude;
        this.longitude = longitude;
    }
}