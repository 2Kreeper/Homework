package eu.baron_online.homework;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

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
    protected ConnectivityManager cm;

    private int[] menuIgnoreArray = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

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

        if(!isConnected()) {
            showDialog(getResources().getString(R.string.error_no_internet_title), getResources().getString(R.string.error_no_internet), getResources().getString(android.R.string.ok), "");
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

    @Nullable
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

    @Nullable
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
        makeHTTPRequest(url, params, true);
    }

    protected void makeHTTPRequest(String url, HashMap<String, String> params, OnRequestFinishedListener postExecute) {
        makeHTTPRequest(url, params, postExecute, true);
    }

    protected void makeHTTPRequest(String url, HashMap<String, String> params, boolean handleNoInternet) {
        new HTTPRequestor(url, params, handleNoInternet).execute();
    }

    protected void makeHTTPRequest(String url, HashMap<String, String> params, OnRequestFinishedListener postExecute, boolean handleNoInternet) {
        new HTTPRequestor(url, params, postExecute, handleNoInternet).execute();
    }

    protected ErrorCode getErrorCode(int code) {
        switch(code) {
            case 1:
                return ErrorCode.INVALID_LOGIN;
            case 2:
                return ErrorCode.MYSQL_ERROR;
            case 3:
                return ErrorCode.ACTION_ALREADY_PERFORMED;
            case 4:
                return ErrorCode.MISSING_PERMISSION;
            case 5:
                return ErrorCode.MISSING_PARAMETER;
            case 6:
                return ErrorCode.INVALID_PARAMETER;
            case 7:
                return ErrorCode.NO_ROWS_RETURNED;
            default:
                return null;
        }
    }

    protected void onNoConnection() {
        Log.w("baron-online.eu", "No internet!");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_no_internet), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isConnected() {
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private int getConnectionType() {
        return cm.getActiveNetworkInfo().getType();
    }

    protected enum ErrorCode {
        INVALID_LOGIN, MYSQL_ERROR, ACTION_ALREADY_PERFORMED, MISSING_PERMISSION, MISSING_PARAMETER, INVALID_PARAMETER, NO_ROWS_RETURNED
    }

    protected void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message)
                .setTitle(title);

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    protected void showDialog(String title, String message, String positiveButtonText, String negativeButtonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(positiveButtonText, null)
                .setNegativeButton(negativeButtonText, null);

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    protected void showDialog(String title, String message, String positiveButtonText, DialogInterface.OnClickListener positiveButtonListener, String negativeButtonText, DialogInterface.OnClickListener negativeButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(positiveButtonText, positiveButtonListener)
                .setNegativeButton(negativeButtonText, negativeButtonListener);

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    protected void showDialog(String title, String message, DialogInterface.OnClickListener positiveButtonListener, DialogInterface.OnClickListener negativeButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton(getResources().getString(android.R.string.ok), positiveButtonListener)
                .setNegativeButton(getResources().getString(android.R.string.cancel), negativeButtonListener);

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    interface OnRequestFinishedListener {
        void onRequestFinished(JSONObject object);
    }

    private class HTTPRequestor extends AsyncTask<String, String, String> {
        private JSONObject result;
        private String url;
        private HashMap<String, String> params;
        private OnRequestFinishedListener listener = null;
        private boolean handleNoInternet;

        public HTTPRequestor(String url, HashMap<String, String> params, boolean handleNoInternet) {
            this.url = url;
            this.params = params;
            this.handleNoInternet = handleNoInternet;
        }

        public HTTPRequestor(String url, HashMap<String, String> params, OnRequestFinishedListener listener, boolean handleNoInternet) {
            this.url = url;
            this.params = params;
            this.listener = listener;
            this.handleNoInternet = handleNoInternet;
        }

        @Override
        protected String doInBackground(String... strParams) {
            result = JSONParser.makeHttpRequest(url, "GET", params);

            return null;
        }

        protected void onPostExecute(String str) {
            if(listener != null && (result != null || !handleNoInternet)) {
                listener.onRequestFinished(result);
            } else if(result == null && handleNoInternet) {
                onNoConnection();
            }
        }
    }
}















