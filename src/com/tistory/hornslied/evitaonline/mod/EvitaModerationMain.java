package com.tistory.hornslied.evitaonline.mod;

import org.bukkit.plugin.java.JavaPlugin;

import com.tistory.hornslied.evitaonline.commands.PunishCommand;
import com.tistory.hornslied.evitaonline.commands.ReportCommand;
import com.tistory.hornslied.evitaonline.core.EvitaCoreMain;
import com.tistory.hornslied.evitaonline.db.DB;
import com.tistory.hornslied.evitaonline.punish.PunishManager;
import com.tistory.hornslied.evitaonline.report.ReportManager;

public class EvitaModerationMain extends JavaPlugin {
	private static EvitaModerationMain instance;
	private DB db;

	public static EvitaModerationMain getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		db = EvitaCoreMain.getInstance().getDB();
		createDBTables();
		PunishManager.getInstance();
		ReportManager.getInstance();
		
		initCommands();
	}

	private void createDBTables() {
		db.query("CREATE TABLE IF NOT EXISTS warns ("
				+ "id int NOT NULL PRIMARY KEY AUTO_INCREMENT,"
				+ "uuid varchar(40) NOT NULL,"
				+ "punisher varchar(50) NOT NULL,"
				+ "date bigint DEFAULT NULL"
				+ ");");
		db.query("CREATE TABLE IF NOT EXISTS mutes (" 
				+ "uuid varchar(255) NOT NULL PRIMARY KEY,"
				+ "punisher varchar(255) NOT NULL,"
				+ "expire bigint DEFAULT NULL"
				+ ");");
		db.query("CREATE TABLE IF NOT EXISTS reports ("
				+ "id int NOT NULL PRIMARY KEY AUTO_INCREMENT,"
				+ "date varchar(20) NOT NULL,"
				+ "reporter varchar(100) NOT NULL,"
				+ "reportee varchar(50) NOT NULL,"
				+ "reason varchar(45) NOT NULL"
				+ ");");
	}
	
	private void initCommands() {
		PunishCommand punishCommand = new PunishCommand();
		ReportCommand reportCommand = new ReportCommand();
		
		getCommand("ban").setExecutor(punishCommand);
		getCommand("ban-ip").setExecutor(punishCommand);
		getCommand("banlist").setExecutor(punishCommand);
		getCommand("unban").setExecutor(punishCommand);
		getCommand("unban-ip").setExecutor(punishCommand);
		getCommand("warn").setExecutor(punishCommand);
		getCommand("mute").setExecutor(punishCommand);
		getCommand("unmute").setExecutor(punishCommand);
		getCommand("report").setExecutor(reportCommand);
		getCommand("freeze").setExecutor(reportCommand);
	}
}
