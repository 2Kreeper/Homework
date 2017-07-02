package eu.baron_online.homework;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


public class ToolbarActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(DataInterchange.containsKey("hideSettings") && (Boolean) DataInterchange.getValue("hideSettings")) {
            getMenuInflater().inflate(R.menu.menu_main_no_settings, menu);
        } else if(DataInterchange.containsKey("emptyToolbar") && (Boolean) DataInterchange.getValue("emptyToolbar")) {
            getMenuInflater().inflate(R.menu.menu_empty, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }


        //reset falgs
        DataInterchange.addValue("hideSettings", false);
        DataInterchange.addValue("emptyToolbar", false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            case R.id.action_logout:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    protected void setToolbarTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }
}
