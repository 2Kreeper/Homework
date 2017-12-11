package eu.baron_online.homework;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import eu.baron_online.homework.exception.LoginException;
import eu.baron_online.homework.exception.MySQLException;

public class FlagActivity extends ToolbarActivity {

    private Button flagButton;
    private TextView media, until, upload;
    private Spinner flagReasonSpinner;
    private int flagID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //set actionbar
        setSupportActionBar(toolbar);
        //remove unwanted options
        DataInterchange.addValue("actionbar_ignore", new int[0]);

        Intent startIntent = getIntent();
        flagID = startIntent.getIntExtra("id", 0);

        flagReasonSpinner = (Spinner) findViewById(R.id.homeworkFlagReason);
        new GetFlagReasons().execute();

        media = (TextView) findViewById(R.id.homeworkMedia);
        until = (TextView) findViewById(R.id.homeworkUntil);
        upload = (TextView) findViewById(R.id.homeworkUser);
        flagButton = (Button) findViewById(R.id.flag_button);

        String mediaStr = startIntent.getStringExtra("media"),
               untilStr = startIntent.getStringExtra("until"),
               uploadName = startIntent.getStringExtra("upload_name");
        int uploadInt = startIntent.getIntExtra("upload_id", 0);

        setToolbarTitle(startIntent.getStringExtra("subject"));
        media.setText(mediaStr);
        until.setText(untilStr);
        upload.setText(uploadName);

        flagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FlagEntry().execute();
            }
        });
    }

    protected void setFlagReasonSpinnerItems(JSONObject result) {
        ArrayList<String> reasons = new ArrayList<>();
        try {
            JSONArray items = result.getJSONArray("flag_reasons");
            for(int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);

                reasons.add(flagIdToLocalizedTitle(item.getInt("ID")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reasons);
        flagReasonSpinner.setAdapter(adapter);
    }

    private String flagIdToLocalizedTitle(int id) {
        return getResources().getStringArray(R.array.flag_reasons)[id - 1];
    }

    private int localizedTitleToFlagId(String title) {
        int i = findIndex(getResources().getStringArray(R.array.flag_reasons), title);
        if(i != 0) {
            return i + 1;
        }

        return 0;
    }

    protected void onFlagFailed(int errorCode, JSONObject object) {
        switch(errorCode) {
            case 1:
                ToolbarActivity.instance.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(ToolbarActivity.instance, ToolbarActivity.instance.getResources().getString(R.string.account_data_wrong), Toast.LENGTH_LONG).show();
                    }
                });
                FirebaseCrash.report(new LoginException("Username: \"" + DataInterchange.getPersistentString("username") + "\" Password: \"" + DataInterchange.getPersistentString("password") + "\""));
                logout();
                break;
            case 2:
                ToolbarActivity.instance.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(ToolbarActivity.instance, ToolbarActivity.instance.getResources().getString(R.string.error_internal), Toast.LENGTH_LONG).show();
                    }
                });
                try {
                    FirebaseCrash.report(new MySQLException(object.getString("message")));
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }
                break;
            case 3:
                ToolbarActivity.instance.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(ToolbarActivity.instance, ToolbarActivity.instance.getResources().getString(R.string.error_already_flagged), Toast.LENGTH_LONG).show();
                    }
                });
                finish();
                break;
        }
    }

    class FlagEntry extends AsyncTask<String, String, String> {

        JSONObject result;

        @Override
        protected String doInBackground(String... strings) {
            HashMap<String, String> jsonParams = new HashMap<>();
            jsonParams.put("user", (String) DataInterchange.getValue("username"));
            jsonParams.put("pass", (String) DataInterchange.getValue("password"));
            jsonParams.put("homework_id", Integer.toString(flagID));
            jsonParams.put("flag_reason", Integer.toString(localizedTitleToFlagId(flagReasonSpinner.getSelectedItem().toString())));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_entry_flag.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String str) {
            try {
                if(result.getInt("success") == 1) {
                    finish();
                } else {
                    onFlagFailed(result.getInt("error_code"), result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class GetFlagReasons extends AsyncTask<String, String, String> {

        private JSONObject result;

        @Override
        protected String doInBackground(String... params) {
            /*List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));
            jsonParams.add(new BasicNameValuePair("homework_id", Integer.toString(homeworkID)));*/

            HashMap<String, String> jsonParams = new HashMap<>();
            jsonParams.put("user", (String) DataInterchange.getValue("username"));
            jsonParams.put("pass", (String) DataInterchange.getValue("password"));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_get_flag_reasons.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String str) {
            try {
                if(result.getInt("success") == 1) {
                    setFlagReasonSpinnerItems(result);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
