package com.tistory.hornslied.evitaonline.events;

import org.bukkit.BanList;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EvitaUnbanEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private BanList.Type type;
	private String expirer;
	private String expiree;
	
	public EvitaUnbanEvent(BanList.Type type, String expirer, String expiree) {
		this.type = type;
		this.expirer = expirer;
		this.expiree = expiree;
	}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public BanList.Type getType() {
    	return type;
    }
    
    public String getExpirer() {
    	return expirer;
    }
    
    public String getExpiree() {
    	return expiree;
    }
}
