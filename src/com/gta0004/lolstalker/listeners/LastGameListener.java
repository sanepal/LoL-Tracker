package com.gta0004.lolstalker.listeners;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.gta0004.lolstalker.events.IEvent;
import com.gta0004.lolstalker.events.LastGameEvent;
import com.gta0004.lolstalker.riot.LastMatch;
import com.gta0004.lolstalker.riot.Summoner;
import com.gta0004.lolstalker.urlrequests.ApiRequestQueue;
import com.gta0004.lolstalker.utils.Constants;

public class LastGameListener extends AbstractPlayerActivityListener {
  
  private static final String TAG = "LastGameListener";
  private long lastMatchId;
  private boolean stateChanged = false;
  private Summoner mSummoner;
  private JsonObjectRequest lastMatchRequest;
  private Context mContext;

  public LastGameListener(Summoner summoner, Context context) {
    this.mSummoner = summoner;
    this.mContext = context;
    if (this.mSummoner.lastMatch != null) {
      this.lastMatchId = this.mSummoner.lastMatch.matchId;
    }
    String url = String.format(Constants.LAST_MATCH_URL, summoner.region, summoner.region, summoner.id);
    lastMatchRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

      @Override
      public void onResponse(JSONObject response) {
        try {
          JSONObject jsonObj = response.getJSONArray("matches").getJSONObject(0);
          long mostRecentMatchId = jsonObj.getLong("matchId");
          Log.i(TAG, "Last Match ID: " + lastMatchId + " Most Recent Match ID: "
              + mostRecentMatchId);
          stateChanged = mostRecentMatchId != lastMatchId;
          if (stateChanged) {
            lastMatchId = mostRecentMatchId;
            LastMatch newMatch = new LastMatch();
            newMatch.matchId = lastMatchId;
            newMatch.matchCreation = jsonObj.getLong("matchCreation");
            newMatch.matchDuration = jsonObj.getLong("matchDuration");
            jsonObj = jsonObj.getJSONArray("participants").getJSONObject(0);
            newMatch.champId = jsonObj.getInt("championId");
            jsonObj = jsonObj.getJSONObject("stats");
            newMatch.winner = jsonObj.getBoolean("winner");
            newMatch.pentakills = jsonObj.getInt("pentaKills");
            mSummoner.lastMatch = newMatch;
          }
        } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }        
      }
      
    }, new Response.ErrorListener() {
  
        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
          Log.i(TAG, "Unsuccessful request");
          Log.i(TAG, error.getLocalizedMessage());
          stateChanged = false;
        }
    });
  }

  @Override
  public void run() {
    ApiRequestQueue.getInstance(mContext).addToRequestQueue(lastMatchRequest);
  }

  @Override
  public boolean stateChanged() {
    return stateChanged;
  }

  @Override
  public String getMessage() {
    return mSummoner.name + " just finished a game!";
  }
  
  @Override
  public IEvent getEvent() {
    return new LastGameEvent(mSummoner);
  }
}
