package eu.baron_online.homework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationDeleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getExtras().getInt("id");

        BackgroundHomeworkChecker.instance.addIgnoreID(id);
    }
}
