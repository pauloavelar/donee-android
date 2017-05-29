package me.avelar.donee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;

import me.avelar.donee.model.Form;
import me.avelar.donee.model.User;

@SuppressWarnings("WeakerAccess")
public class FormDao {

    public static void insert(Context context, ArrayList<Form> forms, User user) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();
        db.beginTransaction();
        try {
            for (Form form : forms) {
                insert(db, form, user);
            }
            UserDao.notifySync(context, user);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("donee.db", e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    private static void insert(SQLiteDatabase db, Form form, User user) throws SQLException {
        if (db == null || !db.isOpen() || form == null || user == null) throw new SQLException();
        final int IGNORE  = SQLiteDatabase.CONFLICT_IGNORE;

        ContentValues formValues = new ContentValues();
        formValues.put(DoneeDbHelper.C_FORM_ID,           form.getId());
        formValues.put(DoneeDbHelper.C_FORM_NAME,         form.getName());
        formValues.put(DoneeDbHelper.C_FORM_CATEGORY,     form.getCategory());
        formValues.put(DoneeDbHelper.C_FORM_DESCRIPTION,  form.getDescription());
        formValues.put(DoneeDbHelper.C_FORM_USE_LOCATION, form.usesLocation());
        formValues.put(DoneeDbHelper.C_FORM_HAS_ICON,     form.hasIcon());

        // inserting or updating
        if (!isFormStored(db, form)) {
            db.insertOrThrow(DoneeDbHelper.T_FORM, null, formValues);
        } else {
            String  where = DoneeDbHelper.C_FORM_ID + " = ?";
            String[] args = { form.getId() };
            db.update(DoneeDbHelper.T_FORM, formValues, where, args);
        }

        // adding relationship between user and form
        ContentValues permission = new ContentValues();
        permission.put(DoneeDbHelper.C_UF_FORM, form.getId());
        permission.put(DoneeDbHelper.C_UF_USER, user.getId());
        db.insertWithOnConflict(DoneeDbHelper.T_USER_FORM, null, permission, IGNORE);

        // adding form fields
        FieldDao.insert(db, form.getFields(), form);
    }

    private static boolean isFormStored(@NonNull SQLiteDatabase db, @NonNull Form form) {
        String  where = DoneeDbHelper.C_FORM_ID + " = ?";
        String[] args = { form.getId() };

        Cursor cursor = db.query(DoneeDbHelper.T_FORM, null, where, args, null, null, null);
        boolean isStored = cursor.getCount() > 0;
        cursor.close();

        return isStored;
    }

    public static ArrayList<Form> find(Context context, User user) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();

        String  where = DoneeDbHelper.C_UF_USER + " = ?";
        String[] args = { user.getId() };
        Cursor cursor = db.query(DoneeDbHelper.V_USER_FORM, null, where, args, null, null, null);

        final int ID           = cursor.getColumnIndex(DoneeDbHelper.C_FORM_ID);
        final int NAME         = cursor.getColumnIndex(DoneeDbHelper.C_FORM_NAME);
        final int CATEGORY     = cursor.getColumnIndex(DoneeDbHelper.C_FORM_CATEGORY);
        final int DESCRIPTION  = cursor.getColumnIndex(DoneeDbHelper.C_FORM_DESCRIPTION);
        final int USE_LOCATION = cursor.getColumnIndex(DoneeDbHelper.C_FORM_USE_LOCATION);
        final int HAS_ICON     = cursor.getColumnIndex(DoneeDbHelper.C_FORM_HAS_ICON);

        ArrayList<Form> forms = new ArrayList<>();
        while (cursor.moveToNext()) {
            Form form = new Form(
                cursor.getString(ID),
                cursor.getString(NAME),
                cursor.getString(CATEGORY),
                cursor.getString(DESCRIPTION),
                cursor.getInt(USE_LOCATION) != 0,
                cursor.getInt(HAS_ICON) != 0
            );
            form.setFields(FieldDao.find(db, form));
            forms.add(form);
        }
        cursor.close();

        return forms;
    }

    public static Form find(SQLiteDatabase db, String formId) {
        final String  where = DoneeDbHelper.C_FORM_ID + " = ?";
        final String[] args = { formId };
        Cursor cursor = db.query(DoneeDbHelper.T_FORM, null, where, args, null, null, null);

        int id          = cursor.getColumnIndex(DoneeDbHelper.C_FORM_ID);
        int name        = cursor.getColumnIndex(DoneeDbHelper.C_FORM_NAME);
        int category    = cursor.getColumnIndex(DoneeDbHelper.C_FORM_CATEGORY);
        int description = cursor.getColumnIndex(DoneeDbHelper.C_FORM_DESCRIPTION);
        int useLocation = cursor.getColumnIndex(DoneeDbHelper.C_FORM_USE_LOCATION);
        int hasIcon     = cursor.getColumnIndex(DoneeDbHelper.C_FORM_HAS_ICON);

        Form form = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            form = new Form(
                    cursor.getString(id),
                    cursor.getString(name),
                    cursor.getString(category),
                    cursor.getString(description),
                    cursor.getInt(useLocation) != 0,
                    cursor.getInt(hasIcon) != 0
            );
            form.setFields(FieldDao.find(db, form));
        }
        cursor.close();

        return form;
    }

    public static void removeAllFromUser(Context context, User user) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();
        String[] args = {user.getId()};
        db.delete(DoneeDbHelper.T_USER_FORM, DoneeDbHelper.C_UF_USER + " = ?", args);
    }

}
