package com.gta0004.loltracker.events;

import android.os.Parcelable;

public interface IEvent extends Parcelable{
  
  public String getFormattedEventTime();
  public long getEventTime();
  public int getEventType();
  public String getMessage();
  public int compareTo(IEvent rhs);
  public String getRegion();

}
