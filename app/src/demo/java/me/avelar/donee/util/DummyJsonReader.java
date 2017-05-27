package me.avelar.donee.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import me.avelar.donee.R;
import me.avelar.donee.model.Form;
import me.avelar.donee.model.Session;

/**
 *
 */
public final class DummyJsonReader {

    private static Gson getGsonInstance() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }

    public static Session loadSession(Context context) {
        try {
            InputStream in = context.getResources().openRawResource(R.raw.session);
            return getGsonInstance().fromJson(new InputStreamReader(in, "UTF-8"), Session.class);
        } catch (Exception ignore) { }
        return null;
    }

    public static ArrayList<Form> loadForms(Context context) {
        try {
            InputStream in = context.getResources().openRawResource(R.raw.forms);
            InputStreamReader reader = new InputStreamReader(in, "UTF-8");
            return getGsonInstance().fromJson(reader, new TypeToken<ArrayList<Form>>(){}.getType());
        } catch (Exception ignore) { }
        return null;
    }

}