package eu.baron_online.homework;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoginActivity extends ToolbarActivity {

    public static LoginActivity instance;

    private EditText username, password;
    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LoginActivity.instance = this;

        super.onCreate(savedInstanceState);

        Log.d("baron-online.eu", "FCM Token: " + DataInterchange.getPersistentString("fcmtoken"));

        //check if user is already logged in
        if(DataInterchange.existsPersistent("username") && DataInterchange.existsPersistent("password")) {
            new CheckForUser().execute(DataInterchange.getPersistentString("username"), DataInterchange.getPersistentString("password"));
        }

        setContentView(R.layout.activity_login);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false);
        //remove unwanted options
        int[] ignoreArray = {R.id.action_logout, R.id.action_search, R.id.action_settings};
        DataInterchange.addValue("actionbar_ignore", ignoreArray);

        username = (EditText) findViewById(R.id.loginUsernameInput);
        password = (EditText) findViewById(R.id.loginPasswordInput);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(username.getText().toString()) || TextUtils.isEmpty(password.getText().toString())) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please enter a username and a password!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    setLoading(true);
                    new CheckForUser().execute(username.getText().toString(), sha256(password.getText().toString()));
                }
            }
        });

        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.instance, RegisterActivity1.class));
            }
        });
    }

    public void onRequestFinished(JSONObject result) {
        try {
            boolean userExists = result.getInt("success") == 1;

            if (userExists) {
                JSONObject userInfo = result.getJSONObject("user_info");

                DataInterchange.addValue("username", userInfo.getString("USERNAME"));
                DataInterchange.addValue("password", userInfo.getString("PASSWORD"));
                DataInterchange.addValue("class_id", Integer.toString(userInfo.getInt("CLASS_ID")));
                DataInterchange.addValue("school", userInfo.getString("SCHOOL"));
                DataInterchange.addValue("class", userInfo.getString("CLASS"));

                DataInterchange.addPersistent("username", userInfo.getString("USERNAME"));
                DataInterchange.addPersistent("password", userInfo.getString("PASSWORD"));

                new UpdateUserToken().execute(userInfo.getString("USERNAME"), userInfo.getString("PASSWORD"));

                //start activity and kill old one
                Intent intent = new Intent(this, HomeworkListActivity.class);
                startActivity(intent);
                finish();
            } else {
                password.setText("");

                Context context = getApplicationContext();
                CharSequence text = "This data is invalid!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

    class CheckForUser extends AsyncTask<String, String, String> {

        private JSONObject result;

        @Override
        protected String doInBackground(String... params) {
            String username = params[0], password = params[1];

            /*List<NameValuePair> jsonParams = new ArrayList<NameValuePair>();
            jsonParams.add(new BasicNameValuePair("user", username));
            jsonParams.add(new BasicNameValuePair("pass", password));*/

            HashMap<String, String> jsonParams = new HashMap<>();
            jsonParams.put("user", username);
            jsonParams.put("pass", password);

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_user_exists.php", "GET", jsonParams);

            return "";
        }

        @Override
        protected void onPostExecute(String str) {
            LoginActivity.instance.onRequestFinished(result);
        }
    }

    class UpdateUserToken extends AsyncTask<String, String, String>{
        private JSONObject result;

        @Override
        protected String doInBackground(String... params) {
            String username = params[0], password = params[1];

            /*List<NameValuePair> jsonParams = new ArrayList<NameValuePair>();
            jsonParams.add(new BasicNameValuePair("user", username));
            jsonParams.add(new BasicNameValuePair("pass", password));
            jsonParams.add(new BasicNameValuePair("new_token", FirebaseInstanceId.getInstance().getToken()));*/

            HashMap<String, String> jsonParams = new HashMap<>();
            jsonParams.put("user", username);
            jsonParams.put("pass", password);
            jsonParams.put("new_token", FirebaseInstanceId.getInstance().getToken());

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_user_update_token.php", "GET", jsonParams);

            return "";
        }
    }
}