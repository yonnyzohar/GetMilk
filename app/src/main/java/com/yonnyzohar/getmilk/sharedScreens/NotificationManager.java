package com.yonnyzohar.getmilk.sharedScreens;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.yonnyzohar.getmilk.Methods;
import com.yonnyzohar.getmilk.data.Model;

public class NotificationManager extends FirebaseMessagingService {

    private static final int BROADCAST_NOTIFICATION_ID = 1;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Methods.log("NEW_TOKEN",s);
        Model.fireBaseMessagingToken = s;
    }


    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String notificationBody = "";
        String notificationTitle = "";
        String notificationData = "";
        try{
            notificationData = remoteMessage.getData().toString();
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        }catch (NullPointerException e){
            Methods.log(Model.TAG, "onMessageReceived: NullPointerException: " + e.getMessage() );
        }
        Methods.log(Model.TAG, "onMessageReceived: data: " + notificationData);
        Methods.log(Model.TAG, "onMessageReceived: notification body: " + notificationBody);
        Methods.log(Model.TAG, "onMessageReceived: notification title: " + notificationTitle);


        /*String dataType = remoteMessage.getData().get(getString(R.string.data_type));
        if(dataType.equals(getString(R.string.direct_message))){
            Methods.log(Model.TAG, "onMessageReceived: new incoming message.");
            String title = remoteMessage.getData().get(getString(R.string.data_title));
            String message = remoteMessage.getData().get(getString(R.string.data_message));
            String messageId = remoteMessage.getData().get(getString(R.string.data_message_id));
            //sendMessageNotification(title, message, messageId);
        }*/
    }

    /**
     * Build a push notification for a chat message
     * @param title
     * @param message
     *//*
    private void sendMessageNotification(String title, String message, String messageId){
        Methods.log(Model.TAG, "sendChatmessageNotification: building a chatmessage notification");

        //get the notification id
        int notificationId = buildNotificationId(messageId);

        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.default_notification_channel_id));
        // Creates an Intent for the Activity
        Intent pendingIntent = new Intent(this, UserListActivity.class);
        // Sets the Activity to start in a new, empty task
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        builder.setSmallIcon(R.drawable.ic_android_blue)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.drawable.ic_android_blue))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setColor(getColor(R.color.blue1))
                .setAutoCancel(true)
                //.setSubText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setOnlyAlertOnce(true);

        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(notificationId, builder.build());

    }


    private int buildNotificationId(String id){
        Methods.log(Model.TAG, "buildNotificationId: building a notification id.");

        int notificationId = 0;
        for(int i = 0; i < 9; i++){
            notificationId = notificationId + id.charAt(0);
        }
        Methods.log(Model.TAG, "buildNotificationId: id: " + id);
        Methods.log(Model.TAG, "buildNotificationId: notification id:" + notificationId);
        return notificationId;
    }*/

}