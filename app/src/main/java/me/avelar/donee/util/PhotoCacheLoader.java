package me.avelar.donee.util;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import me.avelar.donee.R;

@SuppressWarnings("WeakerAccess")
public final class PhotoCacheLoader {

    private static final int PHOTO_SIZE = 256;

    public static void loadFormIcon(final Context context, String iconPath, final ImageView iv) {
        final Uri photoUri = Uri.parse(iconPath);

        Picasso.with(context)
            .load(photoUri)
            .networkPolicy(NetworkPolicy.OFFLINE)
            .into(iv, new Callback() {
                @Override public void onSuccess() { }

                @Override public void onError() {
                    // Try again online if cache failed
                    Picasso.with(context)
                        .load(photoUri)
                        .resize(PHOTO_SIZE, PHOTO_SIZE).centerCrop()
                        .placeholder(R.drawable.form_placeholder)
                        .error(R.drawable.form_placeholder)
                        .into(iv);
                }
            });
    }

    public static void loadUserPhoto(final Context context, String photoPath, final ImageView iv) {
        final Uri photoUri = Uri.parse(photoPath);

        Picasso.with(context)
            .load(photoUri)
            .transform(new CircleTransform())
            .networkPolicy(NetworkPolicy.OFFLINE)
            .into(iv, new Callback() {
                @Override public void onSuccess() { }

                @Override public void onError() {
                    // Try again online if cache failed
                    Picasso.with(context)
                        .load(photoUri)
                        .resize(PHOTO_SIZE, PHOTO_SIZE).centerCrop()
                        .transform(new CircleTransform())
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder)
                        .into(iv);
                }
            });
    }

}
