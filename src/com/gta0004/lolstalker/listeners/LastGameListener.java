package com.gta0004.lolstalker.listeners;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gta0004.lolstalker.events.IEvent;
import com.gta0004.lolstalker.events.LastGameEvent;
import com.gta0004.lolstalker.riot.LastMatch;
import com.gta0004.lolstalker.riot.Summoner;
import com.gta0004.lolstalker.utils.Constants;

public class LastGameListener extends AbstractPlayerActivityListener {
  
  private static final String TAG = "LastGameListener";
  private long lastMatchId;
  private boolean stateChanged = false;
  private Summoner summoner;
  private static HttpClient httpclient;
  private URI website;

  static {
    HttpParams httpParameters = new BasicHttpParams();
    int timeoutConnection = 3000;
    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
    int timeoutSocket = 5000;
    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
    httpclient = new DefaultHttpClient(httpParameters);
  }

  public LastGameListener(Summoner summoner) {
    this.summoner = summoner;
    if (this.summoner.lastMatch != null) {
      this.lastMatchId = this.summoner.lastMatch.matchId;
    }
    try {
      website = new URI("https://na.api.pvp.net/api/lol/na/v2.2/matchhistory/" + summoner.id
          + "?beginIndex=0&endIndex=1&" + Constants.KEY_PARAM);
    } catch (Exception e) {
      Log.e(TAG, "could not create URI for summoner: " + summoner.name, e);
      // probably have some re-try logic or fallback logic or kill the
      // application here
    }
  }

  @Override
  public void run() {
    HttpGet request = new HttpGet();
    HttpResponse response = null;
    JsonParser parser = new JsonParser();
    JsonObject jsonObj = null;
    try {
      request.setURI(website);
      response = httpclient.execute(request);
      Log.i(TAG, "Last Match response " + response.getStatusLine().getStatusCode());
      String jsonStr = EntityUtils.toString(response.getEntity());
      jsonObj = parser.parse(jsonStr).getAsJsonObject();
      jsonObj = jsonObj.get("matches").getAsJsonArray().get(0).getAsJsonObject();
      long mostRecentMatchId = jsonObj.get("matchId").getAsLong();
      Log.i(TAG, "Last Match ID: " + lastMatchId + " Most Recent Match ID: "
          + mostRecentMatchId);
      stateChanged = mostRecentMatchId != lastMatchId;
      if (stateChanged) {
        lastMatchId = mostRecentMatchId;
        LastMatch newMatch = new LastMatch();
        newMatch.matchId = lastMatchId;
        newMatch.matchCreation = jsonObj.get("matchCreation").getAsLong();
        newMatch.matchDuration = jsonObj.get("matchDuration").getAsLong();
        jsonObj = jsonObj.get("participants").getAsJsonArray().get(0).getAsJsonObject();
        newMatch.champId = jsonObj.get("championId").getAsInt();
        jsonObj = jsonObj.get("stats").getAsJsonObject();
        newMatch.winner = jsonObj.get("winner").getAsBoolean();
        newMatch.pentakills = jsonObj.get("pentaKills").getAsInt();
        summoner.lastMatch = newMatch;
      }
    } catch (Exception e) {
      Log.e(TAG, "Error in http connection " + e.toString());
      e.printStackTrace();
    }
  }

  @Override
  public boolean stateChanged() {
    return stateChanged;
  }

  @Override
  public String getMessage() {
    return summoner.name + " just finished a game!";
  }
  
  @Override
  public IEvent getEvent() {
    return new LastGameEvent(summoner);
  }
}
