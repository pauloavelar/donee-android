package me.avelar.donee.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.avelar.donee.controller.SessionManager;
import me.avelar.donee.model.User;
import me.avelar.donee.util.CircleTransform;
import me.avelar.donee.util.FileManager;
import me.avelar.donee.util.UserPhotoSaver;

public final class UserDAO {

    public static void insert(@NonNull SQLiteDatabase db, @NonNull User user) throws SQLException {
        ContentValues values = new ContentValues();
        values.put(DoneeDbHelper.C_USER_ID, user.getId());
        values.put(DoneeDbHelper.C_USER_NAME, user.getName());
        values.put(DoneeDbHelper.C_USER_EMAIL, user.getEmail());
        values.put(DoneeDbHelper.C_USER_ACCOUNT, user.getAccount());
        
        long result = db.insertWithOnConflict(DoneeDbHelper.T_USER, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);
        if (result == DoneeDbHelper.DB_ERROR) throw new SQLException();
    }

    public static void loadPhoto(@NonNull Context context, @NonNull User user, ImageView iv) {
        String photoName = getPhotoName(context, user);
        if (FileManager.fileExists(context, photoName)) {
            Picasso.with(context).load(FileManager.findFile(context, photoName)).into(iv);
        } else {
            fetchPhotoOnline(context, user, iv);
        }
    }

    public static String getPhotoName(@NonNull Context context, @NonNull User user) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        String[] fields = { DoneeDbHelper.C_USER_PHOTO };
        Cursor cursor = db.query(DoneeDbHelper.T_USER, fields,
                        DoneeDbHelper.C_USER_EMAIL + " = " + user.getId(), null, null, null, null);

        if (cursor.getCount() == 0) return null;
        cursor.moveToFirst();
        String photoName = cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_PHOTO));
        cursor.close();

        return photoName;
    }

    public static void storePhotoName(@NonNull Context context, User user, String photoName) {
        if (user == null || photoName == null) return;

        ContentValues values = new ContentValues();
        values.put(DoneeDbHelper.C_USER_PHOTO, photoName);

        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();
        db.update(DoneeDbHelper.T_USER, values,
                DoneeDbHelper.C_USER_EMAIL + " = " + user.getId(), null);
    }

    public static void fetchPhotoOnline(@NonNull final Context context,
                                        @NonNull final User user, final ImageView iv) {
        Picasso.with(context)
            .load(user.getPhotoUrl())
            .resize(UserPhotoSaver.USER_PHOTO_SIZE, UserPhotoSaver.USER_PHOTO_SIZE)
            .centerCrop().transform(new CircleTransform())
            .into(new UserPhotoSaver(context, user, iv));
    }

    @SuppressWarnings("unused")
    public static void update(@NonNull Context context, @NonNull User user) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DoneeDbHelper.C_USER_ID, user.getId());
        if (user.getName()    != null) values.put(DoneeDbHelper.C_USER_NAME,    user.getName());
        if (user.getEmail()   != null) values.put(DoneeDbHelper.C_USER_EMAIL,   user.getEmail());
        if (user.getAccount() != null) values.put(DoneeDbHelper.C_USER_ACCOUNT, user.getAccount());
        if (user.getLastSynced()  > 0) values.put(DoneeDbHelper.C_USER_SYNCED,  user.getLastSynced());

        String[] args = { user.getId() };
        db.update(DoneeDbHelper.T_USER, values, DoneeDbHelper.C_USER_ID + " = ?", args);
    }

    public static User find(@NonNull Context context, String id) {
        SQLiteDatabase db = DoneeDbHelper.getInstance(context).getReadableDatabase();
        String[] args = { id };
        Cursor cursor = db.query(DoneeDbHelper.T_USER, null,
                DoneeDbHelper.C_USER_ID + " = ?", args, null, null, null);

        if (cursor.getCount() == 0) return null;
        cursor.moveToFirst();
        String name    = cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_NAME));
        String email   = cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_EMAIL));
        String account = cursor.getString(cursor.getColumnIndex(DoneeDbHelper.C_USER_ACCOUNT));
        long   updated = cursor.getLong  (cursor.getColumnIndex(DoneeDbHelper.C_USER_SYNCED));
        User user = new User(id, name, email, account, updated);
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