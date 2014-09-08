package com.gta0004.lolstalker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.gta0004.lolstalker.db.DatabaseAccessor;
import com.gta0004.lolstalker.listeners.GameStateListener;
import com.gta0004.lolstalker.listeners.IPlayerActivityListener;
import com.gta0004.lolstalker.riot.SummonerDto;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class FeedUpdateService extends Service {
	
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private Runnable update = new Runnable() {

		@Override
		public void run() {
			Log.e("update runnable", "Running Listener");
			if (currIndex == listeners.size())
				currIndex = 0;
			IPlayerActivityListener listener = listeners.get(currIndex++);
			listener.run();
			if (listener.stateChanged())
				Log.e("update runnable", "Listener state was changed.");
			else
				Log.e("update runnable", "No change to listener.");
			
		}
		
	};
	private ScheduledFuture handle = null;
	private DatabaseAccessor dbA;
	private SharedPreferences mPrefs;
	private List<SummonerDto> listOfSummoners;
	private List<IPlayerActivityListener> listeners;
	private int currIndex = 0;
	

	int mStartMode = Service.START_STICKY;       // indicates how to behave if the service is killed
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind = false; // indicates whether onRebind should be used

    @Override
    public void onCreate() {
        // The service is being created    
    	mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    	dbA = new DatabaseAccessor(this);
    	listOfSummoners = dbA.getAllSummoners();
    	listeners = new ArrayList<IPlayerActivityListener>();
    	for (SummonerDto summoner : listOfSummoners) {
    		listeners.add(new GameStateListener(summoner));
    	}
    	Log.e("FeedUpdateService", "onCreate complete");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
    	handle = scheduler.scheduleWithFixedDelay(update, 0, 1200, TimeUnit.MILLISECONDS);
    	Log.e("FeedUpdateService", "onStartCommand complete");
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
    	handle.cancel(true);
    }

}