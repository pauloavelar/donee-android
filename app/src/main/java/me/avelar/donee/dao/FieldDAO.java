package me.avelar.donee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.ArrayList;
import java.util.List;

import me.avelar.donee.model.Field;
import me.avelar.donee.model.Form;
import me.avelar.donee.model.ValidationRule;

public class FieldDAO {

    public static void insert(SQLiteDatabase db, List<Field> fields, Form form) throws SQLiteException {
        if (fields == null) return;

        deleteAll(db, form);
        for (Field field : fields) {
            insert(db, field, form, fields.indexOf(field));
        }
    }

    private static void insert(SQLiteDatabase db, Field field, Form form, int order) throws SQLException {
        if (db == null || !db.isOpen() || field == null || form == null) throw new SQLException();
        final int REPLACE = SQLiteDatabase.CONFLICT_REPLACE;

        ContentValues values = new ContentValues();

        values.put(DoneeDbHelper.C_FIELD_ID,        field.getId());
        values.put(DoneeDbHelper.C_FIELD_FORM,      form.getId());
        values.put(DoneeDbHelper.C_FIELD_TYPE,      field.getType().toString());
        values.put(DoneeDbHelper.C_FIELD_LABEL,     field.getLabel());
        values.put(DoneeDbHelper.C_FIELD_HINT,      field.getHint());
        values.put(DoneeDbHelper.C_FIELD_HEIGHT,    field.getHeight());
        values.put(DoneeDbHelper.C_FIELD_MULTILINE, field.isMultiline());
        values.put(DoneeDbHelper.C_FIELD_ORDER,     order);

        if (field.getOptions() != null) {
            String options = field.getOptionsAsString();
            values.put(DoneeDbHelper.C_FIELD_OPTIONS, options);
        }
        if (field.getStarting() != null) {
            String starting = field.getStartingAsString();
            values.put(DoneeDbHelper.C_FIELD_STARTING, starting);
        }

        ValidationRule rule = field.getRule();
        if (rule != null) {
            values.put(DoneeDbHelper.C_FIELD_REQUIRED,  rule.isRequired());
            values.put(DoneeDbHelper.C_FIELD_REGEXP,    rule.getRegexp());
            values.put(DoneeDbHelper.C_FIELD_MIN_VALUE, rule.getMinValue());
            values.put(DoneeDbHelper.C_FIELD_MAX_VALUE, rule.getMaxValue());
            values.put(DoneeDbHelper.C_FIELD_MESSAGE,   rule.getMessage());
        }

        long result = db.insertWithOnConflict(DoneeDbHelper.T_FIELD, null, values, REPLACE);
        if (result == DoneeDbHelper.DB_ERROR) throw new SQLiteException();
    }

    public static ArrayList<Field> find(Context context, Form form) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        return find(db, form);
    }

    public static ArrayList<Field> find(SQLiteDatabase db, Form form) {
        ArrayList<Field> fields = new ArrayList<>();
        String[] args = { form.getId() };
        Cursor cursor = db.query(DoneeDbHelper.V_FIELDS, null, DoneeDbHelper.C_FIELD_FORM + " = ?",
                                                   args, null, null, DoneeDbHelper.C_FIELD_ORDER);

        int id        = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_ID);
        int type      = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_TYPE);
        int label     = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_LABEL);
        int hint      = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_HINT);
        int height    = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_HEIGHT);
        int multiline = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_MULTILINE);
        int options   = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_OPTIONS);
        int starting  = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_STARTING);
        int hasRule   = cursor.getColumnIndex(DoneeDbHelper.C_VFIELD_HAS_RULE);
        int required  = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_REQUIRED);
        int regexp    = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_REGEXP);
        int minValue  = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_MIN_VALUE);
        int maxValue  = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_MAX_VALUE);
        int message   = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_MESSAGE);

        while (cursor.moveToNext()) {
            Field field = new Field(
                    cursor.getString(id),
                    cursor.getString(type),
                    cursor.getString(label),
                    cursor.getString(hint),
                    cursor.getInt(height),
                    cursor.getInt(multiline) != 0,
                    cursor.getString(options),
                    cursor.getString(starting)
            );
            if (cursor.getInt(hasRule) != 0) {
                ValidationRule rule = new ValidationRule(
                        cursor.getInt(required) != 0,
                        cursor.getString(regexp),
                        cursor.isNull(minValue) ? null : cursor.getDouble(minValue),
                        cursor.isNull(maxValue) ? null : cursor.getDouble(maxValue),
                        cursor.getString(message)
                );
                field.setRule(rule);
            }
            fields.add(field);
        }
        cursor.close();

        return fields;
    }

    public static void deleteAll(SQLiteDatabase db, Form form) throws SQLiteException {
        if (db == null || form == null) throw new SQLiteException();

        String[] args = { form.getId() };
        db.delete(DoneeDbHelper.T_FIELD, DoneeDbHelper.C_FIELD_FORM + " = ?", args);
    }

}
