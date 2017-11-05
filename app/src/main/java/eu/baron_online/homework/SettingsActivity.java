package eu.baron_online.homework;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.SeekBar;

public class SettingsActivity extends ToolbarActivity {

    public static SettingsActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SettingsActivity.instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //set actionbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.settings_toolbar));
        //remove unwanted options
        int[] ignoreArray = {R.id.action_search, R.id.action_settings};
        DataInterchange.addValue("actionbar_ignore", ignoreArray);

        updateViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateViews();
    }

    private void updateViews() {
        SeekBar refreshRateSeekBar = (SeekBar) findViewById(R.id.refreshRateSeekBar);
        if(DataInterchange.getPersistentInt("homeworkCheckerSleepTime") != 0) {
            refreshRateSeekBar.setProgress(DataInterchange.getPersistentInt("homeworkCheckerSleepTime") / 1000 - 1); //undo multiplication while saving to display user-friendly value
        }
        refreshRateSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DataInterchange.addPersistent("homeworkCheckerSleepTime",
                        (1000 * (progress + 1)) //multiplying by 1000 to get a useful sleep time. Plus one because sleep time zero (1000 * 0) is WAY to high
                );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
