package com.gta0004.lolstalker.Listeners;

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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gta0004.lolstalker.Riot.LastMatch;
import com.gta0004.lolstalker.Riot.SummonerDto;
import com.gta0004.lolstalker.Utils.Constants;

public class GameStateListener extends AbstractPlayerActivityListener {

	private long lastMatchId;
	private boolean stateChanged = false;
	private SummonerDto summoner;

	public GameStateListener(SummonerDto summoner) {
		this.lastMatchId = summoner.lastMatch.matchId;
		this.summoner = summoner;
	}

	@Override
	public void run() {
		try {
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);

			URI website = new URI("https://na.api.pvp.net/api/lol/na/v2.2/matchhistory/"+summoner.id+"?beginIndex=0&endIndex=1&"+Constants.KEY_PARAM);
			HttpGet request = new HttpGet();
			request.setURI(website);
			HttpResponse response = httpclient.execute(request);
			Log.e("log_tag", "Last Match response "+response.getStatusLine().getStatusCode());
			String jsonStr = EntityUtils.toString(response.getEntity());
			JsonParser parser = new JsonParser();
			JsonObject jsonObj = parser.parse(jsonStr).getAsJsonObject();			
			jsonObj = jsonObj.get("matches").getAsJsonArray().get(0).getAsJsonObject();
			long mostRecentMatchId = jsonObj.get("matchId").getAsLong();
			Log.e("GameStateListener", "Last Match ID: " + lastMatchId + " Most Recent Match ID: " + mostRecentMatchId);
			if (mostRecentMatchId == lastMatchId) {
				return;
			}
			stateChanged = true;
			
		} catch (Exception e) {
			Log.e("GameStateListener", "Error in http connection " + e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public boolean stateChanged() {
		return stateChanged;
	}

}
