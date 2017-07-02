package eu.baron_online.homework;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeworkListActivity extends ToolbarActivity {

    public static HomeworkListActivity instance;

    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayList<Integer> listItemsIDs = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    private ListView lView;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HomeworkListActivity.instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lView = (ListView) findViewById(android.R.id.list);
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HomeworkListActivity.instance, HomeworkEntryDetailActivity.class);
                intent.putExtra("id", listItemsIDs.get(position));
                startActivity(intent);
            }
        });

        username = (String) DataInterchange.getValue("username");
        password = (String) DataInterchange.getValue("password");

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        lView.setAdapter(adapter);

        new RequestEntrys().execute();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeworkListActivity.instance, CreateEntryActivity.class);
                startActivity(intent);
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList();
            }
        });
    }

    protected void updateList() {
        clearList();
        new RequestEntrys().execute();
    }

    public void addItems(View v, String title, int id) {
        listItems.add(title);
        listItemsIDs.add(id);
        adapter.notifyDataSetChanged();
    }

    private void clearList() {
        listItems.clear();
        listItemsIDs.clear();

        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    public void setEntrys(JSONObject result) {
        try {
            JSONArray entrys = result.getJSONArray("entrys");

            for(int i = 0; i < entrys.length(); i++) {
                JSONObject entry = (JSONObject) entrys.get(i);

                this.addItems(lView, entry.getString("SUBJECT"), entry.getInt("ID"));
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "An error occured! Please contact the developer.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    class RequestEntrys extends AsyncTask<String, String, String> {

        private JSONObject result;

        @Override
        protected String doInBackground(String... params) {
            List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_get_all.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String res) {
            HomeworkListActivity.instance.setEntrys(result);
        }
    }
}
