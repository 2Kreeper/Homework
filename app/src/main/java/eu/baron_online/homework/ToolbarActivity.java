package eu.baron_online.homework;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;


public class ToolbarActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.loading_title));
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(1);
    }

    protected void setLoading(boolean loading) {
        if(loading) {
            progressDialog.show();
        } else {
            progressDialog.setProgress(progressDialog.getMax());
            progressDialog.dismiss();
        }
    }

    protected void sendNotification(String title, String text, Object targetActivity, HashMap<String, Integer> extras, int icon, int mId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(icon);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(text);

        Intent resultIntent = new Intent(this, targetActivity);
        Iterator it = extras.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            resultIntent.putExtra((String) e.getKey(), (String) e.getValue());
        }

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack((Activity) targetActivity);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(DataInterchange.containsKey("hideSettings") && (Boolean) DataInterchange.getValue("hideSettings")) {
            getMenuInflater().inflate(R.menu.menu_main_no_settings, menu);
        } else if(DataInterchange.containsKey("emptyToolbar") && (Boolean) DataInterchange.getValue("emptyToolbar")) {
            getMenuInflater().inflate(R.menu.menu_empty, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }

        //reset flags
        DataInterchange.addValue("hideSettings", false);
        DataInterchange.addValue("emptyToolbar", false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_logout:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    protected String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            String sha256text = Base64.encodeToString(hash, Base64.DEFAULT);

            Log.v("baron-online.eu", "sha256-result: " + sha256text);

            return sha256text;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "Unabled to hash '" + text + "'!", Toast.LENGTH_SHORT);
        return null;
    }

    protected void setToolbarTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }
}
