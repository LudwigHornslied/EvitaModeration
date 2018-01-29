package com.tistory.hornslied.evitaonline.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EvitaReportEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private String reporter;
	private String reportee;
	private String reason;
	
	public EvitaReportEvent(String reporter, String reportee, String reason) {
		this.reporter = reporter;
		this.reportee = reportee;
		this.reason = reason;
	}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public String getReporter() {
    	return reporter;
    }
    
    public String getReportee() {
    	return reportee;
    }
    
    public String getReason() {
    	return reason;
    }
}
