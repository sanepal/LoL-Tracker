package com.gta0004.lolstalker.events;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;

import com.gta0004.lolstalker.riot.Summoner;
import com.gta0004.lolstalker.utils.Constants;

public class LastGameEvent implements IEvent {
  private static final String TAG = "LastGameEvent";
  private Summoner summoner;
  
  public LastGameEvent (Summoner summoner) {
    this.summoner = summoner;
  }
  
  private LastGameEvent(Parcel in) {    
    summoner = in.readParcelable(Summoner.class.getClassLoader());
  }
  
  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(summoner, flags);    
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
    s += summoner.name + " ";
    if (summoner.lastMatch.winner)
      s += "won a game ";
    else
      s += "lost a game ";
    s += "as " + summoner.lastMatch.champId + ".";
    return s;
  }
  
  @Override
  public int getEventType() {
    if (summoner.lastMatch.winner)
      return Constants.EVENT_POSITIVE;
    else
      return Constants.EVENT_NEGATIVE;
  }

  @Override
  public String getFormattedEventTime() {
    return DateUtils.getRelativeTimeSpanString(summoner.lastMatch.getMatchFinish()).toString();
  }
  
  @Override
  public long getEventTime() {
    return summoner.lastMatch.getMatchFinish();
  }
  
  @Override
  public int compareTo(IEvent rhs) {
    //requires API level 19 qqqq
    //return Long.compare(this.getEventTime(), rhs.getEventTime());    
    
    Long lhs = Long.valueOf(getEventTime());
    return lhs.compareTo(Long.valueOf(rhs.getEventTime()));
  }
}
