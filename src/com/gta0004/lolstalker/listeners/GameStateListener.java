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
import com.gta0004.lolstalker.riot.SummonerDto;
import com.gta0004.lolstalker.utils.Constants;

public class GameStateListener extends AbstractPlayerActivityListener {

  private long lastMatchId;
  private boolean stateChanged = false;
  private SummonerDto summoner;
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

  public GameStateListener(SummonerDto summoner) {
    this.lastMatchId = summoner.lastMatch.matchId;
    this.summoner = summoner;
    try {
      website = new URI("https://na.api.pvp.net/api/lol/na/v2.2/matchhistory/" + summoner.id
          + "?beginIndex=0&endIndex=1&" + Constants.KEY_PARAM);
    } catch (Exception e) {
      Log.e("GSL", "could not create URI for summoner: " + summoner.name, e);
      // probably have some re-try logic or fallback logic or kill the
      // application here
    }
  }

  @Override
  public void run() {
    try {
      HttpGet request = new HttpGet();
      request.setURI(website);
      HttpResponse response = httpclient.execute(request);
      Log.i("log_tag", "Last Match response " + response.getStatusLine().getStatusCode());
      String jsonStr = EntityUtils.toString(response.getEntity());
      JsonParser parser = new JsonParser();
      JsonObject jsonObj = parser.parse(jsonStr).getAsJsonObject();
      jsonObj = jsonObj.get("matches").getAsJsonArray().get(0).getAsJsonObject();
      long mostRecentMatchId = jsonObj.get("matchId").getAsLong();
      Log.i("GameStateListener", "Last Match ID: " + lastMatchId + " Most Recent Match ID: "
          + mostRecentMatchId);
      stateChanged = mostRecentMatchId != lastMatchId;
    } catch (Exception e) {
      Log.e("GameStateListener", "Error in http connection " + e.toString());
      e.printStackTrace();
    }
  }

  @Override
  public boolean stateChanged() {
    return stateChanged;
  }

  @Override
  public String getMessage() {
    return summoner.name + " has started a game";
  }
}
