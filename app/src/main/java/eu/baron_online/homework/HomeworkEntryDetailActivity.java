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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class HomeworkEntryDetailActivity extends ToolbarActivity {

    public static HomeworkEntryDetailActivity instance;

    private TextView media, page, numbers, until;
    private Button done, flag;
    private int showID;

    private String subjectStr, mediaStr, pageStr, numbersStr, untilStr, uploadName;
    private int uploadID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HomeworkEntryDetailActivity.instance = this;

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

        setLoading(true);
        new RequestEntry(showID).execute();

        done = (Button) findViewById(R.id.homeworkDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                new MarkEntryDone(showID).execute();
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
            /*if(result.getInt("success") == 0) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_not_logged_in), Toast.LENGTH_SHORT);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }*/
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
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            //show toast in UI thread
            ToolbarActivity.instance.runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    Toast.makeText(ToolbarActivity.instance, ToolbarActivity.instance.getResources().getString(R.string.error_no_internet), Toast.LENGTH_SHORT).show();
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

    private void tryKillNotification(int id) {
        if(notificationExists(id)) {
            mNotificationManager.cancel(id);
        }
    }

    class RequestEntry extends AsyncTask<String, String, String> {

        private JSONObject result;
        private int id;

        public RequestEntry(int id) {
            this.id = id;
        }

        @Override
        protected String doInBackground(String... params) {
            /*List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));
            jsonParams.add(new BasicNameValuePair("id", Integer.toString(this.id)));*/

            HashMap<String, String> jsonParams = new HashMap<>();
            jsonParams.put("user", (String) DataInterchange.getValue("username"));
            jsonParams.put("pass", (String) DataInterchange.getValue("password"));
            jsonParams.put("id", Integer.toString(this.id));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_get_entry.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String res) {
            HomeworkEntryDetailActivity.instance.setEntry(result);
        }
    }

    class MarkEntryDone extends AsyncTask<String, String, String> {

        private int homeworkID;
        private JSONObject result;

        public MarkEntryDone(int homeworkID) {
            this.homeworkID = homeworkID;
        }

        @Override
        protected String doInBackground(String... params) {
            /*List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));
            jsonParams.add(new BasicNameValuePair("homework_id", Integer.toString(homeworkID)));*/

            HashMap<String, String> jsonParams = new HashMap<>();
            jsonParams.put("user", (String) DataInterchange.getValue("username"));
            jsonParams.put("pass", (String) DataInterchange.getValue("password"));
            jsonParams.put("homework_id", Integer.toString(homeworkID));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_entry_done.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String str) {
            try {
                if(result.getInt("success") == 1) {
                    HomeworkEntryDetailActivity.instance.onEntryMarked();
                } else {
                    HomeworkEntryDetailActivity.instance.onEntryMarkFailed();
                }
            } catch (JSONException e) {
                HomeworkEntryDetailActivity.instance.onEntryMarkFailed();
                e.printStackTrace();
            }
        }
    }
}
