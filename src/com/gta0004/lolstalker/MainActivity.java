package com.gta0004.lolstalker;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gta0004.lolstalker.Riot.LastMatch;
import com.gta0004.lolstalker.Riot.SummonerDto;
import com.gta0004.lolstalker.Service.FeedUpdateService;
import com.gta0004.lolstalker.Utils.Constants;
import com.gta0004.lolstalker.db.DatabaseAccessor;

public class MainActivity extends Activity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;
	private List<SummonerDto> listOfSummoners = null;
	private static ArrayAdapter<SummonerDto> adapterForSummoners = null;
	
	private List<String> feed = null;
	private static ArrayAdapter<String> feedAdapter = null;
	
	private DatabaseAccessor dbA;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		dbA = new DatabaseAccessor(this);
		getInitialSummoners();
		getInititalFeed();
		Intent intent = new Intent(this, FeedUpdateService.class);
		startService(intent);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager
				.beginTransaction()
				.replace(R.id.container,
						PlaceholderFragment.newInstance(position + 1)).commit();
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
		}
		else if (item.getItemId() == R.id.action_add) {
			DialogFragment dialog = new RequestNameDialog();
			dialog.show(getFragmentManager(), "name");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}	
	
	private void addNewSummoner(SummonerDto summoner) {
		dbA.insertNewSummoner(summoner);
		adapterForSummoners.add(summoner);
		adapterForSummoners.notifyDataSetChanged();
	}

	private void getInitialSummoners() {
		listOfSummoners = dbA.getAllSummoners();
		
		adapterForSummoners = new ArrayAdapter<SummonerDto>(this, android.R.layout.simple_list_item_1, listOfSummoners);
		
	}
	
	private void addToFeed(SummonerDto summoner) {
		feedAdapter.add("Last match was " + summoner.lastMatch.matchId + " that resulted in a " + summoner.lastMatch.winner + " with " + summoner.lastMatch.pentakills + " pentakills.");
		feedAdapter.notifyDataSetChanged();
	}
	
	private void getInititalFeed() {
		feed = new ArrayList<String>();
		for (SummonerDto summoner : listOfSummoners) {
			feed.add("Last Match was " + summoner.lastMatch.matchId);
		}
		feedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, feed);
	}
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
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
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			ListView listView = (ListView) rootView.findViewById(R.id.section_list);
			if (this.getArguments().get(ARG_SECTION_NUMBER).equals(1)) {
				listView.setAdapter(adapterForSummoners);
			}
			else if (this.getArguments().get(ARG_SECTION_NUMBER).equals(2)) {
				listView.setAdapter(feedAdapter);
			}
			
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(
					ARG_SECTION_NUMBER));
		}
	}
	
	public class VerifyNameTask extends AsyncTask<String, Void, SummonerDto> {

		@Override
		protected SummonerDto doInBackground(String... params) {
			String name = params[0];
			name = name.replace(" ", "");
			try {
				HttpParams httpParameters = new BasicHttpParams();
			    int timeoutConnection = 3000;
			    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			    int timeoutSocket = 5000;
			    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
				HttpClient httpclient = new DefaultHttpClient(httpParameters);

		        URI website = new URI("https://na.api.pvp.net/api/lol/na/v1.4/summoner/by-name/"+name+"?"+Constants.KEY_PARAM);
		        Log.e("log_tag", website.toString());
		        HttpGet request = new HttpGet();
		        request.setURI(website);
		        HttpResponse response = httpclient.execute(request);
		        Log.e("log_tag", "Get summoner response "+response.getStatusLine().getStatusCode());
		        
		        String jsonStr = EntityUtils.toString(response.getEntity());		        
		        JsonParser parser = new JsonParser();
				JsonObject jsonObj = parser.parse(jsonStr).getAsJsonObject();				
				Gson gson = new Gson();
		        SummonerDto summoner = new SummonerDto();
		        summoner = gson.fromJson(jsonObj.get(name).getAsJsonObject(), SummonerDto.class);
		        
		        website = new URI("https://na.api.pvp.net/api/lol/na/v2.2/matchhistory/"+summoner.id+"?beginIndex=0&endIndex=1&"+Constants.KEY_PARAM);
		        request.setURI(website);
		        response = httpclient.execute(request);
		        Log.e("log_tag", "Last Match response "+response.getStatusLine().getStatusCode());
		        jsonStr = EntityUtils.toString(response.getEntity());
		        jsonObj = parser.parse(jsonStr).getAsJsonObject();
		        jsonObj = jsonObj.get("matches").getAsJsonArray().get(0).getAsJsonObject();
		        LastMatch lastMatch = new LastMatch();
		        lastMatch.matchId = jsonObj.get("matchId").getAsLong();
		        lastMatch.queueType = jsonObj.get("queueType").getAsString();
		        jsonObj = jsonObj.get("participants").getAsJsonArray().get(0).getAsJsonObject();
		        jsonObj = jsonObj.get("stats").getAsJsonObject();
		        lastMatch.winner = jsonObj.get("winner").getAsBoolean();
		        lastMatch.pentakills = jsonObj.get("pentaKills").getAsInt();
		        summoner.lastMatch = lastMatch;
		        
		        return summoner;
				
			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection " + e.toString());
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public void onPostExecute(SummonerDto summoner) {
			Toast.makeText(getApplication(), "Summoner found", Toast.LENGTH_SHORT).show();
	        //Log.e("log_tag", summoner.toString());
			addNewSummoner(summoner);
			addToFeed(summoner);
		}
		
	}
	
	public class RequestNameDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final EditText input = new EditText(getActivity());  
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
			                        LinearLayout.LayoutParams.MATCH_PARENT,
			                        LinearLayout.LayoutParams.MATCH_PARENT);
			input.setLayoutParams(lp);
			input.setLines(1);
			builder.setTitle("Enter Summoner Name");
			builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					AsyncTask<String, ?, ?> task = new VerifyNameTask();
					task.execute(input.getText().toString());					
				}
			});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();					
				}
				
			});
			builder.setView(input);
			return builder.create();
		}
	}

}