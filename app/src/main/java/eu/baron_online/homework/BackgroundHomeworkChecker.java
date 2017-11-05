package eu.baron_online.homework;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class BackgroundHomeworkChecker extends IntentService {

    public static BackgroundHomeworkChecker instance;

    public boolean homeworkListActivityInited = false;

    private NotificationManager mNotificationManager;
    private ArrayList<Integer> displayedNotClickedIDs = new ArrayList<>();

    public BackgroundHomeworkChecker() {
        super("BackgroundHomeworkChecker");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if(HomeworkListActivity.instance == null) {
            return;
        }

        //set instance
        BackgroundHomeworkChecker.instance = this;

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        /*while(true) {
            //get ignore array
            int[] ignoreArray = combineArrays(arraylistToArray(displayedNotClickedIDs), HomeworkListActivity.instance.getIdArray());

            String ignoreList = "0";
            ignoreList = Arrays.toString(ignoreArray).replace('[', ' ').replace(']', ' ').replaceAll(" ", "");

            List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));
            jsonParams.add(new BasicNameValuePair("ignore", ignoreList));

            JSONObject result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_get_all.php", "GET", jsonParams);
            handleJSONResult(result);

            try {
                Thread.sleep(DataInterchange.getPersistentInt("homeworkCheckerSleepTime"));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }

    public void addIgnoreID(int ignoreID) {
        displayedNotClickedIDs.add(ignoreID);
    }

    private void handleJSONResult(JSONObject result) {
        try {
            JSONArray newEntrys = result.getJSONArray("entrys");

            for(int i = 0; i < newEntrys.length(); i++) {
                Log.v("baron-online.eu", "New homework found!");

                JSONObject object = newEntrys.getJSONObject(i);

                String untilStr = object.getString("UNTIL");
                Log.v("baron-online.eu", "untilStr: " + untilStr);


                SimpleDateFormat serverFmt = new SimpleDateFormat(getResources().getString(R.string.server_date_format));
                Date date = serverFmt.parse(untilStr);

                SimpleDateFormat userFmt = new SimpleDateFormat(getResources().getString(R.string.local_date_format));
                untilStr = userFmt.format(date);

                Log.v("baron-online.eu", "New untilStr: " + untilStr);

                Intent intent = new Intent(this, HomeworkEntryDetailActivity.class);
                intent.setAction(Long.toString(System.currentTimeMillis()));
                intent.putExtra("id", object.getInt("ID"));

                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                showNotification(getResources().getString(R.string.new_homework_title), String.format(getResources().getString(R.string.new_homework_text), object.getString("SUBJECT"), untilStr), contentIntent, R.drawable.ic_notifications_black_24dp, object.getInt("ID"));

                addIgnoreID(object.getInt("ID"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private int[] getUserDoneIDs() {
        ArrayList<Integer> ignoreIDs = new ArrayList<>();

        List<NameValuePair> jsonParams = new ArrayList<>();
        jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
        jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));

        JSONObject result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_get_entrys_done.php", "GET", jsonParams);
        try {
            JSONArray newEntrys = result.getJSONArray("entrys");

            for(int i = 0; i < newEntrys.length(); i++) {
                JSONObject object = newEntrys.getJSONObject(i);

                ignoreIDs.add(object.getInt("HOMEWORK_ID"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return arraylistToArray(ignoreIDs);
    }

    private void showNotification(String title, String text, PendingIntent contentIntent, int icon, int notificationID) {
        if(notificationExists(notificationID)) {
            return;
        }

        long[] vibratePattern = {0, 500, 500, 500};

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);
        mBuilder.setSmallIcon(icon);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setVibrate(vibratePattern);
        mBuilder.setAutoCancel(true);

        Intent intent = new Intent(this, NotificationDeleteReceiver.class);
        intent.setAction(Long.toString(System.currentTimeMillis()));
        intent.putExtra("id", notificationID);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setDeleteIntent(pendingIntent);

        mNotificationManager.notify(notificationID, mBuilder.build());
    }

    private int[] combineArrays(int[] array1, int[] array2) {
        int[] array1and2 = new int[array1.length + array2.length];

        System.arraycopy(array1, 0, array1and2, 0, array1.length);
        System.arraycopy(array2, 0, array1and2, array1.length, array2.length);

        return array1and2;
    }

    private boolean notificationExists(int id) {
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();

        for(StatusBarNotification notification : notifications) {
            if(notification.getId() == id) {
                return true;
            }
        }

        return false;
    }

    private int[] arraylistToArray(ArrayList<Integer> arrayList) {
        int[] result = new int[arrayList.size()];

        for(int i = 0; i < arrayList.size(); i++) {
            result[i] = arrayList.get(i);
        }

        return result;
    }
}
