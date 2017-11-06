package eu.baron_online.homework;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextWatcher;

public class RegisterActivity1 extends ToolbarActivity {

    public static RegisterActivity1 instance;
    public String usernameText = "", passwordText = "", schoolText = "", classText = "";

    protected TextWatcher watcher;
    protected EditText usernameField, passwordField, schoolField, classField;
    private Button continueRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RegisterActivity1.instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //mark actionbar to be empty
        DataInterchange.addValue("emptyToolbar", true);
        //set actionbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.register_toolbar));
        //remove unwanted options
        int[] ignoreArray = {R.id.action_logout, R.id.action_search, R.id.action_settings};
        DataInterchange.addValue("actionbar_ignore", ignoreArray);

        //set variables
        usernameField = (EditText) findViewById(R.id.registerUsername);
        passwordField = (EditText) findViewById(R.id.registerPassword);
        schoolField = (EditText) findViewById(R.id.registerSchool);
        classField = (EditText) findViewById(R.id.registerClass);

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
    }
}
