package eu.baron_online.homework;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterActivity2 extends ToolbarActivity {
    private AlertDialog dialog;
    private TableLayout userCoursesTable;
    private Button newCourseButton, registerButton;
    private ArrayList<String[]> tableContent = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //mark actionbar to be empty
        DataInterchange.addValue("emptyToolbar", true);
        //set actionbar
        setSupportActionBar(toolbar);
        setToolbarTitle(getResources().getString(R.string.register_toolbar));
        //remove unwanted options
        int[] ignoreArray = {R.id.action_logout, R.id.action_search, R.id.action_settings};
        DataInterchange.addValue("actionbar_ignore", ignoreArray);

        userCoursesTable = (TableLayout) findViewById(R.id.userCoursesTable);

        newCourseButton = (Button) findViewById(R.id.newCourseButton);
        newCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewCoursePopup();
            }
        });

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tableContent.size() > 0) {
                    new RegisterUser().execute();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "You have to enter at least one course!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showNewCoursePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_new_course, null))
                // Add action buttons
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dia, int id) {
                        AlertDialog dialog = RegisterActivity2.this.dialog;
                        String subjectText = ((EditText) dialog.findViewById(R.id.courseText)).getText().toString(), teacherText = ((EditText) dialog.findViewById(R.id.teacherText)).getText().toString();
                        if(!subjectText.equals("") && !teacherText.equals("")) {
                            addTableEntry(subjectText, teacherText);
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Please enter valid inputs", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            showNewCoursePopup();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RegisterActivity2.this.dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        RegisterActivity2.this.dialog = dialog;
    }

    private void addTableEntry(String subjectString, String teacherString) {
        final TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);

        android.widget.TableRow.LayoutParams trparams = new TableRow.LayoutParams(android.widget.TableRow.LayoutParams.WRAP_CONTENT, android.widget.TableRow.LayoutParams.WRAP_CONTENT);

        TextView subjectText = new TextView(this);
        subjectText.setText(subjectString);
        subjectText.setTextSize(24);
        trparams.setMarginStart(10);
        subjectText.setLayoutParams(trparams);

        TextView teacherText = new TextView(this);
        teacherText.setText(teacherString);
        teacherText.setTextSize(24);
        trparams.setMargins(0, 0, 0, 0);
        trparams.setMarginStart(100);
        teacherText.setLayoutParams(trparams);

        ImageButton removeButton = new ImageButton(this);
        removeButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_name, getTheme()));
        removeButton.setBackground(null);
        removeButton.setLayoutParams(trparams);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userCoursesTable.removeView(row);
            }
        });

        row.addView(subjectText);
        row.addView(teacherText);
        row.addView(removeButton);

        userCoursesTable.addView(row, userCoursesTable.getChildCount());
        String[] content = {subjectString, teacherString};
        tableContent.add(content);
    }

    private void onUserRegister(JSONObject result) {
        try {
            if(result.getInt("success") == 1) {
                DataInterchange.addValue("username", result.getString("USERNAME"));
                DataInterchange.addValue("password", result.getString("PASSWORD"));
                DataInterchange.addValue("class_id", Integer.toString(result.getInt("CLASS_ID")));
                DataInterchange.addValue("school", result.getString("SCHOOL"));
                DataInterchange.addValue("class", result.getString("CLASS"));

                startActivity(new Intent(this, HomeworkListActivity.class));
                LoginActivity.instance.finish();
                finish();
            } else {
                //Toast.makeText(getApplicationContext(), result.getString("message"), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }  catch (NullPointerException e) {
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

    private class RegisterUser extends AsyncTask<String, String, String> {

        private JSONObject result;

        @Override
        protected String doInBackground(String... params) {
            /*List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("usernameText")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("passwordText")));
            jsonParams.add(new BasicNameValuePair("school", (String) DataInterchange.getValue("schoolText")));
            jsonParams.add(new BasicNameValuePair("class", (String) DataInterchange.getValue("classText")));
            jsonParams.add(new BasicNameValuePair("token", (String) DataInterchange.getPersistentString("fcmtoken")));

            for(String[] course : tableContent) {
                jsonParams.add(new BasicNameValuePair("cs_sub[]", course[0]));
                jsonParams.add(new BasicNameValuePair("cs_tcs[]", course[1]));
            }*/

            HashMap<String, String> jsonParams = new HashMap<>();
            jsonParams.put("user", (String) DataInterchange.getValue("usernameText"));
            jsonParams.put("pass", (String) DataInterchange.getValue("passwordText"));
            jsonParams.put("school", (String) DataInterchange.getValue("schoolText"));
            jsonParams.put("class", (String) DataInterchange.getValue("classText"));
            jsonParams.put("token", (String) DataInterchange.getPersistentString("fcmtoken"));

            for(String[] course : tableContent) {
                jsonParams.put("cs_sub[]", course[0]);
                jsonParams.put("cs_tcs[]", course[1]);
            }

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_user_register.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String str) {
            onUserRegister(result);
        }
    }
}
