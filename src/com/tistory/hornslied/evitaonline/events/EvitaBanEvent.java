package com.tistory.hornslied.evitaonline.events;

import java.util.Date;

import org.bukkit.BanList;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EvitaBanEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private BanList.Type type;
	private String punisher;
	private String punishee;
	private String reason;
	private Date expire;
	
	public EvitaBanEvent(BanList.Type type, String punisher, String punishee, String reason, Date expire) {
		this.type = type;
		this.punisher = punisher;
		this.punishee = punishee;
		this.reason = reason;
		this.expire = expire;
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
    
    public String getPunisher() {
    	return punisher;
    }
    
    public String getPunishee() {
    	return punishee;
    }
    
    public String getReason() {
    	return reason;
    }
    
    public Date getExpire() {
    	return expire;
    }
}
