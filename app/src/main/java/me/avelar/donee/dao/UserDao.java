package me.avelar.donee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.avelar.donee.controller.SessionManager;
import me.avelar.donee.model.User;

@SuppressWarnings("WeakerAccess")
public final class UserDao {

    public static void insert(@NonNull SQLiteDatabase db, @NonNull User user) throws SQLException {
        ContentValues values = createValues(user);
        if (!isUserStored(db, user)) {
            db.insertOrThrow(DoneeDbHelper.T_USER, null, values);
        } else update(db, values);
    }

    private static boolean isUserStored(@NonNull SQLiteDatabase db, User user) {
        if (user == null) return false;

        Cursor cursor = db.query(DoneeDbHelper.T_USER, new String[]{ DoneeDbHelper.C_USER_ID },
                DoneeDbHelper.C_USER_ID + " = ?", new String[]{ user.getId() }, null, null, null);

        boolean result = cursor.getCount() > 0;
        cursor.close();

        return result;
    }

    public static void update(@NonNull Context context, @NonNull User user) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();
        ContentValues values = createValues(user);
        update(db, values);
    }

    private static void update(@NonNull SQLiteDatabase db, @NonNull ContentValues values) {
        String[] args = { values.getAsString(DoneeDbHelper.C_USER_ID) };
        db.update(DoneeDbHelper.T_USER, values, DoneeDbHelper.C_USER_ID + " = ?", args);
    }

    private static ContentValues createValues(@NonNull User user) {
        ContentValues vals = new ContentValues();

        vals.put(DoneeDbHelper.C_USER_ID, user.getId());
        if (user.getName()    != null) vals.put(DoneeDbHelper.C_USER_NAME,    user.getName());
        if (user.getEmail()   != null) vals.put(DoneeDbHelper.C_USER_EMAIL,   user.getEmail());
        if (user.getAccount() != null) vals.put(DoneeDbHelper.C_USER_ACCOUNT, user.getAccount());
        if (user.getLastSynced()  > 0) vals.put(DoneeDbHelper.C_USER_SYNCED,  user.getLastSynced());

        return vals;
    }

    public static User find(@NonNull Context context, String id) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        String[] args = { id };
        Cursor cursor = db.query(DoneeDbHelper.T_USER, null, DoneeDbHelper.C_USER_ID + " = ?",
                                 args, null, null, null, "1");

        if (cursor.getCount() == 0) return null;
        cursor.moveToFirst();
        User user = new User(
                id,
                cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_NAME)),
                cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_EMAIL)),
                cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_ACCOUNT)),
                cursor.getLong  (cursor.getColumnIndex(DoneeDbHelper.C_USER_SYNCED)));
        cursor.close();

        return user;
    }

    public static List<User> getAll(@NonNull Context context) {
        ArrayList<User> users = new ArrayList<>();
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = db.query(DoneeDbHelper.T_USER, null, null,
                                 null, null, null, DoneeDbHelper.C_USER_NAME);

        while (cursor.moveToNext()) {
            users.add(new User(
                cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_ID)),
                cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_NAME)),
                cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_EMAIL)),
                cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_ACCOUNT)),
                cursor.getLong  (cursor.getColumnIndex(DoneeDbHelper.C_USER_SYNCED))
            ));
        }
        cursor.close();

        return users;
    }

    public static List<User> getOthers(Context context) {
        List<User> others = getAll(context);
        others.remove(SessionManager.getLastSession(context).getUser());
        return others;
    }

    public static void notifySync(Context context, User user) {
        // sets the lastSynced attribute to current time and updates in DB
        update(context, user.setLastSynced(new Date().getTime()));
    }

    public static void delete(Context context, User user) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();
        String[] args = {user.getId()};
        db.delete(DoneeDbHelper.T_USER, DoneeDbHelper.C_USER_ID + " = ?", args);
    }
}