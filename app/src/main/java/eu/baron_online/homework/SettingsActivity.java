package eu.baron_online.homework;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class SettingsActivity extends ToolbarActivity {

    public static SettingsActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //mark to load "menu_main_no_settings"
        DataInterchange.addValue("hideSettings", true);
        //set actionbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
    }

}
