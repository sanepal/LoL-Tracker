package com.gta0004.lolstalker.listeners;

import com.gta0004.lolstalker.events.IEvent;

public abstract class AbstractPlayerActivityListener implements IPlayerActivityListener{
	public abstract void run();
	public abstract boolean stateChanged();
	public abstract String getMessage();
	public abstract IEvent getEvent();
}
