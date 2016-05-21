package me.avelar.donee.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

import me.avelar.donee.web.UrlRepository;

@SuppressWarnings("unused")
public class Form implements Parcelable {

    @Expose private String id;
    @Expose(serialize = false) private String name;
    @Expose(serialize = false) private String category;
    @Expose(serialize = false) private String description;
    @Expose(serialize = false) private boolean useLocation;
    @Expose(serialize = false) private boolean hasIcon;
    @Expose(serialize = false) private ArrayList<Field> fields;

    public Form(String id, String name, String category) {
        this.id          = id;
        this.name        = name;
        this.category    = category;
    }

    public Form(String id, String name, String category, String description, boolean useLocation) {
        this.id          = id;
        this.name        = name;
        this.category    = category;
        this.description = description;
        this.useLocation = useLocation;
        this.hasIcon     = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return (hasIcon() ? UrlRepository.getFormIcon(id) : null);
    }

    public boolean usesLocation() {
        return useLocation;
    }

    public void setUseLocation(boolean useLocation) {
        this.useLocation = useLocation;
    }

    public boolean hasIcon() {
        return hasIcon;
    }

    public void setHasIcon(boolean hasIcon) {
        this.hasIcon = hasIcon;
    }

    public ArrayList<Field> getFields() {
        return fields;
    }

    public void setFields(ArrayList<Field> fields) {
        this.fields = fields;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(category);
        dest.writeString(description);
        dest.writeByte((byte) (useLocation ? 1 : 0));
        dest.writeByte((byte) (hasIcon ? 1 : 0));
        dest.writeTypedList(fields);
    }

    protected Form(Parcel in) {
        id = in.readString();
        name = in.readString();
        category = in.readString();
        description = in.readString();
        useLocation = in.readByte() != 0;
        hasIcon = in.readByte() != 0;
        fields = in.createTypedArrayList(Field.CREATOR);
    }

    public static final Creator<Form> CREATOR = new Creator<Form>() {
        @Override
        public Form createFromParcel(Parcel in) {
            return new Form(in);
        }

        @Override
        public Form[] newArray(int size) {
            return new Form[size];
        }
    };

}