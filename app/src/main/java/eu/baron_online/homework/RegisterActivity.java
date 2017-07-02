package eu.baron_online.homework;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Spinner;

public class RegisterActivity extends ToolbarActivity {

    private Spinner numberSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //mark actionbar to be empty
        DataInterchange.addValue("emptyToolbar", true);
        //set actionbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");

        numberSpinner = (Spinner) findViewById(R.id.regis)
    }
}
