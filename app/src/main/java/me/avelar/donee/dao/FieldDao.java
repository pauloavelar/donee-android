package me.avelar.donee.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.avelar.donee.model.Field;
import me.avelar.donee.model.Form;
import me.avelar.donee.model.ValidationRule;

// Accessible only from within other DAOs (package-private)
class FieldDao {

    static void insert(SQLiteDatabase db, List<Field> fields, Form form) throws SQLException {
        if (db == null || !db.isOpen() || form == null) throw new SQLException();

        // removing all fields in case some got removed from the form
        deleteRemoved(db, form);

        // readding all fields
        if (fields == null) return;
        for (Field field : fields) {
            insert(db, field, form, fields.indexOf(field));
        }
    }

    private static void insert(@NonNull SQLiteDatabase db, Field field, Form form,
                               int order) throws SQLException {
        if (!db.isOpen() || field == null || form == null) throw new SQLException();

        ContentValues vals = new ContentValues();
        vals.put(DoneeDbHelper.C_FIELD_ID,        field.getId());
        vals.put(DoneeDbHelper.C_FIELD_FORM,      form.getId());
        vals.put(DoneeDbHelper.C_FIELD_TYPE,      field.getType().toString());
        vals.put(DoneeDbHelper.C_FIELD_LABEL,     field.getLabel());
        vals.put(DoneeDbHelper.C_FIELD_HINT,      field.getHint());
        vals.put(DoneeDbHelper.C_FIELD_HEIGHT,    field.getHeight());
        vals.put(DoneeDbHelper.C_FIELD_MULTILINE, field.isMultiline());
        vals.put(DoneeDbHelper.C_FIELD_ORDER,     order);

        if (field.getOptions() != null) {
            String options = field.getOptionsAsString();
            vals.put(DoneeDbHelper.C_FIELD_OPTIONS, options);
        }
        if (field.getStarting() != null) {
            String starting = field.getStartingAsString();
            vals.put(DoneeDbHelper.C_FIELD_STARTING, starting);
        }

        ValidationRule rule = field.getRule();
        if (rule != null) {
            vals.put(DoneeDbHelper.C_FIELD_REQUIRED,  rule.isRequired());
            vals.put(DoneeDbHelper.C_FIELD_REGEXP,    rule.getRegexp());
            vals.put(DoneeDbHelper.C_FIELD_MIN_VALUE, rule.getMinValue());
            vals.put(DoneeDbHelper.C_FIELD_MAX_VALUE, rule.getMaxValue());
            vals.put(DoneeDbHelper.C_FIELD_MESSAGE,   rule.getMessage());
        }

        db.insertWithOnConflict(DoneeDbHelper.T_FIELD, null, vals, SQLiteDatabase.CONFLICT_REPLACE);
    }

    static ArrayList<Field> find(SQLiteDatabase db, Form form) {
        ArrayList<Field> fields = new ArrayList<>();
        String[] args = { form.getId() };
        Cursor cursor = db.query(DoneeDbHelper.V_FIELDS, null, DoneeDbHelper.C_FIELD_FORM + " = ?",
                                                   args, null, null, DoneeDbHelper.C_FIELD_ORDER);

        final int ID        = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_ID);
        final int TYPE      = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_TYPE);
        final int LABEL     = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_LABEL);
        final int HINT      = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_HINT);
        final int HEIGHT    = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_HEIGHT);
        final int MULTILINE = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_MULTILINE);
        final int OPTIONS   = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_OPTIONS);
        final int STARTING  = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_STARTING);
        final int HAS_RULE  = cursor.getColumnIndex(DoneeDbHelper.C_VFIELD_HAS_RULE);
        final int REQUIRED  = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_REQUIRED);
        final int REGEXP    = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_REGEXP);
        final int MIN_VALUE = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_MIN_VALUE);
        final int MAX_VALUE = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_MAX_VALUE);
        final int MESSAGE   = cursor.getColumnIndex(DoneeDbHelper.C_FIELD_MESSAGE);

        while (cursor.moveToNext()) {
            Field field = new Field(
                    cursor.getString(ID),
                    cursor.getString(TYPE),
                    cursor.getString(LABEL),
                    cursor.getString(HINT),
                    cursor.getInt(HEIGHT),
                    cursor.getInt(MULTILINE) != 0,
                    cursor.getString(OPTIONS),
                    cursor.getString(STARTING)
            );
            if (cursor.getInt(HAS_RULE) != 0) {
                ValidationRule rule = new ValidationRule(
                        cursor.getInt(REQUIRED) != 0,
                        cursor.getString(REGEXP),
                        cursor.isNull(MIN_VALUE) ? null : cursor.getDouble(MIN_VALUE),
                        cursor.isNull(MAX_VALUE) ? null : cursor.getDouble(MAX_VALUE),
                        cursor.getString(MESSAGE)
                );
                field.setRule(rule);
            }
            fields.add(field);
        }
        cursor.close();

        return fields;
    }

    private static void deleteRemoved(SQLiteDatabase db, Form form) throws SQLException {
        if (db == null || !db.isOpen() || form == null) throw new SQLException();

        ArrayList<String> fieldIds = new ArrayList<>();

        // the first argument is the form ID
        fieldIds.add(form.getId());

        // the remaining arguments are the field IDs
        if (form.getFields() != null) {
            for (Field field : form.getFields()) {
                fieldIds.add(field.getId());
            }
        }
        // if length == 1 (form ID only) add a dummy zero to remove all other fields
        if (fieldIds.size() == 1) fieldIds.add("0");
        String [] args = fieldIds.toArray(new String[fieldIds.size()]);

        // getting the selection string with as many ? as array length - 1 (form ID already there)
        String where = DoneeDbHelper.C_FIELD_FORM + " = ? AND " +
                       DoneeDbHelper.C_FIELD_ID + " NOT IN (" + generateArgs(args.length - 1) + ")";

        db.delete(DoneeDbHelper.T_FIELD, where, args);
    }

    private static String generateArgs(int argCount) {
        return TextUtils.join(",", Collections.nCopies(argCount, "?"));
    }

}