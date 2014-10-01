package com.gta0004.loltracker.listeners;

import com.gta0004.loltracker.events.IEvent;

public abstract class AbstractPlayerActivityListener implements IPlayerActivityListener{
	public abstract void run();
	public abstract boolean stateChanged();
	public abstract void setCallback(OnPlayerActivityListenerFinished mCallback);
	public abstract String getMessage();
	public abstract IEvent getEvent();
}
