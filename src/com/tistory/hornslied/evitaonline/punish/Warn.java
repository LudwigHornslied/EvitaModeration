package com.tistory.hornslied.evitaonline.punish;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.tistory.hornslied.evitaonline.core.EvitaCoreMain;

public class Warn {
	private UUID uuid;
	private String punisher;
	private Date date;
	
	public Warn(UUID uuid, String punisher, Date date) {
		this.uuid = uuid;
		this.punisher = punisher;
		this.date = date;
		
		setExpire(date);
	}
	
	private void setExpire(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 7);
		
		new Timer().schedule(new WarnExpireTimerTask(this), calendar.getTime());
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public String getPunisher() {
		return punisher;
	}
	
	class WarnExpireTimerTask extends TimerTask {
		private Warn warn;
		
		public WarnExpireTimerTask(Warn warn) {
			this.warn = warn;
		}
		
		@Override
		public void run() {
			PunishManager.getInstance().getWarnList(uuid).removeWarn(warn);
			EvitaCoreMain.getInstance().getDB().query("DELETE FROM warns WHERE date = " + date.getTime() + ";");
		}
	}
}
