package com.tistory.hornslied.evitaonline.events;

import java.util.Date;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EvitaMuteEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private String punisher;
	private String punishee;
	private Date expire;
	
	public EvitaMuteEvent(String punisher, String punishee, Date expire) {
		this.punisher = punisher;
		this.punishee = punishee;
		this.expire = expire;
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
    
    public Date getExpire() {
    	return expire;
    }
}
