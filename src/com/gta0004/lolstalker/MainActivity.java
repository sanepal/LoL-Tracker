package com.gta0004.lolstalker;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.gta0004.lolstalker.adapters.FeedArrayAdapter;
import com.gta0004.lolstalker.adapters.SummonerArrayAdapter;
import com.gta0004.lolstalker.db.DatabaseAccessor;
import com.gta0004.lolstalker.events.IEvent;
import com.gta0004.lolstalker.riot.Summoner;
import com.gta0004.lolstalker.service.FeedUpdateService;
import com.gta0004.lolstalker.urlrequests.ApiRequestQueue;
import com.gta0004.lolstalker.utils.Constants;
import com.gta0004.lolstalker.utils.Region;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

  /**
   * Fragment managing the behaviors, interactions and presentation of the
   * navigation drawer.
   */
  private static final String TAG = "MainActivity";
  private NavigationDrawerFragment mNavigationDrawerFragment;
  private ArrayList<Summoner> summoners = null;
  private static ArrayAdapter<Summoner> adapterForSummoners = null;
  private ArrayList<IEvent> events = null;
  private static ArrayAdapter<IEvent> adapterForEvents = null;
  private DatabaseAccessor dbA;
  private SharedPreferences mPrefs;
  private static LocalBroadcastManager bManager = null;
  private BroadcastReceiver bReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      //Log.i(TAG, intent.getAction());
      if(intent.getAction().equals(Constants.ACTION_NEW_FEED)) {
        IEvent event = intent.getParcelableExtra(Constants.EXTRA_EVENT);
        addNewEvent(event);
      }  
      else if (intent.getAction().equals(Constants.ACTION_UPDATED_FEED)) {
        ArrayList<IEvent> received = intent.getParcelableArrayListExtra(Constants.EXTRA_EVENTS);
        adapterForEvents.addAll(received);
        adapterForEvents.notifyDataSetChanged();
      }
    }

  };

  /**
   * Used to store the last screen title. For use in {@link #restoreActionBar()}
   * .
   */
  private CharSequence mTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(
        R.id.navigation_drawer);
    mTitle = getTitle();
    // Set up the drawer.
    mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    
    dbA = new DatabaseAccessor(this);   
    
    //if data was saved in the instance state, restore from there
    if (savedInstanceState != null) {
      //Log.i(TAG, "Restoring data from savedInstanceState");
      restoreFromBundle(savedInstanceState);
    }
    else {
      getInitialData();   
      //service is still running, get updated feed from there
      if (FeedUpdateService.isInstanceCreated()) {
        //Log.i(TAG, "Requesting feed from service");
        Intent intent = new Intent(this, FeedUpdateService.class);
        intent.setAction(Constants.ACTION_REQUEST_FEED);
        startService(intent);
      }
    }    
    
    //start service if service is not running
    if (!FeedUpdateService.isInstanceCreated()) {     
      //Log.i(TAG, "Starting intent for the first time");
      Intent intent = new Intent(this, FeedUpdateService.class);
      intent.setAction(Constants.ACTION_START_SERVICE);
      startService(intent);
    }
    
    if (bManager == null) {
      bManager = LocalBroadcastManager.getInstance(this);
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(Constants.ACTION_NEW_FEED);
      intentFilter.addAction(Constants.ACTION_UPDATED_FEED);
      bManager.registerReceiver(bReceiver, intentFilter);
    }    
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    // Save the user's current game state
    savedInstanceState.putParcelableArrayList("SummonerList", summoners);
    savedInstanceState.putParcelableArrayList("EventList", events);

    // Always call the superclass so it can save the view hierarchy state
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  protected void onPause() {
    //activity is not in foreground anymore
    mPrefs.edit().putBoolean("isInForeground", false).commit();
    super.onPause();    
  }

  @Override
  protected void onResume() {
    super.onResume();
    //activity is in foreground
    mPrefs.edit().putBoolean("isInForeground", true).commit();  
  }

  @Override
  public void onNavigationDrawerItemSelected(int position) {
    // update the main content by replacing fragments
    FragmentManager fragmentManager = getFragmentManager();
    fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
    .commit();
  }

  public void onSectionAttached(int number) {
    switch (number) {
    case 1:
      mTitle = getString(R.string.title_section1);
      break;
    case 2:
      mTitle = getString(R.string.title_section2);
      break;
    }
  }

  public void restoreActionBar() {
    ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (!mNavigationDrawerFragment.isDrawerOpen()) {
      // Only show items in the action bar relevant to this screen
      // if the drawer is not showing. Otherwise, let the drawer
      // decide what to show in the action bar.
      getMenuInflater().inflate(R.menu.main, menu);
      restoreActionBar();
      return true;
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    } else if (item.getItemId() == R.id.action_add) {
      DialogFragment dialog = new RequestNameDialog();
      dialog.show(getFragmentManager(), "name");
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void addNewSummoner(String... params) {
    final String name = params[0].replace(" ", "");
    final String region = params[1];
    String url = String.format(Constants.FIND_NAME_URL, region, region, name);
    JsonObjectRequest verifyNameRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

      @Override
      public void onResponse(JSONObject response) {
        Gson gson = new Gson();
        try {
          Summoner summoner = gson.fromJson(response.getString(name.toLowerCase(Locale.ENGLISH)), Summoner.class);
          if (summoner == null) {
            Toast.makeText(getApplicationContext(), NameLookupResponseCode.DOES_NOT_EXIST.getMessage(), Toast.LENGTH_SHORT).show();
            return;
          }
          summoner.region = region;
          dbA.insertNewSummoner(summoner);
          adapterForSummoners.add(summoner);
          adapterForSummoners.notifyDataSetChanged();
          Intent intent = new Intent(getApplication(), FeedUpdateService.class);
          intent.setAction(Constants.ACTION_NEW_SUMMONER);
          intent.putExtra(Constants.EXTRA_SUMMONER, summoner);
          startService(intent);
          Toast.makeText(getApplicationContext(), NameLookupResponseCode.SUCCESS.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (JsonSyntaxException e) {
          Toast.makeText(getApplicationContext(), NameLookupResponseCode.UNKNOWN_ERROR.getMessage(), Toast.LENGTH_SHORT).show();
          e.printStackTrace();
        } catch (JSONException e) {
          Toast.makeText(getApplicationContext(), NameLookupResponseCode.UNKNOWN_ERROR.getMessage(), Toast.LENGTH_SHORT).show();
          e.printStackTrace();
        }
        
      }
    }, new Response.ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError e) {
        Toast.makeText(getApplicationContext(), NameLookupResponseCode.UNKNOWN_ERROR.getMessage(), Toast.LENGTH_SHORT).show();
        // TODO Auto-generated method stub
        
      }
    });
    ApiRequestQueue.getInstance(this).addToRequestQueue(verifyNameRequest);
  }

  private void addNewEvent(IEvent event) {
    adapterForEvents.insert(event, 0);
    adapterForEvents.notifyDataSetChanged();
  }

  private void getInitialData() {
    events = new ArrayList<IEvent>();
    summoners = dbA.getAllSummoners();

    adapterForSummoners = new SummonerArrayAdapter(this, summoners);
    adapterForEvents = new FeedArrayAdapter(this, events);
  }

  private void restoreFromBundle(Bundle savedInstanceState) {
    summoners = savedInstanceState.getParcelableArrayList("SummonerList");
    events = savedInstanceState.getParcelableArrayList("EventList");

    adapterForSummoners = new SummonerArrayAdapter(this, summoners);
    adapterForEvents = new FeedArrayAdapter(this, events);
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  public static class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_SECTION_NUMBER, sectionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);
      ListView listView = (ListView) rootView.findViewById(R.id.section_list);
      if (this.getArguments().get(ARG_SECTION_NUMBER).equals(1)) {
        listView.setAdapter(adapterForSummoners);
      } else if (this.getArguments().get(ARG_SECTION_NUMBER).equals(2)) {
        listView.setAdapter(adapterForEvents);
      }

      return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
      super.onAttach(activity);
      ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }
  }

  private static enum NameLookupResponseCode {
    SUCCESS("Summoner found"), 
    BAD_SUMMONER_NAME("That summoner does not exist or has not played any games this season"), 
    DOES_NOT_EXIST( "That summoner name is not registered"), 
    NO_GAMES("That summoner has not played any games this season"),
    UNKNOWN_ERROR("Unknown error");
    private String message;

    private NameLookupResponseCode(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }

  public class RequestNameDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      LayoutInflater inflater =(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
      View view = inflater.inflate(R.layout.request_name_dialog, null); 
      final EditText input = (EditText) view.findViewById(R.id.name);
      final Spinner region = (Spinner) view.findViewById(R.id.region);
      region.setAdapter(new ArrayAdapter<Region>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, Region.values()));
      region.setSelection(Region.NA.ordinal());
      builder.setTitle("Enter Summoner Name");
      builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.cancel();
          Region selected = (Region) region.getSelectedItem();      
          addNewSummoner(input.getText().toString(), selected.getRegionCode());
        }
      });
      builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          dialog.cancel();
        }

      });
      builder.setView(view);
      return builder.create();
    }
  }

}
