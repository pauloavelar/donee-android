package me.avelar.donee.util;

import android.content.Context;

import me.avelar.donee.model.Field;
import me.avelar.donee.view.fields.CheckGroup;
import me.avelar.donee.view.fields.FieldGroup;
import me.avelar.donee.view.fields.ImageGroup;
import me.avelar.donee.view.fields.NumberGroup;
import me.avelar.donee.view.fields.RadioGroup;
import me.avelar.donee.view.fields.SelectGroup;
import me.avelar.donee.view.fields.TextGroup;

public final class FieldFactory {

    public static FieldGroup create(Context context, Field field) {
        if (context == null || field == null) return null;

        switch(field.getType()) {
            case TEXT:   return new   TextGroup(context, field);
            case NUMBER: return new NumberGroup(context, field);
            case SELECT: return new SelectGroup(context, field);
            case RADIO:  return new  RadioGroup(context, field);
            case CHECK:  return new  CheckGroup(context, field);
            case IMAGE:  return new  ImageGroup(context, field);
        }

        return null;
    }

}