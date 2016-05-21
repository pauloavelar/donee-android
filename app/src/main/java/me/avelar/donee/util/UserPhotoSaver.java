package me.avelar.donee.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileOutputStream;
import java.io.IOException;

import me.avelar.donee.dao.UserDAO;
import me.avelar.donee.model.User;

public class UserPhotoSaver implements Target {

    public static final int    USER_PHOTO_SIZE = 150;
    public static final String PHOTO_EXTENSION = ".jpg";

    private Context   context;
    private User      user;
    private ImageView imageView;

    public UserPhotoSaver(Context context, User user, ImageView imageView) {
        this.context   = context;
        this.user      = user;
        this.imageView = imageView;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        RandomString random = new RandomString(6);
        String file = random.nextString() + user.getId() + PHOTO_EXTENSION;
        try {
            FileOutputStream ostream = context.openFileOutput(file, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
            ostream.close();
            UserDAO.storePhotoName(context, user, file);
            loadPhotoIntoView(context, file, imageView);
        } catch (IOException ioe) {
            context.deleteFile(file);
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {}

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {}

    public void loadPhotoIntoView(Context context, String photoName, ImageView imageView) {
        String path = FileManager.findFilePath(context, photoName);
        if (path != null && imageView != null) {
            Picasso.with(context).load(path).into(imageView);
        }
    }

}
