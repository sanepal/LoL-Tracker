package com.gta0004.lolstalker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.gta0004.lolstalker.MainActivity;
import com.gta0004.lolstalker.db.DatabaseAccessor;
import com.gta0004.lolstalker.listeners.LastGameListener;
import com.gta0004.lolstalker.listeners.IPlayerActivityListener;
import com.gta0004.lolstalker.riot.Summoner;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class FeedUpdateService extends Service {
	
  private static final String TAG = "FeedUpdateService";
  IBinder mBinder;      // interface for clients that bind
	private ScheduledExecutorService listUpdateScheduler = Executors.newScheduledThreadPool(1);
	private ScheduledExecutorService listenerRunScheduler = Executors.newScheduledThreadPool(1);
	
	private Runnable updateList = new Runnable() {
    @Override
    public void run() {
      //runs an update on each listener with a 1.2s delay
      listenerRunScheduler.scheduleWithFixedDelay(runListener, 0, 1200, TimeUnit.MILLISECONDS); 
    }
	  
	};
	
	private Runnable runListener = new Runnable() {
		@Override
		public void run() {
			Log.i(TAG, "Running Listener");
			//if reached end of list, cancel the task so that the scheduler stops running
			if (currIndex == listeners.size()) {
			  Log.i(TAG, "Cancelling run");
			  currIndex = 0;
        listenerUpdateHandle.cancel(true);
			  return;
			}				
			//run listener and get updates
			IPlayerActivityListener listener = listeners.get(currIndex++);
			listener.run();
			if (listener.stateChanged()) {
			  Log.i(listener.getClass().getSimpleName(), "Listener state was changed.");
			  //if state was changed, send the message back to the main activity
			  //TODO if activity is in bg, send notification as well
			  Intent intent = new Intent(MainActivity.NEW_FEED);
			  intent.putExtra("Message", listener.getMessage());
			  LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
			} else {
				Log.i(listener.getClass().getSimpleName(), "No change to listener.");
			}	
		}
		
	};
	private ScheduledFuture listUpdateHandle = null;
	private ScheduledFuture listenerUpdateHandle = null;
	private DatabaseAccessor dbA;
	private List<Summoner> listOfSummoners;
	private List<IPlayerActivityListener> listeners;
	private int currIndex = 0;
	

	int mStartMode = Service.START_STICKY;       // indicates how to behave if the service is killed
  boolean mAllowRebind = false; // indicates whether onRebind should be used

    @Override
    public void onCreate() {
      // The service is being created    
    	dbA = new DatabaseAccessor(this);
    	listOfSummoners = dbA.getAllSummoners();
    	listeners = new ArrayList<IPlayerActivityListener>();
    	for (Summoner summoner : listOfSummoners) {
    		listeners.add(new LastGameListener(summoner));
    	}
    	Log.i(TAG, "onCreate complete");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {      
      if (intent.getAction().equals("Initial")) {
        //run cumulative update on the whole list
        Log.i(TAG, "Running with initital intent");
        listUpdateHandle = listUpdateScheduler.scheduleWithFixedDelay(updateList, 0, 30, TimeUnit.SECONDS);
      }
      else if (intent.getAction().equals("NewSummoner")) {
        //get summoner details from intent and add to list
        Log.i(TAG, "Adding new summoner in service");
        Summoner summoner = intent.getParcelableExtra("summoner");
        listOfSummoners.add(summoner);
        listeners.add(new LastGameListener(summoner));
      }
    	Log.i(TAG, "onStartCommand complete");
      return mStartMode;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    	listUpdateHandle.cancel(true);
    	listUpdateScheduler.shutdown();
    }
}