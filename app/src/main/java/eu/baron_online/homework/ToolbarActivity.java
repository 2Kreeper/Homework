package eu.baron_online.homework;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;


public class ToolbarActivity extends AppCompatActivity {

    public static ToolbarActivity instance;

    protected Toolbar toolbar;
    protected ProgressDialog progressDialog;
    protected static NotificationManager mNotificationManager;

    private int[] menuIgnoreArray = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        //init ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.loading_title));
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(1);

        if(DataInterchange.getValue("username") == null && DataInterchange.existsPersistent("username")) {
            //user is not logged in and login data is saved
            DataInterchange.addValue("username", DataInterchange.getPersistentString("username"));
            DataInterchange.addValue("password", DataInterchange.getPersistentString("password"));
        }
    }

    protected void setToolbarTitle(String title) {
        ((TextView) ((Toolbar) findViewById(R.id.toolbar)).findViewById(R.id.toolbar_title)).setText(title);
    }

    protected int findIndex(Object[] array, Object item) {
        for(int i = 0; i < array.length; i++) {
            if(array[i].equals(item)) {
                return i;
            }
        }
        return 0;
    }

    protected void setLoading(boolean loading) {
        if(loading) {
            progressDialog.show();
        } else {
            progressDialog.setProgress(progressDialog.getMax());
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if(DataInterchange.getValue("actionbar_ignore") != null) {
            menuIgnoreArray = (int[]) DataInterchange.getValue("actionbar_ignore");

            for(int i : menuIgnoreArray) {
                menu.findItem(i).setVisible(false);
            }
            menu.findItem(R.id.action_settings).setVisible(false);
        }

        DataInterchange.removeValue("actionbar_ignore");

        if(DataInterchange.getValue("actionbar_options_visible") != null) {
            boolean optionsVisible = (boolean) DataInterchange.getValue("actionbar_options_visible");

            for(int i = 0; i < menu.size(); i++) {
                if(optionsVisible) {
                    if(!Arrays.asList(menuIgnoreArray).contains(i)) { //only make option visible if it is NOT marked as ignored (if ignored it has to be invisible all the time)
                        menu.getItem(i).setVisible(true);
                    }
                } else {
                    menu.getItem(i).setVisible(false);
                }
            }
        }

        DataInterchange.removeValue("actionbar_options_visible");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_logout:
                logout();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    protected void logout() {
        DataInterchange.removePersisten("username");
        DataInterchange.removePersisten("password");

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            String sha256text = Base64.encodeToString(hash, Base64.DEFAULT);

            return sha256text;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected int[] arrayListToArray(ArrayList<Integer> arrayList) {
        int[] result = new int[arrayList.size()];

        for(int i = 0; i < arrayList.size(); i++) {
            result[i] = arrayList.get(i);
        }

        return result;
    }

    public static boolean notificationExists(int id) {
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();

        for(StatusBarNotification notification : notifications) {
            if(notification.getId() == id) {
                return true;
            }
        }

        return false;
    }

    public static String changeDateFormat(String date, String originalFmtString, String targetFmtString) {
        try {
            SimpleDateFormat originalFmt = new SimpleDateFormat(originalFmtString);
            Date dateObject = originalFmt.parse(date);

            SimpleDateFormat targetFmt = new SimpleDateFormat(targetFmtString);
            return targetFmt.format(dateObject);
        } catch (ParseException e) {
            Log.e("baron-online.eu", e.getMessage());
        }

        return null;
    }

    protected void makeHTTPRequest(String url, HashMap<String, String> params) {
        new HTTPRequestor(url, params).execute();
    }

    protected void makeHTTPRequest(String url, HashMap<String, String> params, OnRequestFinishedListener postExecute) {
        new HTTPRequestor(url, params, postExecute).execute();
    }

    interface OnRequestFinishedListener {
        void onRequestFinished(JSONObject object);
    }

    private class HTTPRequestor extends AsyncTask<String, String, String> {
        private JSONObject result;
        private String url;
        private HashMap<String, String> params;
        private OnRequestFinishedListener listener = null;

        public HTTPRequestor(String url, HashMap<String, String> params) {
            this.url = url;
            this.params = params;
        }

        public HTTPRequestor(String url, HashMap<String, String> params, OnRequestFinishedListener listener) {
            this.url = url;
            this.params = params;
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... strParams) {
            result = JSONParser.makeHttpRequest(url, "GET", params);

            return null;
        }

        protected void onPostExecute(String str) {
            if(listener != null) {
                listener.onRequestFinished(result);
            }
        }
    }
}















