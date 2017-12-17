package eu.baron_online.homework;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import eu.baron_online.homework.exception.MySQLException;

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
        setToolbarTitle(getResources().getString(R.string.create_entry));
        //remove unwanted options
        int[] ignoreArray = {R.id.action_search};
        DataInterchange.addValue("actionbar_ignore", ignoreArray);

        create = (Button) findViewById(R.id.createEntrySubmit);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoading(true);

                HashMap<String, String> params = new HashMap<>();
                params.put("user", (String) DataInterchange.getValue("username"));
                params.put("pass", (String) DataInterchange.getValue("password"));
                params.put("subject", subjectStr);
                params.put("media", mediaStr);
                params.put("page", pageStr);
                params.put("numbers", numbersStr);
                params.put("until", changeDateFormat(untilStr, getResources().getString(R.string.local_date_format), getResources().getString(R.string.server_date_format)));
                params.put("class_id", (String) DataInterchange.getValue("class_id"));
                makeHTTPRequest("http://baron-online.eu/services/homework_entry_create.php", params, new OnRequestFinishedListener() {
                    @Override
                    public void onRequestFinished(JSONObject object) {
                        onEntryCreate(object);
                    }
                });
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

                    DatePickerDialog dialog = new DatePickerDialog(CreateEntryActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker picker, int yearSelected, int monthSelected, int dayOfMonthSelected) {
                            Log.d("baron-online.eu", yearSelected + " " + monthSelected + " " + dayOfMonthSelected);

                            String yearStr = String.valueOf(yearSelected);
                            String monthStr = String.valueOf(monthSelected + 1);
                            String dayStr = String.valueOf(dayOfMonthSelected);

                            String untilString = changeDateFormat(yearStr + "-" + monthStr + "-" + dayStr, getResources().getString(R.string.server_date_format), getResources().getString(R.string.local_date_format));

                            until.setText(untilString);
                            until.clearFocus();

                            View view = getCurrentFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                            //manually call TextChangeEvent
                            watcher.afterTextChanged(new SpannableStringBuilder());

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
                switch(getErrorCode(result.getInt("error_code"))) {
                    case MYSQL_ERROR: //mysql error
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_internal), Toast.LENGTH_LONG).show();
                        FirebaseCrash.report(new MySQLException(result.getString("message")));
                        finish();
                        break;
                    case MISSING_PERMISSION: //user is not in this course
                        Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.error_not_in_course), result.getJSONObject("request_info").getString("subject")), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setLoading(false);
    }

    @Override
    protected void onNoConnection() {
        super.onNoConnection();
        setLoading(false);
        finish();
    }
}
