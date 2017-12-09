package eu.baron_online.homework;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class HomeworkListActivity extends ToolbarActivity {

    public static HomeworkListActivity instance;

    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayList<Integer> listItemsIDs = new ArrayList<>();
    private ArrayList<Boolean> listItemsOutdated = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    private View actionbarView;
    private ListView lView;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton filterButton, sortButton;

    private String[] subjects = null;
    private boolean filtered = false;
    private String filterType = "", filterParam = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HomeworkListActivity.instance = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //remove unwanted options
        int[] ignoreArray = {R.id.action_search};
        DataInterchange.addValue("actionbar_ignore", ignoreArray);

        actionbarView  = getWindow().getDecorView().findViewById(R.id.toolbar);

        lView = (ListView) findViewById(android.R.id.list);
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(HomeworkListActivity.instance, HomeworkEntryDetailActivity.class);
                intent.putExtra("id", listItemsIDs.get(position));
                startActivity(intent);
            }
        });

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);

                boolean outdated = listItemsOutdated.get(position);
                if(outdated) {
                    textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWarning));
                    textView.setTypeface(null, Typeface.BOLD);
                }

                return textView;
            }
        };

        lView.setAdapter(adapter);

        //refreshing in onResume()

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

        filterButton = (ImageButton) findViewById(R.id.filterButton);
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(HomeworkListActivity.this, findViewById(R.id.filterButton));
                menu.inflate(R.menu.filter_chooser_popup);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.filter_subject:
                                showFilterSubjectPopup();
                                return true;

                            case R.id.filter_date:
                                showFilterDatePopup();
                                return true;
                        }

                        return false;
                    }
                });

                menu.show();
            }
        });

        sortButton = (ImageButton) findViewById(R.id.sortButton);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(HomeworkListActivity.this, findViewById(R.id.sortButton));
                menu.inflate(R.menu.sort_chooser_popup);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()) {
                            case R.id.sort_date_asc:
                                DataInterchange.addPersistent("sort_column", "UNTIL");
                                DataInterchange.addPersistent("sort_mode", "ASC");
                                HomeworkListActivity.this.updateList();

                                return true;
                            case R.id.sort_date_desc:
                                DataInterchange.addPersistent("sort_column", "UNTIL");
                                DataInterchange.addPersistent("sort_mode", "DESC");
                                HomeworkListActivity.this.updateList();

                                return true;

                            case R.id.sort_subject_asc:
                                DataInterchange.addPersistent("sort_column", "SUBJECT");
                                DataInterchange.addPersistent("sort_mode", "ASC");
                                HomeworkListActivity.this.updateList();

                                return true;
                            case R.id.sort_subject_desc:
                                DataInterchange.addPersistent("sort_column", "SUBJECT");
                                DataInterchange.addPersistent("sort_mode", "DESC");
                                HomeworkListActivity.this.updateList();

                                return true;
                        }

                        return false;
                    }
                });

                menu.show();
            }
        });

        //set default sorting values (if necessary)
        if(!DataInterchange.existsPersistent("sort_column")) {
            Log.v("baron-online.eu", "Adding value for \"sort_column\"");
            DataInterchange.addPersistent("sort_column", "UNTIL");
        }

        if(!DataInterchange.existsPersistent("sort_mode")) {
            Log.v("baron-online.eu", "Adding value for \"sort_mode\"");
            DataInterchange.addPersistent("sort_mode", "ASC");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateList();
    }

    private void showFilterSubjectPopup() {
        this.setLoading(true);
        //make an http request, popup is displayed in result handling (setSubjects(JSONObject result))
        new RequestSubjects().execute();
    }

    private void showFilterDatePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_filter_date, null);

        final EditText from = (EditText) view.findViewById(R.id.filter_date_from);
        final EditText until = (EditText) view.findViewById(R.id.filter_date_until);

        from.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            private DatePickerDialog dialog;
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    long time = new Date().getTime();
                    int year = Integer.parseInt(DateFormat.format("yyyy", time).toString());
                    int month = Integer.parseInt(DateFormat.format("MM", time).toString());
                    int day = Integer.parseInt(DateFormat.format("dd", time).toString());

                    dialog = new DatePickerDialog(HomeworkListActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker picker, int year, int month, int dayOfMonth) {
                            try {
                                String yearStr = String.valueOf(year);
                                String monthStr = String.valueOf(month);
                                String dayStr = String.valueOf(dayOfMonth);

                                String untilString = yearStr + "-" + monthStr + "-" + dayStr;
                                Log.v("baron-online.eu", untilString);
                                SimpleDateFormat serverFmt = new SimpleDateFormat(getResources().getString(R.string.server_date_format));
                                Date date = serverFmt.parse(untilString);

                                SimpleDateFormat userFmt = new SimpleDateFormat(getResources().getString(R.string.local_date_format));
                                untilString = userFmt.format(date);

                                from.setText(untilString);

                                //clear focus
                                from.setFocusable(false);
                                until.setFocusable(false);
                                from.setFocusable(true);
                                until.setFocusable(true);
                            } catch(ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }, year, month - 1, day);
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                from.clearFocus();
                            }
                        }
                    });
                    dialog.show();
                }
            }
        });

        until.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    long time = new Date().getTime();
                    int year = Integer.parseInt(DateFormat.format("yyyy", time).toString());
                    int month = Integer.parseInt(DateFormat.format("MM", time).toString());
                    int day = Integer.parseInt(DateFormat.format("dd", time).toString());

                    DatePickerDialog dialog = new DatePickerDialog(HomeworkListActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker picker, int year, int month, int dayOfMonth) {
                            try {
                                String yearStr = String.valueOf(year);
                                String monthStr = String.valueOf(month);
                                String dayStr = String.valueOf(dayOfMonth);

                                String untilString = yearStr + "-" + monthStr + "-" + dayStr;
                                Log.v("baron-online.eu", untilString);
                                SimpleDateFormat serverFmt = new SimpleDateFormat(getResources().getString(R.string.server_date_format));
                                Date date = serverFmt.parse(untilString);

                                SimpleDateFormat userFmt = new SimpleDateFormat(getResources().getString(R.string.local_date_format));
                                untilString = userFmt.format(date);

                                until.setText(untilString);

                                //clear focus
                                from.setFocusable(false);
                                until.setFocusable(false);
                                from.setFocusable(true);
                                until.setFocusable(true);
                            } catch(ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }, year, month - 1, day);
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_NEGATIVE) {
                                until.clearFocus();
                            }
                        }
                    });
                    dialog.show();
                }
            }
        });

        builder.setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    protected void updateList() {
        clearList();
        if(filtered) {
            new RequestEntrys(filterType, filterParam).execute();
        } else {
            new RequestEntrys().execute();
        }
    }

    public void addItem(String title, int id, boolean outdated) {
        listItems.add(title);
        listItemsIDs.add(id);
        listItemsOutdated.add(outdated);
        adapter.notifyDataSetChanged();
    }

    private void clearList() {
        listItems.clear();
        listItemsIDs.clear();
        listItemsOutdated.clear();

        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    public void setEntrys(JSONObject result) {
        try {
            JSONArray entrys = result.getJSONArray("entrys");

            for(int i = 0; i < entrys.length(); i++) {
                JSONObject entry = (JSONObject) entrys.get(i);

                this.addItem(entry.getString("SUBJECT"), entry.getInt("ENTRY_ID"), entry.getInt("OUTDATED") == 1);
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "A JSONError occurred! Please contact the developer.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
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

        swipeRefreshLayout.setRefreshing(false);
    }

    public void setSubjects(JSONObject result) {
        try {
            JSONArray subjects = result.getJSONArray("subjects");
            String[] subs = new String[subjects.length() + 1];

            for(int i = 0; i < subjects.length(); i++) {
                JSONObject subject = (JSONObject) subjects.get(i);

                subs[i] = subject.getString("TITLE");
            }
            subs[subjects.length()] = getResources().getString(R.string.filter_subject_all);

            this.subjects = subs;

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(getResources().getString(R.string.filter_subject_selection)).setItems(this.subjects, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dia, int which) {
                    AlertDialog dialog = (AlertDialog) dia;

                    String selectedSubject = (String) dialog.getListView().getAdapter().getItem(which);

                    if(selectedSubject.equals(getResources().getString(R.string.filter_subject_all))) {
                        HomeworkListActivity.this.filtered = false;
                        HomeworkListActivity.this.filterType = "";
                        HomeworkListActivity.this.filterParam = "";
                        HomeworkListActivity.this.updateList();
                        return;
                    }

                    HomeworkListActivity.this.filtered = true;
                    HomeworkListActivity.this.filterType = "subject";
                    HomeworkListActivity.this.filterParam = selectedSubject;
                    HomeworkListActivity.this.updateList();
                }
            }).create().show();
        } catch (JSONException e) {
            e.printStackTrace();
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

        this.setLoading(false);
    }

    public int[] getIdArray() {
        return arrayListToArray(listItemsIDs);
    }

    public class RequestEntrys extends AsyncTask<String, String, String> {

        private JSONObject result;

        private boolean filtered;
        private String filterType = "", filterParam = "";

        public RequestEntrys() {
            super();

            filtered = false;
        }

        public RequestEntrys(String filterType, String filterParam) {
            super();

            filtered = true;
            this.filterType = filterType;
            this.filterParam = filterParam;
        }

        @Override
        protected String doInBackground(String... params) {
            /*List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));
            jsonParams.add(new BasicNameValuePair("sort_col", (String) DataInterchange.getPersistentString("sort_column")));
            jsonParams.add(new BasicNameValuePair("sort_md", (String) DataInterchange.getPersistentString("sort_mode")));

            if(filtered) {
                jsonParams.add(new BasicNameValuePair("filter", "true"));
                jsonParams.add(new BasicNameValuePair(filterType, filterParam));
            }*/

            HashMap<String, String> jsonParams = new HashMap<>();
            jsonParams.put("user", (String) DataInterchange.getValue("username"));
            jsonParams.put("pass", (String) DataInterchange.getValue("password"));
            jsonParams.put("sort_col", (String) DataInterchange.getPersistentString("sort_column"));
            jsonParams.put("sort_md", (String) DataInterchange.getPersistentString("sort_mode"));

            if(filtered) {
                jsonParams.put("filter", "true");
                jsonParams.put(filterType, filterParam);
            }

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_get_all.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String res) {
            HomeworkListActivity.instance.setEntrys(result);
        }
    }

    public class RequestSubjects extends AsyncTask<String, String, String> {

        private JSONObject result;

        @Override
        protected String doInBackground(String... params) {
            /*List<NameValuePair> jsonParams = new ArrayList<>();
            jsonParams.add(new BasicNameValuePair("user", (String) DataInterchange.getValue("username")));
            jsonParams.add(new BasicNameValuePair("pass", (String) DataInterchange.getValue("password")));*/

            HashMap<String, String> jsonParams = new HashMap<>();
            jsonParams.put("user", (String) DataInterchange.getValue("username"));
            jsonParams.put("pass", (String) DataInterchange.getValue("password"));

            result = JSONParser.makeHttpRequest("http://baron-online.eu/services/homework_get_subjects.php", "GET", jsonParams);

            return null;
        }

        protected void onPostExecute(String res) {
            HomeworkListActivity.instance.setSubjects(result);
        }
    }
}
