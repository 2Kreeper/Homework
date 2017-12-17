package eu.baron_online.homework;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import eu.baron_online.homework.exception.MySQLException;

public class HomeworkEntryDetailActivity extends ToolbarActivity {

    private TextView media, page, numbers, until;
    private Button done, flag;
    private int showID;

    private String subjectStr, mediaStr, pageStr, numbersStr, untilStr, uploadName;
    private int uploadID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_entry_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.loading));
        //remove unwanted options
        int[] ignoreArray = {R.id.action_search};
        DataInterchange.addValue("actionbar_ignore", ignoreArray);

        Intent startIntent = getIntent();
        showID = startIntent.getIntExtra("id", 1);

        if(startIntent.getBooleanExtra("notification", false)) {
            tryKillNotification(showID); //if opened via notification, kill the notification
        }

        media = (TextView) findViewById(R.id.homeworkMedia);
        page = (TextView) findViewById(R.id.homeworkPage);
        numbers = (TextView) findViewById(R.id.homeworkUser);
        until = (TextView) findViewById(R.id.homeworkUntil);

        //mark entry as done
        setLoading(true);
        HashMap<String, String> params = new HashMap<>();
        params.put("user", (String) DataInterchange.getValue("username"));
        params.put("pass", (String) DataInterchange.getValue("password"));
        params.put("id", Integer.toString(showID));
        makeHTTPRequest("http://baron-online.eu/services/homework_get_entry.php", params, new OnRequestFinishedListener() {
            @Override
            public void onRequestFinished(JSONObject object) {
                setEntry(object);
            }
        });

        done = (Button) findViewById(R.id.homeworkDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);

                HashMap<String, String> params = new HashMap<>();
                params.put("user", (String) DataInterchange.getValue("username"));
                params.put("pass", (String) DataInterchange.getValue("password"));
                params.put("homework_id", Integer.toString(showID));
                makeHTTPRequest("http://baron-online.eu/services/homework_entry_done.php", params, new OnRequestFinishedListener() {
                    @Override
                    public void onRequestFinished(JSONObject object) {
                        try {
                            if(object.getInt("success") == 1) {
                                onEntryMarked();
                            } else {
                                onEntryMarkFailed();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        flag = (Button) findViewById(R.id.homeworkFlag);
        flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeworkEntryDetailActivity.instance, FlagActivity.class);
                intent.putExtra("id", showID);
                intent.putExtra("subject", subjectStr);
                intent.putExtra("media", mediaStr);
                intent.putExtra("page", pageStr);
                intent.putExtra("numbers", numbersStr);
                intent.putExtra("until", untilStr);
                intent.putExtra("upload_id", uploadID);
                intent.putExtra("upload_name", uploadName);

                startActivity(intent);
            }
        });
    }

    public void setEntry(JSONObject result) {
        try {
            if(result.getInt("success") == 1) {

                JSONObject entryData = result.getJSONObject("entry_data");

                String untilStr = changeDateFormat(entryData.getString("UNTIL"), getResources().getString(R.string.server_date_format), getResources().getString(R.string.local_date_format));

                //displaying results
                setToolbarTitle(entryData.getString("SUBJECT"));
                media.setText(entryData.getString("MEDIA"));
                page.setText(String.format(getResources().getString(R.string.page_placeholder), entryData.getString("PAGE")));
                numbers.setText(String.format(getResources().getString(R.string.numbers_placeholder), entryData.getString("NUMBERS")));
                until.setText(String.format(getResources().getString(R.string.until_placeholder), untilStr));

                //setting variables
                subjectStr = entryData.getString("SUBJECT");
                mediaStr = entryData.getString("MEDIA");
                pageStr = entryData.getString("PAGE");
                numbersStr = entryData.getString("NUMBERS");
                this.untilStr = entryData.getString("UNTIL");
                uploadID = entryData.getJSONObject("uploader_data").getInt("ID");
                uploadName = entryData.getJSONObject("uploader_data").getString("USERNAME");
            } else {
                switch(getErrorCode(result.getInt("error_code"))) {
                    case MYSQL_ERROR:
                        ToolbarActivity.instance.runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                Toast.makeText(ToolbarActivity.instance, getResources().getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                            }
                        });
                        FirebaseCrash.report(new MySQLException(result.getString("error")));
                        break;
                    case NO_ROWS_RETURNED:
                        ToolbarActivity.instance.runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                Toast.makeText(ToolbarActivity.instance, getResources().getString(R.string.error_internal), Toast.LENGTH_SHORT).show();
                            }
                        });
                        FirebaseCrash.report(new MySQLException("Zero rows returned!"));
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            //show toast in UI thread
            ToolbarActivity.instance.runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    Toast.makeText(ToolbarActivity.instance, getResources().getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
        setLoading(false);
    }

    public void onEntryMarked() {
        setLoading(false);

        finish();
    }
    public void onEntryMarkFailed() {
        Context context = getApplicationContext();
        CharSequence text = "An error occured!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        setLoading(false);
    }

    @Override
    protected void onNoConnection() {
        super.onNoConnection();
        setLoading(false);
        finish();
    }

    private void tryKillNotification(int id) {
        if(notificationExists(id)) {
            mNotificationManager.cancel(id);
        }
    }
}
