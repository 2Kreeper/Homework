package eu.baron_online.homework;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends ToolbarActivity implements ToolbarActivity.OnRequestFinishedListener {
    private EditText username, password;
    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("baron-online.eu", "FCM Token: " + DataInterchange.getPersistentString("fcmtoken"));

        //check if user is already logged in
        if(DataInterchange.existsPersistent("username") && DataInterchange.existsPersistent("password")) {
            //new CheckForUser().execute(DataInterchange.getPersistentString("username"), DataInterchange.getPersistentString("password"));

            checkForUser(DataInterchange.getPersistentString("username"), DataInterchange.getPersistentString("password"));
        }

        setContentView(R.layout.activity_login);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //remove unwanted options
        int[] ignoreArray = {R.id.action_logout, R.id.action_search, R.id.action_settings};
        DataInterchange.addValue("actionbar_ignore", ignoreArray);

        username = (EditText) findViewById(R.id.loginUsernameInput);
        password = (EditText) findViewById(R.id.loginPasswordInput);

        TextView.OnEditorActionListener onEditorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    loginButton.callOnClick();
                    return true;
                }

                return false;
            }
        };

        username.setOnEditorActionListener(onEditorActionListener);
        password.setOnEditorActionListener(onEditorActionListener);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(username.getText().toString()) || TextUtils.isEmpty(password.getText().toString())) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.warning_missing_username_or_password), Toast.LENGTH_LONG).show();
                } else {
                    setLoading(true);
                    checkForUser(username.getText().toString(), sha256(password.getText().toString()));
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

    private void checkForUser(String username, String password) {
        HashMap<String, String> params = new HashMap<>();
        params.put("user", username);
        params.put("pass", password);
        makeHTTPRequest("http://baron-online.eu/services/homework_user_exists.php", params, this);
    }

    @Override
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

                //update token
                HashMap<String, String> params = new HashMap<>();
                params.put("user", userInfo.getString("USERNAME"));
                params.put("pass", userInfo.getString("PASSWORD"));
                params.put("new_token", FirebaseInstanceId.getInstance().getToken());
                makeHTTPRequest("http://baron-online.eu/services/homework_user_update_token.php", params);

                //start activity and kill old one
                Intent intent = new Intent(this, HomeworkListActivity.class);
                startActivity(intent);
                finish();
            } else {
                password.setText("");

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.account_data_wrong), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        setLoading(false);
    }

    @Override
    protected void onNoConnection() {
        super.onNoConnection();
        setLoading(false);
    }
}