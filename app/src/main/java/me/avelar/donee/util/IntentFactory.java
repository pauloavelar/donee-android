package me.avelar.donee.util;

import android.content.Intent;
import android.content.IntentFilter;

public class IntentFactory {

    public enum ViewReference { LOGIN, MAIN, FORMS, COLLECTIONS, COLLECT }

    public enum Type {
        MAIN_GO_TO_LOGIN       (ViewReference.MAIN),
        MAIN_REFRESH_USER      (ViewReference.MAIN),
        MAIN_LOGIN_PERFORMED   (ViewReference.LOGIN),
        FORMS_LOAD_FINISHED    (ViewReference.FORMS),
        FORMS_LOAD_ERROR       (ViewReference.FORMS),
        COLLECTION_LOADED      (ViewReference.COLLECTIONS),
        COLLECTION_SENT        (ViewReference.COLLECTIONS),
        COLLECTION_STORED      (ViewReference.COLLECT),
        COLLECTION_STORED_ERROR(ViewReference.COLLECT);

        public ViewReference activity;

        Type(ViewReference activity) { this.activity = activity; }
    }
    
    public static final String EXTRA_ACTION = "ACTION";
    public static final String EXTRA_DETAIL = "DETAIL";
    public static final String EXTRA_DATA   = "DATA";

    public static Intent create(Type intentType) {
        Intent intent = new Intent(intentType.activity.toString());
        intent.putExtra(EXTRA_ACTION, intentType.toString());
        return intent;
    }

    public static IntentFilter createFilter(ViewReference reference) {
        return new IntentFilter(reference.toString());
    }

}
