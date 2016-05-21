package me.avelar.donee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.avelar.donee.model.Collection;
import me.avelar.donee.model.Form;
import me.avelar.donee.model.User;

public final class CollectionDAO {

    private static final int REPLACE = SQLiteDatabase.CONFLICT_REPLACE;
    private static final int DRAFT   = 0;
    private static final int OUTBOX  = 1;

    public static boolean insert(Context context, Collection collection, User user) {
        if (collection == null  || collection.getRelatedForm() == null ||
            user == null || collection.getValues() == null) return false;
        boolean result = true;

        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            if (collection.getLocalId() != null) {
                values.put(DoneeDbHelper.C_COLLECTION_ID, collection.getLocalId());
            }
            values.put(DoneeDbHelper.C_COLLECTION_FORM, collection.getRelatedForm().getId());
            values.put(DoneeDbHelper.C_COLLECTION_USER, user.getId());
            values.put(DoneeDbHelper.C_COLLECTION_SUBMITTED, collection.isSubmitted());
            values.put(DoneeDbHelper.C_COLLECTION_LATITUDE, collection.getLatitude());
            values.put(DoneeDbHelper.C_COLLECTION_LONGITUDE, collection.getLongitude());
            values.put(DoneeDbHelper.C_COLLECTION_TIME, collection.getSubmittedTime().getTime());

            long id = db.insertWithOnConflict(DoneeDbHelper.T_COLLECTION, null, values, REPLACE);
            if (id == DoneeDbHelper.DB_ERROR) throw new SQLException();

            Map<String, String> fields = collection.getValues();
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                insertItem(db, id, entry);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            result = false;
        } finally {
            db.endTransaction();
        }

        return result;
    }

    private static void insertItem(SQLiteDatabase db, long collectionId,
                                   Map.Entry<String, String> item) throws SQLException {
        if (db == null || !db.isOpen() || collectionId <= 0 || item == null) throw new SQLException();

        ContentValues values = new ContentValues();
        values.put(DoneeDbHelper.C_ITEM_COLLECTION, collectionId);
        values.put(DoneeDbHelper.C_ITEM_FIELD, item.getKey());
        values.put(DoneeDbHelper.C_ITEM_VALUE, item.getValue());

        long result = db.insertWithOnConflict(DoneeDbHelper.T_ITEM, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (result == DoneeDbHelper.DB_ERROR) throw new SQLException();
    }

    public static ArrayList<Collection> findDrafts(Context context, User user) {
        return findSimple(context, user, DRAFT);
    }

    public static ArrayList<Collection> findOutbox(Context context, User user) {
        return findSimple(context, user, OUTBOX);
    }

    public static ArrayList<Collection> findSimple(Context context, User user, int collectionType) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        ArrayList<Collection> collections = new ArrayList<>();

        String  where = DoneeDbHelper.C_COLLECTION_USER      + " = ? AND " +
                        DoneeDbHelper.C_COLLECTION_SUBMITTED + " = ?";
        String[] args = { user.getId(), Long.toString(collectionType) };

        Cursor cursor = db.query(DoneeDbHelper.V_COLLECTIONS, null, where, args, null, null, null);

        int id            = cursor.getColumnIndex(DoneeDbHelper.C_COLLECTION_ID);
        int formId        = cursor.getColumnIndex(DoneeDbHelper.C_COLLECTION_FORM);
        int formName      = cursor.getColumnIndex(DoneeDbHelper.C_FORM_NAME);
        int formCategory  = cursor.getColumnIndex(DoneeDbHelper.C_FORM_CATEGORY);
        int submitted     = cursor.getColumnIndex(DoneeDbHelper.C_COLLECTION_SUBMITTED);
        int latitude      = cursor.getColumnIndex(DoneeDbHelper.C_COLLECTION_LATITUDE);
        int longitude     = cursor.getColumnIndex(DoneeDbHelper.C_COLLECTION_LONGITUDE);
        int submittedTime = cursor.getColumnIndex(DoneeDbHelper.C_COLLECTION_TIME);

        Form form; Collection c;
        while (cursor.moveToNext()) {
            form = new Form(
                cursor.getString(formId),
                cursor.getString(formName),
                cursor.getString(formCategory)
            );
            c = new Collection(cursor.getString(id), new Date(cursor.getLong(submittedTime)), form);
            if (!cursor.isNull(latitude)) {
                c.setLocation(cursor.getDouble(latitude), cursor.getDouble(longitude));
            }
            c.setSubmitted(cursor.getInt(submitted) != 0);
            collections.add(c);
        }
        cursor.close();
        return collections;
    }

    // When this is called, the input collection is missing the values map
    public static Collection findComplete(Context context, Collection collection) {
        if (collection == null || collection.getRelatedForm() == null || context == null) return collection;

        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();

        // completing the input collection
        Form form = FormDAO.find(db, collection.getRelatedForm().getId());
        collection.setRelatedForm(form);
        Map<String, String> items = findItems(db, collection);
        collection.setValues(items);

        return collection;
    }

    public static Map<String, String> findItems(SQLiteDatabase db, Collection collection) {
        if (db == null || !db.isOpen() || collection == null) return null;
        Map<String, String> items = new HashMap<>();

        String  where = DoneeDbHelper.C_ITEM_COLLECTION + " = ?";
        String[] args = { collection.getLocalId() };

        Cursor cursor = db.query(DoneeDbHelper.T_ITEM, null, where, args, null, null, null);

        final int FIELD = cursor.getColumnIndex(DoneeDbHelper.C_ITEM_FIELD);
        final int VALUE = cursor.getColumnIndex(DoneeDbHelper.C_ITEM_VALUE);

        while (cursor.moveToNext()) {
            items.put(cursor.getString(FIELD), cursor.getString(VALUE));
        }
        cursor.close();

        return items;
    }

    public static void delete(Context context, Collection collection) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();
        String[] args = { collection.getLocalId() };

        db.delete(DoneeDbHelper.T_COLLECTION, DoneeDbHelper.C_COLLECTION_ID + " = ?", args);
    }

    public static void delete(Context context, int submitted) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();
        String[] args = { Integer.toString(submitted) };

        db.delete(DoneeDbHelper.T_COLLECTION, DoneeDbHelper.C_COLLECTION_SUBMITTED + " = ?", args);
    }

}
