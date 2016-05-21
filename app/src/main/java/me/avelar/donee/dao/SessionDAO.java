package me.avelar.donee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import me.avelar.donee.model.Session;
import me.avelar.donee.model.User;

public final class SessionDAO {

    public static void insert(@NonNull Context context, @NonNull Session session) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();
        User user = session.getUser();

        ContentValues sessionValues = new ContentValues();
        sessionValues.put(DoneeDbHelper.C_SESSION_ID,   session.getId());
        sessionValues.put(DoneeDbHelper.C_SESSION_USER, user.getId());

        db.beginTransaction();
        try {
            UserDAO.insert(db, user);
            long result = db.insertWithOnConflict(DoneeDbHelper.T_SESSION, null,
                                        sessionValues, SQLiteDatabase.CONFLICT_REPLACE);
            if (result == DoneeDbHelper.DB_ERROR) throw new SQLException();

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public static void update(@NonNull Context context, @NonNull Session session) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DoneeDbHelper.C_SESSION_ID, session.getId());
        values.put(DoneeDbHelper.C_SESSION_USER, session.getUser().getId());
        values.put(DoneeDbHelper.C_SESSION_TIME, session.getLastUsed().getTime());

        String[] args = { session.getId() };
        db.update(DoneeDbHelper.T_SESSION, values, DoneeDbHelper.C_SESSION_ID + " = ?", args);
    }

    public static Session find(@NonNull Context context, String fieldName, String fieldValue) {
        if (fieldValue == null) return null;

        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        String[] args = { fieldValue };
        Cursor cursor = db.query(DoneeDbHelper.T_SESSION, null,
                        fieldName + " = ?", args, null, null, null, "1");
        Session session = convertRecord(context, cursor);
        cursor.close();

        return session;
    }

    public static void delete(@NonNull Context context, String sessionId) {
        if (sessionId == null) return;
        String[] args = { sessionId };
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        db.delete(DoneeDbHelper.T_SESSION, DoneeDbHelper.C_SESSION_ID + " = ?", args);
    }

    private static Session convertRecord(@NonNull Context context, @NonNull Cursor cursor) {
        if (cursor.getCount() == 0) return null;

        cursor.moveToFirst();
        String sessionId = cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_SESSION_ID));
        long userId      = cursor.getLong(cursor.getColumnIndex(DoneeDbHelper.C_SESSION_USER));
        User sessionUser = UserDAO.find(context, String.valueOf(userId));
        return new Session(sessionId, sessionUser);
    }

    public static Session findLastUsed(@NonNull Context context) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(DoneeDbHelper.T_SESSION, null, null, null, null, null,
                                 DoneeDbHelper.C_SESSION_TIME + " DESC", "1");
        Session session = convertRecord(context, cursor);
        cursor.close();
        return session;
    }

}