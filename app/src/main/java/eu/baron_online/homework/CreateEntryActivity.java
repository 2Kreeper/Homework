package eu.baron_online.homework;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateEntryActivity extends ToolbarActivity {

    public static CreateEntryActivity instance;

    private EditText subject, media, page, numbers, until;
    protected String subjectStr, mediaStr, pageStr, numbersStr, untilStr;
    protected TextWatcher watcher;
    private Button create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CreateEntryActivity.instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entry);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.create_entry));
        //remove unwanted options
        int[] ignoreArray = {R.id.action_search};
        DataInterchange.addValue("actionbar_ignore", ignoreArray);

        create = (Button) findViewById(R.id.createEntrySubmit);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);
                new CreateEntry().execute();
            }
        });

        subject = (EditText) findViewById(R.id.createEntrySubject);
        media = (EditText) findViewById(R.id.createEntryMedia);
        page = (EditText) findViewById(R.id.createEntryPage);
        numbers = (EditText) findViewById(R.id.createEntryNumbers);
        until = (EditText) findViewById(R.id.createEntryUntil);

        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!TextUtils.isEmpty(subject.getText().toString()) && !TextUtils.isEmpty(media.getText().toString()) && !TextUtils.isEmpty(numbers.getText().toString()) && !TextUtils.isEmpty(until.getText().toString())) {
                    //every three EditTexts (page is excluded: worksheet) have a valid value
                    create.setEnabled(true);
                } else {
                    create.setEnabled(false);
                }

                //update string values
                subjectStr = subject.getText().toString();
                mediaStr = media.getText().toString();
                numbersStr = numbers.getText().toString();
                untilStr = until.getText().toString();

                if(TextUtils.isEmpty(page.getText().toString())) {
                    pageStr = "";
                } else {
                    pageStr = page.getText().toString();
                }
            }
        };
        subject.addTextChangedListener(watcher);
        media.addTextChangedListener(watcher);
        page.addTextChangedListener(watcher);
        numbers.addTextChangedListener(watcher);
        until.addTextChangedListener(watcher);

        until.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    long time = new Date().getTime();
                    int year = Integer.parseInt(DateFormat.format("yyyy", time).toString());
                    int month = Integer.parseInt(DateFormat.format("MM", time).toString());
                    int day = Integer.parseInt(DateFormat.format("dd", time).toString());

                    Log.v("baron-online.eu", year + "-" + month + "-" + day);

                    /*DatePickerDialog dialog = new DatePickerDialog(CreateEntryActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker picker, int year, int month, int dayOfMonth) {
                            String yearStr = String.valueOf(year);
                            String monthStr = String.valueOf(month);
                            String dayStr = String.valueOf(dayOfMonth);

                            if(monthStr.length() == 1) {
                                monthStr = "0" + monthStr;
                            }
                            if(dayStr.length() == 1) {
                                dayStr = "0" + dayStr;
                            }

                            until.setText(yearStr + "-" + monthStr + "-" + dayStr);
                            until.clearFocus();

                            //manually call TextChangeEvent
                            watcher.afterTextChanged(new SpannableStringBuilder());
                        }
                    }, year, month, day);*/
                    DatePickerDialog dialog = new DatePickerDialog(CreateEntryActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker picker, int yearSelected, int monthSelected, int dayOfMonthSelected) {
                            try {
                                String yearStr = String.valueOf(yearSelected);
                                String monthStr = String.valueOf(monthSelected);
                                String dayStr = String.valueOf(dayOfMonthSelected);


                                String untilString = yearStr + "-" + monthStr + "-" + dayStr;
                                Log.v("baron-online.eu", untilString);
                                SimpleDateFormat serverFmt = new SimpleDateFormat(getResources().getString(R.string.server_date_format));
                                Date date = serverFmt.parse(untilString);

                                SimpleDateFormat userFmt = new SimpleDateFormat(getResources().getString(R.string.local_date_format));
                                untilString = userFmt.format(date);

                                until.setText(untilString);
                                until.clearFocus();

                                //manually call TextChangeEvent
                                watcher.afterTextChanged(new SpannableStringBuilder());
                            } catch(ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }, year, month - 1, day);
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                until.clearFocus();

                            }
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

    private void onEntryCreate(JSONObject result) {
        try {
            if(result.getInt("success") == 1) {
                HomeworkListActivity.instance.updateList();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "An error occured:\n" + result.getString("message"), Toast.LENGTH_LONG);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setLoading(false);
    }

    class CreateEntry extends AsyncTask<String, String, String> {

        private JSONObject result;

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> jsonParams = new ArrayList<NameValuePair>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));
            jsonParams.add(new BasicNameValuePair("subject", subjectStr));
            jsonParams.add(new BasicNameValuePair("media", mediaStr));
            jsonParams.add(new BasicNameValuePair("page", pageStr));
            jsonParams.add(new BasicNameValuePair("numbers", numbersStr));
            jsonParams.add(new BasicNameValuePair("until", untilStr));
            jsonParams.add(new BasicNameValuePair("class_id", (String) DataInterchange.getValue("class_id")));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_entry_create.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String str) {
            CreateEntryActivity.instance.onEntryCreate(result);
        }
    }
}
