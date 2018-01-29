package com.tistory.hornslied.evitaonline.punish;

import java.util.ArrayList;
import java.util.UUID;

public class WarnList {
	private UUID uuid;
	private ArrayList<Warn> warns;
	
	public WarnList(UUID uuid) {
		this.uuid = uuid;
		warns = new ArrayList<>();
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public void addWarn(Warn warn) {
		warns.add(warn);
	}
	
	public void removeWarn(Warn warn) {
		warns.remove(warn);
	}
	
	public int getWarnNumber() {
		return warns.size();
	}
}
