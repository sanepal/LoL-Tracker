package com.gta0004.lolstalker.events;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;

import com.gta0004.lolstalker.riot.Summoner;
import com.gta0004.lolstalker.utils.Constants;

public class LastGameEvent implements IEvent {
  private static final String TAG = "LastGameEvent";
  private String summonerName;
  private boolean winner;
  private int champId;
  private long matchFinish;
  
  public LastGameEvent (Summoner summoner) {
    this.summonerName = summoner.name;
    this.winner = summoner.lastMatch.winner;
    this.champId = summoner.lastMatch.champId;
    this.matchFinish = summoner.lastMatch.getMatchFinish();
  }
  
  private LastGameEvent(Parcel in) {    
    summonerName = in.readString();
    winner = (in.readInt() == 1);
    champId = in.readInt();
    matchFinish = in.readLong();
  }
  
  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(summonerName);
    dest.writeInt(winner ? 1 : 0);
    dest.writeInt(champId);
    dest.writeLong(matchFinish);
  }
  
  public static final Parcelable.Creator<LastGameEvent> CREATOR = new Parcelable.Creator<LastGameEvent>() {
    public LastGameEvent createFromParcel(Parcel in) {
      return new LastGameEvent(in);
    }

    public LastGameEvent[] newArray(int size) {
      return new LastGameEvent[size];
    }
  };
  
  @Override
  public String getMessage() {
    String s = "";
    s += summonerName + " ";
    if (winner)
      s += "won a game ";
    else
      s += "lost a game ";
    s += "as " + champId + ".";
    return s;
  }
  
  @Override
  public int getEventType() {
    if (winner)
      return Constants.EVENT_POSITIVE;
    else
      return Constants.EVENT_NEGATIVE;
  }

  @Override
  public String getFormattedEventTime() {
    return DateUtils.getRelativeTimeSpanString(matchFinish).toString();
  }
  
  @Override
  public long getEventTime() {
    return matchFinish;
  }
  
  @Override
  public int compareTo(IEvent rhs) {
    //requires API level 19 qqqq
    //return Long.compare(this.getEventTime(), rhs.getEventTime());    
    
    Long lhs = Long.valueOf(getEventTime());
    return lhs.compareTo(Long.valueOf(rhs.getEventTime()));
  }
}
