package me.avelar.donee.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import me.avelar.donee.R;
import me.avelar.donee.model.User;
import me.avelar.donee.web.UrlRepository;

public class UserAdapter extends ArrayAdapter<User> {

    private static class ViewHolder {
        ImageView userPhoto;
        TextView userName;
        TextView userAccount;

    }

    public UserAdapter(Context context, int resource) {
        super(context, resource);
    }

    @NonNull
    @Override
    public View getView(final int pos, final View convertView, @NonNull final ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            rowView = inflater.inflate(R.layout.user_card, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.userPhoto  = (ImageView) rowView.findViewById(R.id.user_photo);
            holder.userName    = (TextView) rowView.findViewById(R.id.user_name);
            holder.userAccount = (TextView) rowView.findViewById(R.id.user_account);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                rowView.setBackgroundResource(R.drawable.user_card_bg);
            }
            rowView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();

        User item = getItem(pos);
        if (item != null) {
            String userPhotoUrl = UrlRepository.getUserPhotoUrl(item.getId());
            PhotoCacheLoader.loadUserPhoto(getContext(), userPhotoUrl, holder.userPhoto);
            holder.userName.setText(item.getName());
            holder.userAccount.setText(item.getAccount());
        }
        return rowView;
    }

}
