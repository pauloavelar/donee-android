package me.avelar.donee.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

@SuppressWarnings("WeakerAccess")
public class DoneeDbHelper extends SQLiteOpenHelper {

    // useful constants
    public static final long DB_ERROR = -1;

    // database information
    public static final int DATABASE_VERSION = 16;
    public static final String DATABASE_NAME = "donee.db";

    // table names
    public static final String T_SESSION    = "session";
    public static final String T_USER       = "user";
    public static final String T_FORM       = "form";
    public static final String T_USER_FORM  = "user_form";
    public static final String T_FIELD      = "field";
    public static final String T_COLLECTION = "collection";
    public static final String T_ITEM       = "item";

    // column names
    public static final String C_SESSION_ID   = "_id";
    public static final String C_SESSION_USER = "user_id";
    public static final String C_SESSION_TIME = "last_used";

    public static final String C_USER_ID      = "_id";
    public static final String C_USER_NAME    = "name";
    public static final String C_USER_EMAIL   = "email";
    public static final String C_USER_ACCOUNT = "account";
    public static final String C_USER_SYNCED  = "last_updated";

    public static final String C_FORM_ID           = "_id";
    public static final String C_FORM_NAME         = "name";
    public static final String C_FORM_CATEGORY     = "category";
    public static final String C_FORM_DESCRIPTION  = "description";
    public static final String C_FORM_HAS_ICON     = "has_icon";
    public static final String C_FORM_USE_LOCATION = "use_location";
    public static final String C_FORM_ACTIVE       = "active";

    public static final String C_UF_USER = "user";
    public static final String C_UF_FORM = "form";

    public static final String C_FIELD_ID        = "_id";
    public static final String C_FIELD_FORM      = "form";
    public static final String C_FIELD_TYPE      = "type";
    public static final String C_FIELD_LABEL     = "label";
    public static final String C_FIELD_HINT      = "hint";
    public static final String C_FIELD_HEIGHT    = "height";
    public static final String C_FIELD_MULTILINE = "multiline";
    public static final String C_FIELD_OPTIONS   = "options";
    public static final String C_FIELD_STARTING  = "starting";
    public static final String C_FIELD_REGEXP    = "regexp";
    public static final String C_FIELD_REQUIRED  = "required";
    public static final String C_FIELD_MIN_VALUE = "min_value";
    public static final String C_FIELD_MAX_VALUE = "max_value";
    public static final String C_FIELD_MESSAGE   = "message";
    public static final String C_FIELD_ORDER     = "ordering";
    public static final String C_VFIELD_HAS_RULE = "has_rule";

    public static final String C_COLLECTION_ID        = "_id";
    public static final String C_COLLECTION_FORM      = "form";
    public static final String C_COLLECTION_USER      = "user";
    public static final String C_COLLECTION_TIME      = "date_time";
    public static final String C_COLLECTION_SUBMITTED = "submitted";
    public static final String C_COLLECTION_LATITUDE  = "latitude";
    public static final String C_COLLECTION_LONGITUDE = "longitude";

    public static final String C_ITEM_ID         = "_id";
    public static final String C_ITEM_COLLECTION = "collection";
    public static final String C_ITEM_FIELD      = "field";
    public static final String C_ITEM_VALUE      = "value";

    // view names
    public static final String V_USER_FORM   = "vw_user_form";
    public static final String V_FIELDS      = "vw_fields";
    public static final String V_COLLECTIONS = "vw_collections";

