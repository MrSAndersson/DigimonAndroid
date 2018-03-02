package se.standersson.icingalert;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseCommunication extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //Create the intent
        Intent resultIntent = new Intent(this, LoginActivity.class);

        //Create a pending intent from the intent since it's not starting now
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);


        //Set the details of the notification to be shown to the user
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "IcingAlert")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setAutoCancel(true)
                        .setColor(getColor(R.color.colorAccent))
                        .setContentIntent(resultPendingIntent);


    // Sets an ID for the notification
        int mNotificationId = 1;
    // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    // Builds the notification and issues it.
        assert mNotifyMgr != null;
        mNotifyMgr.notify(mNotificationId, notification.build());
    }
}
