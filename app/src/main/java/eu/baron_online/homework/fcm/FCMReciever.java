package eu.baron_online.homework.fcm;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import eu.baron_online.homework.DataInterchange;
import eu.baron_online.homework.HomeworkEntryDetailActivity;
import eu.baron_online.homework.HomeworkListActivity;
import eu.baron_online.homework.R;

public class FCMReciever extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("baron-online.eu", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("baron-online.eu", "Message data payload: " + remoteMessage.getData());
            onDataRecieved(remoteMessage.getData().toString());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification n = remoteMessage.getNotification();

            Log.d("baron-online.eu", "Message Notification Body: " + n.getBody() + "\nMessage Notification Title: " + n.getTitle());
            showNotification(45899, n.getTitle(), n.getBody());
        }
    }

    private void showNotification(int id, String title, String text, Class<? extends Activity> resultActivity, Intent resultIntent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(title)
                .setContentText(text)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        mBuilder.setContentIntent(
                PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        );

        NotificationManager mNotificationManager = (NotificationManager)  getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }

    private void showNotification(int id, String title, String text) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(title)
                .setContentText(text)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager mNotificationManager = (NotificationManager)  getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(id, mBuilder.build());
    }

    private void onDataRecieved(String data) {
        //check if user is logged in, if not, quit
        if(!DataInterchange.existsPersistent("username")) {
            return;
        }

        try {
            data = data.replace("{value=", "");
            data = data.substring(0, data.length() - 1);

            JSONObject object = new JSONObject(data);

            //if notification is not for currently logged in user, quit
            /*if(!object.getString("USERNAME").equals(DataInterchange.getPersistentString("username"))) {
                return;
            }*/

            String untilStr = object.getString("UNTIL");

            SimpleDateFormat serverFmt = new SimpleDateFormat(getResources().getString(R.string.server_date_format));
            Date date = serverFmt.parse(untilStr);

            SimpleDateFormat userFmt = new SimpleDateFormat(getResources().getString(R.string.local_date_format));
            untilStr = userFmt.format(date);

            Intent resultIntent = new Intent(this, HomeworkEntryDetailActivity.class);
            resultIntent.putExtra("id", object.getInt("ENTRY_ID"));

            showNotification(
                    object.getInt("ENTRY_ID"),
                    String.format(getResources().getString(R.string.notification_new_homework_title), object.getString("SUBJECT")),
                    String.format(getResources().getString(R.string.notification_new_homework_text), object.getString("MEDIA"), object.getString("PAGE"), untilStr),
                    HomeworkEntryDetailActivity.class,
                    resultIntent
            );
        } catch (JSONException e) {
            Log.e("baron-online.eu", e.getMessage());
        } catch (ParseException e) {
            Log.e("baron-online.eu", e.getMessage());
        }
    }
}
