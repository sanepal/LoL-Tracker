package com.gta0004.lolstalker.listeners;

import com.gta0004.lolstalker.events.IEvent;

public interface IPlayerActivityListener {
  
	public void run();
	public void setCallback(OnPlayerActivityListenerFinished mCallback);
	public boolean stateChanged();
	public String getMessage();
	public IEvent getEvent();
}
