package com.gta0004.loltracker.riot;

import android.os.Parcel;
import android.os.Parcelable;

public class LastMatch implements Parcelable{
  public long matchId;
  public String queueType;
  public boolean winner;
  public int pentakills;
  public int champId;
  public String champName;
  public long matchCreation;
  public long matchDuration;

  public LastMatch() {

  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(matchId);
    dest.writeString(queueType);
    dest.writeInt(winner ? 1 : 0);
    dest.writeInt(pentakills);   
    dest.writeInt(champId);
    dest.writeString(champName);
    dest.writeLong(matchCreation);
    dest.writeLong(matchDuration);
  }
  
  public static final Parcelable.Creator<LastMatch> CREATOR = new Parcelable.Creator<LastMatch>() {
    public LastMatch createFromParcel(Parcel in) {
      return new LastMatch(in);
    }

    public LastMatch[] newArray(int size) {
      return new LastMatch[size];
    }
  };

  private LastMatch(Parcel in) {
    matchId = in.readLong();
    queueType = in.readString();
    winner = (in.readInt() == 1);
    pentakills = in.readInt();
    champId = in.readInt();
    champName = in.readString();
    matchCreation = in.readLong();
    matchDuration = in.readLong();
  }
  
  public long getMatchFinish() {
    return matchCreation + (matchDuration*1000);
  }
}