    // table creation
    private static final String CREATE_USER =
        "create table " + T_USER + " (" +
            C_USER_ID      + " integer primary key, " +
            C_USER_EMAIL   + " text unique not null, " +
            C_USER_NAME    + " text not null, " +
            C_USER_ACCOUNT + " text not null, " +
            C_USER_SYNCED  + " integer)";
    private static final String CREATE_SESSION =
        "create table " + T_SESSION + " (" +
            C_SESSION_ID   + " text primary key not null, " +
            C_SESSION_USER + " integer unique not null, " +
            C_SESSION_TIME + " integer default current_timestamp, " +
            // When are users deleted? ONLY in Settings > Remove user
            "foreign key (" + C_SESSION_USER + ") " +
                "references " + T_USER + "(" + C_USER_ID + ") on delete cascade, "+
            "unique(" + C_SESSION_USER + ") on conflict replace)";
    private static final String CREATE_FORM =
        "create table " + T_FORM + " (" +
            C_FORM_ID           + " integer primary key, " +
            C_FORM_NAME         + " text not null, " +
            C_FORM_CATEGORY     + " text, " +
            C_FORM_DESCRIPTION  + " text, " +
            C_FORM_HAS_ICON     + " integer default 0, " +
            C_FORM_USE_LOCATION + " integer default 0, " +
            C_FORM_ACTIVE       + " integer default 1)";
    private static final String CREATE_USER_FORM =
        "create table " + T_USER_FORM + " (" +
            C_UF_USER + " integer not null, " +
            C_UF_FORM + " integer not null, " +
            // When are users deleted? ONLY in Settings > Remove user
            "foreign key (" + C_UF_USER + ") " +
                "references " + T_USER + "(" + C_USER_ID + ") on delete cascade, " +
            // When are forms deleted? Never. No way to safely remove them in multi-user mode
            "foreign key (" + C_UF_FORM + ") " +
                "references " + T_FORM + "(" + C_FORM_ID + ") on delete cascade, " +
            "unique(" + C_UF_USER + "," + C_UF_FORM + ") on conflict ignore)";
    private static final String CREATE_FIELD =
        "create table " + T_FIELD + " (" +
            C_FIELD_ID        + " integer primary key, " +
            C_FIELD_FORM      + " integer not null, " +
            C_FIELD_TYPE      + " text not null, " +
            C_FIELD_LABEL     + " text, " +
            C_FIELD_HINT      + " text, " +
            C_FIELD_HEIGHT    + " integer default 50, " +
            C_FIELD_MULTILINE + " integer default 0, " +
            C_FIELD_OPTIONS   + " text, " +
            C_FIELD_STARTING  + " text, " +
            C_FIELD_REGEXP    + " text, " +
            C_FIELD_REQUIRED  + " integer default 0, " +
            C_FIELD_MIN_VALUE + " real, " +
            C_FIELD_MAX_VALUE + " real, " +
            C_FIELD_ORDER     + " integer default 0, " +
            C_FIELD_MESSAGE   + " text," +
            // When are forms deleted? Never. No way to safely remove them in multi-user mode
            "foreign key (" + C_FIELD_FORM + ") " +
                "references " + T_FORM + "(" + C_FORM_ID + ") on delete cascade)";
    private static final String CREATE_COLLECTION =
        "create table " + T_COLLECTION + " (" +
            C_COLLECTION_ID   + " integer primary key, " +
            C_COLLECTION_FORM + " integer not null, " +
            C_COLLECTION_USER + " integer not null, " +
            C_COLLECTION_TIME + " integer, " +
            C_COLLECTION_LATITUDE  + " real, " +
            C_COLLECTION_LONGITUDE + " real, " +
            C_COLLECTION_SUBMITTED + " integer default 0, " +
            // When are users deleted? ONLY in Settings > Remove user
            "foreign key (" + C_COLLECTION_USER + ") " +
                "references " + T_USER + "(" + C_USER_ID + ") on delete cascade, " +
            // When are forms deleted? Never. No way to safely remove them in multi-user mode
            "foreign key (" + C_COLLECTION_FORM + ") " +
                "references " + T_FORM + "(" + C_FORM_ID + ") on delete cascade)";
    private static final String CREATE_ITEM =
        "create table " + T_ITEM + " (" +
            C_ITEM_ID         + " integer primary key, " +
            C_ITEM_COLLECTION + " integer not null, " +
            C_ITEM_FIELD      + " integer not null, " +
            C_ITEM_VALUE      + " text, " +
            // When are collections deleted? In Drafts and Outbox > Delete button
            "foreign key (" + C_ITEM_COLLECTION + ") " +
                "references " + T_COLLECTION + "(" + C_COLLECTION_ID + ") on delete cascade, " +
            // When are fields deleted? ONLY when a form structure changed and fields were removed
            "foreign key (" + C_ITEM_FIELD + ") " +
                "references " + T_FIELD + "(" + C_FIELD_ID + ") on delete no action)";

    private static final String CREATE_VIEW_FORMS =
        "create view " + V_USER_FORM + " as " +
            "select " + T_USER_FORM + "." + C_UF_USER + ", " + T_FORM + ".* " +
              "from " + T_USER_FORM + " left join " + T_FORM + " " +
                "on " + T_USER_FORM + "." + C_UF_FORM + " = " + T_FORM + "." + C_FORM_ID + " " +
             "where " + C_FORM_ID + " is not null";
    private static final String CREATE_VIEW_FIELDS =
        "create view " + V_FIELDS + " as " +
            "select *, case when (" +
                C_FIELD_REQUIRED  + " = 1 or " +
                C_FIELD_REGEXP    + " is not null or " +
                C_FIELD_MIN_VALUE + " is not null or " +
                C_FIELD_MAX_VALUE + " is not null" +
                ") then 1 else 0 end as " + C_VFIELD_HAS_RULE + " " +
            "from " + T_FIELD;
    private static final String CREATE_VIEW_COLLECTIONS =
        "create view " + V_COLLECTIONS + " as " +
            "select " + T_COLLECTION + ".*, " +
                        T_FORM + "." + C_FORM_CATEGORY + ", " +
                        T_FORM + "." + C_FORM_NAME     + " "  +
              "from " + T_COLLECTION + " left join " + T_FORM + " " +
                "on " + T_FORM + "." + C_FORM_ID + " = " + T_COLLECTION + "." + C_COLLECTION_FORM;

    private static DoneeDbHelper mInstance;

    public synchronized static DoneeDbHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DoneeDbHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private DoneeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_SESSION);
        db.execSQL(CREATE_FORM);
        db.execSQL(CREATE_USER_FORM);
        db.execSQL(CREATE_FIELD);
        db.execSQL(CREATE_COLLECTION);
        db.execSQL(CREATE_ITEM);

        db.execSQL(CREATE_VIEW_FORMS);
        db.execSQL(CREATE_VIEW_FIELDS);
        db.execSQL(CREATE_VIEW_COLLECTIONS);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        // FUTURE IMPLEMENTATION
        switch (oldVersion) {
            case 1: make changes from 1 to 2
            case 2: make changes from 2 to 3
            case 3: make changes from 3 to newVersion
        }
        // */
        db.execSQL("DROP VIEW  IF EXISTS vw_user_form");
        db.execSQL("DROP VIEW  IF EXISTS vw_fields");
        db.execSQL("DROP VIEW  IF EXISTS vw_collections");
        db.execSQL("DROP TABLE IF EXISTS item");
        db.execSQL("DROP TABLE IF EXISTS collection");
        db.execSQL("DROP TABLE IF EXISTS user_form");
        db.execSQL("DROP TABLE IF EXISTS field");
        db.execSQL("DROP TABLE IF EXISTS form");
        db.execSQL("DROP TABLE IF EXISTS session");
        db.execSQL("DROP TABLE IF EXISTS user");
        onCreate(db);
    }

}