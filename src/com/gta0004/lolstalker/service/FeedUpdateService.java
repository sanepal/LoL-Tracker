package com.gta0004.lolstalker.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.gta0004.lolstalker.MainActivity;
import com.gta0004.lolstalker.R;
import com.gta0004.lolstalker.db.DatabaseAccessor;
import com.gta0004.lolstalker.events.IEvent;
import com.gta0004.lolstalker.listeners.IPlayerActivityListener;
import com.gta0004.lolstalker.listeners.LastGameListener;
import com.gta0004.lolstalker.listeners.OnPlayerActivityListenerFinished;
import com.gta0004.lolstalker.riot.Summoner;
import com.gta0004.lolstalker.utils.Constants;

public class FeedUpdateService extends Service {

  private static final String TAG = "FeedUpdateService";
  private boolean onWiFi;
  private static FeedUpdateService instance = null;
  private static ArrayList<IEvent> events = new ArrayList<IEvent>();
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
      //Log.i(TAG, "Running Listener");
      //if reached end of list, cancel the task so that the scheduler stops running
      if (currIndex == listeners.size()) {
        //Log.i(TAG, "Cancelling run");
        currIndex = 0;
        listenerUpdateHandle.cancel(true);
        return;
      }				
      //run listener and get updates
      final IPlayerActivityListener listener = listeners.get(currIndex++);
      listener.setCallback(new OnPlayerActivityListenerFinished() {

        @Override
        public void onFinish() {
          if (listener.stateChanged()) {
            //Log.i(TAG, "Listener state was changed.");
            //if state was changed, send the message back to the main activity
            IEvent event = listener.getEvent();
            notifyActivity(event);
            if (!mPrefs.getBoolean("isInForeground", true))
              sendNotification(event);
            events.add(event);
          } else {
            //Log.i(TAG, "No change to listener.");
          }           
        }   
        
      });
      listener.run();      
    }
  };
  private ScheduledFuture<?> listUpdateHandle = null;
  private ScheduledFuture<?> listenerUpdateHandle = null;
  private DatabaseAccessor dbA;
  private List<Summoner> listOfSummoners;
  private List<IPlayerActivityListener> listeners;
  private int currIndex = 0;
  private SharedPreferences mPrefs;


  int mStartMode = Service.START_STICKY;       // indicates how to behave if the service is killed
  boolean mAllowRebind = false; // indicates whether onRebind should be used

  @Override
  public void onCreate() {
    // The service is being created   
    instance = this;
    mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    dbA = new DatabaseAccessor(this);
    listOfSummoners = dbA.getAllSummoners();
    listeners = new ArrayList<IPlayerActivityListener>();
    for (Summoner summoner : listOfSummoners) {
      listeners.add(new LastGameListener(summoner, getApplicationContext()));
    }
    registerReceiver(new BroadcastReceiver() {      
      
      @Override
      public void onReceive(Context context, Intent intent) {
        if (listUpdateHandle != null) { 
          ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
          boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
          if (isConnected) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;   
            if (isWiFi && !onWiFi) {
              Log.i(TAG, "Switching to wifi from network");
              onWiFi = true;
              listUpdateHandle.cancel(true);
              listUpdateHandle = listUpdateScheduler.scheduleWithFixedDelay(updateList, 0, 5, TimeUnit.MINUTES);
            }
            else if (!isWiFi && onWiFi) {
              Log.i(TAG, "Switching to network from wifi");
              onWiFi = false;
              listUpdateHandle.cancel(true);
              long delay = listUpdateHandle.getDelay(TimeUnit.MINUTES);
              listUpdateHandle = listUpdateScheduler.scheduleWithFixedDelay(updateList, delay, 30, TimeUnit.MINUTES);
            }
          }
        }
      }

    }, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    //Log.i(TAG, "onCreate complete");
  }
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {      
    if (intent != null && intent.getAction().equals(Constants.ACTION_START_SERVICE)) {
      //run cumulative update on the whole list
      Log.i(TAG, "Running with initial intent");
      ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);   
      NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
      boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
      if (isConnected) {
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        if (isWiFi) {
          Log.i(TAG, "Starting service on wifi");
          listUpdateHandle = listUpdateScheduler.scheduleWithFixedDelay(updateList, 0, 5, TimeUnit.MINUTES);
          onWiFi = true;
        }
        else {
          Log.i(TAG, "Starting service on network");
          listUpdateHandle = listUpdateScheduler.scheduleWithFixedDelay(updateList, 0, 30, TimeUnit.MINUTES);
          onWiFi = false;
        }
      }
      
    }
    else if (intent != null && intent.getAction().equals(Constants.ACTION_NEW_SUMMONER)) {
      //get summoner details from intent and add to list
      Log.i(TAG, "Adding new summoner in service");
      Summoner summoner = intent.getParcelableExtra(Constants.EXTRA_SUMMONER);
      listOfSummoners.add(summoner);
      listeners.add(new LastGameListener(summoner, getApplicationContext()));
    }
    else if (intent != null && intent.getAction().equals(Constants.ACTION_REQUEST_FEED)) {
      Log.i(TAG, "Processing request for updated list");
      sendEventsList();
    }
    //Log.i(TAG, "onStartCommand complete");
    return mStartMode;
  }
  
  private void sendEventsList() {
    //Log.i(TAG, "Sending list to MainActivity");
    Intent intent = new Intent(Constants.ACTION_UPDATED_FEED);
    intent.putParcelableArrayListExtra(Constants.EXTRA_EVENTS, events);
    LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);    
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
    instance = null;
    listUpdateHandle.cancel(true);
    listUpdateScheduler.shutdown();
  }
  
  private void notifyActivity(IEvent event) {
    Intent intent = new Intent(Constants.ACTION_NEW_FEED);
    intent.putExtra(Constants.EXTRA_EVENT,event);
    LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
  }

  private void sendNotification(IEvent event) {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(getApplication())
    .setSmallIcon(com.gta0004.lolstalker.R.drawable.ic_launcher)
    .setContentTitle(getResources().getString(R.string.app_name))
    .setContentText(event.getMessage())
    .setAutoCancel(true);
    // Creates an explicit intent for an Activity in your app
    Intent resultIntent = new Intent(getApplication(), MainActivity.class);

    // The stack builder object will contain an artificial back stack for the
    // started Activity.
    // This ensures that navigating backward from the Activity leads out of
    // your application to the Home screen.
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplication());
    // Adds the back stack for the Intent (but not the Intent itself)
    stackBuilder.addParentStack(MainActivity.class);
    // Adds the Intent that starts the Activity to the top of the stack
    stackBuilder.addNextIntent(resultIntent);
    PendingIntent resultPendingIntent =
        stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT
            );
    mBuilder.setContentIntent(resultPendingIntent);
    NotificationManager mNotificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    // mId allows you to update the notification later on.
    mNotificationManager.notify(1, mBuilder.build());

  }
  
  public static boolean isInstanceCreated() { 
    return instance != null; 
  }
}