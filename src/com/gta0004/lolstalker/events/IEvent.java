package com.gta0004.lolstalker.events;

import android.os.Parcelable;

public interface IEvent extends Parcelable{
  
  public String getEventTime();
  public int getEventType();
  public String getMessage();

}
