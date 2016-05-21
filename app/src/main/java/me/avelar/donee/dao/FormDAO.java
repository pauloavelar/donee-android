package me.avelar.donee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import me.avelar.donee.model.Form;
import me.avelar.donee.model.User;

public class FormDAO {

    public static void insert(Context context, ArrayList<Form> forms, User user) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        db.beginTransaction();
        try {
            for (Form form : forms) {
                insert(db, form, user);
            }
            UserDAO.notifySync(context, user);
            db.setTransactionSuccessful();
        } catch (SQLException ignore) { }
        finally {
            db.endTransaction();
        }
    }

    private static void insert(SQLiteDatabase db, Form form, User user) throws SQLException {
        if (db == null || !db.isOpen() || form == null || user == null) throw new SQLException();
        final int IGNORE  = SQLiteDatabase.CONFLICT_IGNORE;
        final int REPLACE = SQLiteDatabase.CONFLICT_REPLACE;

        ContentValues formValues = new ContentValues();
        formValues.put(DoneeDbHelper.C_FORM_ID,           form.getId());
        formValues.put(DoneeDbHelper.C_FORM_NAME,         form.getName());
        formValues.put(DoneeDbHelper.C_FORM_CATEGORY,     form.getCategory());
        formValues.put(DoneeDbHelper.C_FORM_DESCRIPTION,  form.getDescription());
        formValues.put(DoneeDbHelper.C_FORM_USE_LOCATION, form.usesLocation());

        ContentValues permission = new ContentValues();
        permission.put(DoneeDbHelper.C_UF_FORM, form.getId());
        permission.put(DoneeDbHelper.C_UF_USER, user.getId());

        db.insertWithOnConflict(DoneeDbHelper.T_FORM, null, formValues, IGNORE);

        long resultP = db.insertWithOnConflict(DoneeDbHelper.T_USER_FORM, null, permission, REPLACE);
        FieldDAO.insert(db, form.getFields(), form);

        if (resultP == DoneeDbHelper.DB_ERROR) throw new SQLException();
    }

    public static ArrayList<Form> find(Context context, User user) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        ArrayList<Form> forms = new ArrayList<>();
        String[] args = { user.getId() };
        Cursor cursor = db.query(DoneeDbHelper.V_USER_FORM, null,
                                 DoneeDbHelper.C_UF_USER + " = ?", args, null, null, null);

        int id          = cursor.getColumnIndex(DoneeDbHelper.C_FORM_ID);
        int name        = cursor.getColumnIndex(DoneeDbHelper.C_FORM_NAME);
        int category    = cursor.getColumnIndex(DoneeDbHelper.C_FORM_CATEGORY);
        int description = cursor.getColumnIndex(DoneeDbHelper.C_FORM_DESCRIPTION);
        int useLocation = cursor.getColumnIndex(DoneeDbHelper.C_FORM_USE_LOCATION);

        while (cursor.moveToNext()) {
            Form form = new Form(
                cursor.getString(id),
                cursor.getString(name),
                cursor.getString(category),
                cursor.getString(description),
                cursor.getInt(useLocation) != 0
            );
            form.setFields(FieldDAO.find(context, form));
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

        if (cursor.getCount() == 0) return null;

        cursor.moveToFirst();
        Form form = new Form(
                cursor.getString(id),
                cursor.getString(name),
                cursor.getString(category),
                cursor.getString(description),
                cursor.getInt(useLocation) != 0
        );
        form.setFields(FieldDAO.find(db, form));
        cursor.close();

        return form;
    }

    public static void removeAllFromUser(Context context, User user) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();
        String[] args = {user.getId()};

        db.delete(DoneeDbHelper.T_USER_FORM, DoneeDbHelper.C_UF_USER + " = ?", args);
    }

}
