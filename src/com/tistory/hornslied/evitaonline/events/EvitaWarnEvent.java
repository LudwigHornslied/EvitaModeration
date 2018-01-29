package com.tistory.hornslied.evitaonline.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EvitaWarnEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private String punisher;
	private String punishee;
	
	public EvitaWarnEvent(String punisher, String punishee) {
		this.punisher = punisher;
		this.punishee = punishee;
	}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public String getPunisher() {
    	return punisher;
    }
    
    public String getPunishee() {
    	return punishee;
    }
}
