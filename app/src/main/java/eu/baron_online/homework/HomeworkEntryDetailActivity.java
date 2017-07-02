package eu.baron_online.homework;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class HomeworkEntryDetailActivity extends ToolbarActivity {

    public static HomeworkEntryDetailActivity instance;

    private TextView subject, media, page, numbers, until;
    private Button done, flag;
    private int showID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HomeworkEntryDetailActivity.instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_entry_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent startIntent = getIntent();
        showID = startIntent.getIntExtra("id", 1);

        media = (TextView) findViewById(R.id.homeworkMedia);
        page = (TextView) findViewById(R.id.homeworkPage);
        numbers = (TextView) findViewById(R.id.homeworkNumbers);
        until = (TextView) findViewById(R.id.homeworkUntil);

        new RequestEntry(showID).execute();

        done = (Button) findViewById(R.id.homeworkDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EntryDone(showID).execute();
            }
        });

        flag = (Button) findViewById(R.id.homeworkFlag);
        flag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "This is not implemented yet!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setEntry(JSONObject result) {
        try {
            //formatting date to user-friendly format
            String untilStr = result.getString("UNTIL");
            String yearStr = untilStr.substring(0, 4);
            String monthStr = untilStr.substring(5, 7);
            String dayStr = untilStr.substring(8);

            Date d = new Date(new GregorianCalendar(Integer.parseInt(yearStr), Integer.parseInt(monthStr), Integer.parseInt(dayStr)).getTimeInMillis());
            untilStr = android.text.format.DateFormat.format("dd.MM.yyyy", d).toString();

            //displaying results
            getSupportActionBar().setTitle(result.getString("SUBJECT"));
            media.setText(result.getString("MEDIA"));
            page.setText("Page(s) " + result.getString("PAGE"));
            numbers.setText("Number(s) " + result.getString("NUMBERS"));
            until.setText("Until " + untilStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onEntryFlagged() {
        finish();
        HomeworkListActivity.instance.updateList();
    }
    public void onEntryFlagFailed() {
        Context context = getApplicationContext();
        CharSequence text = "An error occured!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    class RequestEntry extends AsyncTask<String, String, String> {

        private JSONObject result;
        private int id;

        public RequestEntry(int id) {
            this.id = id;
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));
            jsonParams.add(new BasicNameValuePair("id", Integer.toString(this.id)));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_get_entry.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String res) {
            HomeworkEntryDetailActivity.instance.setEntry(result);
        }
    }

    class EntryDone extends AsyncTask<String, String, String> {

        private int homeworkID;
        private JSONObject result;

        public EntryDone(int homeworkID) {
            this.homeworkID = homeworkID;
        }

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));
            jsonParams.add(new BasicNameValuePair("homework_id", Integer.toString(homeworkID)));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_entry_done.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String str) {
            try {
                if(result.getInt("success") == 1) {
                    HomeworkEntryDetailActivity.instance.onEntryFlagged();
                } else {
                    HomeworkEntryDetailActivity.instance.onEntryFlagFailed();
                }
            } catch (JSONException e) {
                HomeworkEntryDetailActivity.instance.onEntryFlagFailed();
                e.printStackTrace();
            }
        }
    }
}
