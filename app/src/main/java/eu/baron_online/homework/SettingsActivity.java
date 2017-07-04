package eu.baron_online.homework;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;

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
        getSupportActionBar().setTitle(getResources().getString(R.string.settings_toolbar));

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Integer> extras = new HashMap<>();
                extras.put("id", 1);

                sendNotification("Test", "This is a test.\nIf you click here, you will see the entry with ID 1", HomeworkEntryDetailActivity.class, extras, R.drawable.ic_stat_name, 0);
            }
        });
    }
}
