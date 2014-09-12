package com.gta0004.lolstalker.riot;

import android.os.Parcel;
import android.os.Parcelable;

public class Summoner implements Parcelable{
  public long id;
  public String name;
  public int profileIconId;
  public long revisionDate;
  public int summonerLevel;
  public LastMatch lastMatch;

  public Summoner(long id, String name, int profileIconId, long revisionDate, int summonerLevel) {
    this.id = id;
    this.name = name;
    this.profileIconId = profileIconId;
    this.revisionDate = revisionDate;
    this.summonerLevel = summonerLevel;
  }

  public Summoner(long id, String name) {
    this.id = id;
    this.name = name;
  }
  
  public Summoner() {
    
  }

  public String toString() {
    return name;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(id);
    dest.writeString(name);
    dest.writeInt(profileIconId);
    dest.writeInt(summonerLevel);
    dest.writeParcelable(lastMatch, flags);    
  }
  
  public static final Parcelable.Creator<Summoner> CREATOR = new Parcelable.Creator<Summoner>() {
    public Summoner createFromParcel(Parcel in) {
      return new Summoner(in);
    }

    public Summoner[] newArray(int size) {
      return new Summoner[size];
    }
  };

  private Summoner(Parcel in) {
    id = in.readLong();
    name = in.readString();
    profileIconId = in.readInt();
    summonerLevel = in.readInt();
    lastMatch = in.readParcelable(LastMatch.class.getClassLoader());
  }
}
