package eu.baron_online.homework;

import android.content.DialogInterface;
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

        //get content for spinner
        HashMap<String, String> params = new HashMap<>();
        params.put("user", (String) DataInterchange.getValue("username"));
        params.put("pass", (String) DataInterchange.getValue("password"));
        makeHTTPRequest("http://baron-online.eu/services/homework_get_flag_reasons.php", params, new OnRequestFinishedListener() {
            @Override
            public void onRequestFinished(JSONObject object) {
                setFlagReasonSpinnerItems(object);
            }
        });

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
                showDialog(getResources().getString(R.string.flag_confirmation_title), getResources().getString(R.string.flag_confirmation_message), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //user confirmed flagging process
                        HashMap<String, String> params = new HashMap<>();
                        params.put("user", (String) DataInterchange.getValue("username"));
                        params.put("pass", (String) DataInterchange.getValue("password"));
                        params.put("homework_id", Integer.toString(flagID));
                        params.put("flag_reason", Integer.toString(localizedTitleToFlagId(flagReasonSpinner.getSelectedItem().toString())));

                        makeHTTPRequest("http://baron-online.eu/services/homework_entry_flag.php", params, new OnRequestFinishedListener() {
                            @Override
                            public void onRequestFinished(JSONObject object) {
                                try {
                                    if(object.getInt("success") == 1) {
                                        startActivity(new Intent(getApplicationContext(), HomeworkListActivity.class));
                                        finish();
                                    } else {
                                        onFlagFailed(object.getInt("error_code"), object);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //user canceled flagging
                        finish();
                    }
                });
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
        } catch (JSONException | NullPointerException e) {
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
        if(i != -1) {
            return i + 1;
        }

        return 0;
    }

    protected void onFlagFailed(int errorCode, JSONObject object) {
        switch(getErrorCode(errorCode)) {
            case INVALID_LOGIN:
                ToolbarActivity.instance.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(ToolbarActivity.instance, ToolbarActivity.instance.getResources().getString(R.string.account_data_wrong), Toast.LENGTH_LONG).show();
                    }
                });
                FirebaseCrash.report(new LoginException("Username: \"" + DataInterchange.getPersistentString("username") + "\" Password: \"" + DataInterchange.getPersistentString("password") + "\""));
                logout();
                break;
            case MYSQL_ERROR:
                ToolbarActivity.instance.runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        Toast.makeText(ToolbarActivity.instance, ToolbarActivity.instance.getResources().getString(R.string.error_internal), Toast.LENGTH_LONG).show();
                    }
                });
                try {
                    FirebaseCrash.report(new MySQLException(object.getString("error")));
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }
                break;
            case ACTION_ALREADY_PERFORMED:
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
}
