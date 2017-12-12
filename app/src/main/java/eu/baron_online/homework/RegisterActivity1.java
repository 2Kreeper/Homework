package eu.baron_online.homework;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextWatcher;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterActivity1 extends ToolbarActivity {
    public String usernameText = "", passwordText = "", schoolText = "", classText = "";

    protected TextWatcher watcher;
    protected EditText usernameField, passwordField;
    protected TextView usernameTakenTextview;
    protected AutoCompleteTextView schoolField, classField;
    private Button continueRegisterButton;

    private boolean usernameTaken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //mark actionbar to be empty
        DataInterchange.addValue("emptyToolbar", true);
        //set actionbar
        setSupportActionBar(toolbar);
        setToolbarTitle(getResources().getString(R.string.register_toolbar));
        //remove unwanted options
        int[] ignoreArray = {R.id.action_logout, R.id.action_search, R.id.action_settings};
        DataInterchange.addValue("actionbar_ignore", ignoreArray);

        //set variables
        usernameField = (EditText) findViewById(R.id.registerUsername);
        passwordField = (EditText) findViewById(R.id.registerPassword);
        schoolField = (AutoCompleteTextView) findViewById(R.id.registerSchool);
        classField = (AutoCompleteTextView) findViewById(R.id.registerClass);
        usernameTakenTextview = (TextView) findViewById(R.id.username_taken_textview);

        //init school autocomplete view: make HTTP-Request, results are used in setSchools(JSONObject response)
        new GetSchools().execute();

        //init class autocomplete view: make HTTP-Request, results are used in setClasses(JSONObject response)
        new GetClasses().execute();

        continueRegisterButton = (Button) findViewById(R.id.continueRegisterButton);
        continueRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataInterchange.addValue("usernameText", usernameText);
                DataInterchange.addValue("passwordText", passwordText);
                DataInterchange.addValue("schoolText", schoolText);
                DataInterchange.addValue("classText", classText);
                startActivity(new Intent(RegisterActivity1.instance, RegisterActivity2.class));
            }
        });

        //enable button if all data is given
        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                usernameText = usernameField.getText().toString();
                schoolText = schoolField.getText().toString();
                classText = classField.getText().toString();

                if(!TextUtils.isEmpty(usernameText) && !TextUtils.isEmpty(passwordField.getText().toString()) && !TextUtils.isEmpty(schoolText) && !TextUtils.isEmpty(classText) && !usernameTaken) {
                    continueRegisterButton.setEnabled(true);
                } else {
                    continueRegisterButton.setEnabled(false);
                }

                passwordText = sha256(passwordField.getText().toString());
            }
        };
        usernameField.addTextChangedListener(watcher);
        passwordField.addTextChangedListener(watcher);
        schoolField.addTextChangedListener(watcher);
        classField.addTextChangedListener(watcher);

        //check if username is already taken
        usernameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                new CheckUsername(s.toString()).execute();
            }
        });
    }

    private void setSchools(JSONObject response) {
        try {
            ArrayList<String> schools = new ArrayList<>();
            JSONArray arr = response.getJSONArray("schools");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject school = arr.getJSONObject(i);
                schools.add(school.getString("name"));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, schools);
            schoolField.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setClasses(JSONObject response) {
        try {
            ArrayList<String> classes = new ArrayList<>();
            JSONArray arr = response.getJSONArray("classes");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject school = arr.getJSONObject(i);
                classes.add(school.getString("name"));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, classes);
            classField.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUsernameTaken(JSONObject response) {
        try {
            usernameTaken = response.getInt("success") == 1;
            if(usernameTaken) {
                usernameTakenTextview.setVisibility(TextView.VISIBLE);
            } else {
                usernameTakenTextview.setVisibility(TextView.INVISIBLE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            watcher.afterTextChanged(null);
        }
    }

    class GetSchools extends AsyncTask<String, String, String> {
        private JSONObject result;

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> jsonParams = new HashMap<>();

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_get_schools.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String str) {
            setSchools(result);
        }
    }

    class GetClasses extends AsyncTask<String, String, String> {
        private JSONObject result;

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> jsonParams = new HashMap<>();

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_get_classes.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String str) {
            setClasses(result);
        }
    }

    class CheckUsername extends AsyncTask<String, String, String> {
        private JSONObject result;
        private String username;

        public CheckUsername(String username) {
            this.username = username;
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> jsonParams = new HashMap<>();
            jsonParams.put("user", username);

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_username_exists.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String str) {
            setUsernameTaken(result);
        }
    }
}
