package eu.baron_online.homework;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegisterActivity extends ToolbarActivity {

    public static RegisterActivity instance;

    public String usernameText = "", passwordText = "", schoolText = "", classText = "";

    protected TextWatcher watcher;

    private EditText usernameField, passwordField, schoolField, classField;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RegisterActivity.instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //mark actionbar to be empty
        DataInterchange.addValue("emptyToolbar", true);
        //set actionbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.register_toolbar));

        //set variables
        usernameField = (EditText) findViewById(R.id.registerUsername);
        passwordField = (EditText) findViewById(R.id.registerPassword);
        schoolField = (EditText) findViewById(R.id.registerSchool);
        classField = (EditText) findViewById(R.id.registerClass);

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RegisterUser().execute();
            }
        });

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
                
                if(!TextUtils.isEmpty(usernameText) && !TextUtils.isEmpty(passwordField.getText().toString()) && !TextUtils.isEmpty(schoolText) && !TextUtils.isEmpty(classText)) {
                    registerButton.setEnabled(true);
                } else {
                    registerButton.setEnabled(false);
                }

                passwordText = sha256(passwordField.getText().toString());
            }
        };
        usernameField.addTextChangedListener(watcher);
        passwordField.addTextChangedListener(watcher);
        schoolField.addTextChangedListener(watcher);
        classField.addTextChangedListener(watcher);
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
                Toast.makeText(getApplicationContext(), "Sorry! It seams like the user already exists.\nTry another username.", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class RegisterUser extends AsyncTask<String, String, String> {

        private JSONObject result;
        
        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", usernameText));
            jsonParams.add(new BasicNameValuePair("pass", passwordText));
            jsonParams.add(new BasicNameValuePair("school", schoolText));
            jsonParams.add(new BasicNameValuePair("class", classText));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_user_register.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String str) {
            RegisterActivity.instance.onUserRegister(result);
        }
    }
}
