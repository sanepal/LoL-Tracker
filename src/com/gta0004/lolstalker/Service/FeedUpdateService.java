package com.gta0004.lolstalker.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class FeedUpdateService extends Service {
	
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private Runnable update = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	};
	private ScheduledFuture handle = null;
	private SharedPreferences mPrefs;
	

	int mStartMode = Service.START_STICKY;       // indicates how to behave if the service is killed
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind = false; // indicates whether onRebind should be used

    @Override
    public void onCreate() {
        // The service is being created    
    	mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
    	handle = scheduler.scheduleWithFixedDelay(update, 10, 10, TimeUnit.SECONDS);
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
