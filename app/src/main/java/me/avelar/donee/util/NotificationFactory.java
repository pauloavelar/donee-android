package me.avelar.donee.util;

import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import me.avelar.donee.R;
import me.avelar.donee.view.ActivityMain;

@SuppressWarnings("deprecation")
public class NotificationFactory {

    public static final String EXTRA_OUTBOX = "OUTBOX";

    public enum Type { ONGOING_SYNC, FINISHED_SYNC_OK, FINISHED_SYNC_ERROR }

    public static final int  ONGOING_SYNC_ID = 222;
    public static final int FINISHED_SYNC_ID = 223;

    public static Builder create(Context context, Type type) {
        if (context == null) return null;

        Builder builder = new Builder(context)
            .setLights(context.getResources().getColor(R.color.donee_green), 400, 600)
            .setSmallIcon(R.drawable.logo_notification)
            .setContentInfo(context.getResources().getString(R.string.app_name))
            .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setColor(context.getResources().getColor(R.color.donee_blue_dark));
        }

        int contentTitleId = 0, contentTextId = 0;
        switch (type) {
            case ONGOING_SYNC:
                contentTitleId = R.string.sending_collections;
                builder.setAutoCancel(false);
                break;
            case FINISHED_SYNC_OK:
                contentTitleId = R.string.sending_finished;
                contentTextId  = R.string.sending_finished_more;
                break;
            case FINISHED_SYNC_ERROR:
                contentTitleId = R.string.sending_failed;
                contentTextId  = R.string.sending_failed_more;
                break;
        }
        builder.setContentTitle(context.getResources().getString(contentTitleId));
        if (contentTextId != 0) {
            builder.setContentText(context.getResources().getString(contentTextId));
        }

        PendingIntent pendingIntent = createPendingIntent(context);
        builder.setContentIntent(pendingIntent);
        return builder;
    }

    private static PendingIntent createPendingIntent(Context context) {
        Intent resultIntent = new Intent(context, ActivityMain.class);
        resultIntent.putExtra(EXTRA_OUTBOX, true);
        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ActivityMain.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
