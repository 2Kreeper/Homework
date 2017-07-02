package eu.baron_online.homework;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends ToolbarActivity {

    public static LoginActivity instance;

    private EditText username, password;
    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LoginActivity.instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //mark actionbar to be empty
        DataInterchange.addValue("emptyToolbar", true);
        //set actionbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.login_toolbar));

        username = (EditText) findViewById(R.id.loginUsernameInput);
        password = (EditText) findViewById(R.id.loginPasswordInput);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(username.getText().toString()) || TextUtils.isEmpty(password.getText().toString())) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please enter a username and a password!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    new CheckForUser().execute(username.getText().toString(), sha256(password.getText().toString()));
                }
            }
        });
        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.instance, RegisterActivity.class));
            }
        });
    }

    public void onRequestFinished(JSONObject result) {
        try {
            boolean userExists = result.getInt("success") == 1;

            if(userExists) {
                DataInterchange.addValue("username", result.getString("USERNAME"));
                DataInterchange.addValue("password", result.getString("PASSWORD"));
                DataInterchange.addValue("class_id", Integer.toString(result.getInt("CLASS_ID")));
                DataInterchange.addValue("school", result.getString("SCHOOL"));
                DataInterchange.addValue("class", result.getString("CLASS"));

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
        }
    }

    class CheckForUser extends AsyncTask<String, String, String> {

        private JSONObject result;

        @Override
        protected String doInBackground(String... params) {
            String username = params[0], password = params[1];

            List<NameValuePair> jsonParams = new ArrayList<NameValuePair>();
            jsonParams.add(new BasicNameValuePair("user", username));
            jsonParams.add(new BasicNameValuePair("pass", password));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_user_exists.php", "GET", jsonParams);

            return "";
        }

        protected void onPostExecute(String str) {
            LoginActivity.instance.onRequestFinished(result);
        }
    }
}
