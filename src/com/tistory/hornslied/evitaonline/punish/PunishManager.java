package com.tistory.hornslied.evitaonline.punish;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import com.tistory.hornslied.evitaonline.core.EvitaCoreMain;
import com.tistory.hornslied.evitaonline.events.EvitaBanEvent;
import com.tistory.hornslied.evitaonline.events.EvitaMuteEvent;
import com.tistory.hornslied.evitaonline.events.EvitaUnbanEvent;
import com.tistory.hornslied.evitaonline.events.EvitaWarnEvent;
import com.tistory.hornslied.evitaonline.mod.EvitaModerationMain;
import com.tistory.hornslied.evitaonline.utils.ChatTools;
import com.tistory.hornslied.evitaonline.utils.Resources;

import net.md_5.bungee.api.ChatColor;

public class PunishManager implements Listener {
	private volatile static PunishManager instance;

	private HashMap<UUID, WarnList> warnLists;
	private HashMap<UUID, Date> mutedPlayers;
	private HashMap<UUID, Timer> muteExpireTimers;

	private PunishManager() {
		warnLists = new HashMap<>();
		mutedPlayers = new HashMap<>();
		muteExpireTimers = new HashMap<>();

		loadWarns();
		loadMutes();

		Bukkit.getPluginManager().registerEvents(this, EvitaModerationMain.getInstance());
	}

	private void loadWarns() {
		ResultSet rs = EvitaCoreMain.getInstance().getDB().selectUpdatable("SELECT * FROM warns");

		try {
			while (rs.next()) {
				Date expire = new Date(rs.getLong("date") + 432000000);

				if (expire.before(new Date())) {
					rs.deleteRow();
					continue;
				}

				UUID uuid = UUID.fromString(rs.getString("uuid"));
				if (!warnLists.containsKey(uuid))
					warnLists.put(uuid, new WarnList(uuid));
				warnLists.get(uuid).addWarn(new Warn(uuid, rs.getString("punisher"), new Date(rs.getLong("date"))));
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void loadMutes() {
		ResultSet rs = EvitaCoreMain.getInstance().getDB().selectUpdatable("SELECT * FROM mutes");

		try {
			while (rs.next()) {
				Date expire = new Date(rs.getLong("expire"));

				if (expire.before(new Date())) {
					rs.deleteRow();
					continue;
				}

				UUID uuid = UUID.fromString(rs.getString("uuid"));

				mutedPlayers.put(uuid, expire);
				muteExpireTimers.put(uuid, new Timer());
				muteExpireTimers.get(uuid).schedule(new TimerTask() {

					@Override
					public void run() {
						unmute(Bukkit.getOfflinePlayer(uuid));
					}
				}, expire);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static PunishManager getInstance() {
		if (instance == null) {
			synchronized (PunishManager.class) {
				if (instance == null) {
					instance = new PunishManager();
				}
			}
		}

		return instance;
	}

	public void ban(String name, CommandSender punisher, String reason) {
		String formattedReason = ChatColor.RED + "관리자에 의해 밴 처리되었습니다!" + "\n" + "\n" + ChatColor.GRAY + "처리자: "
				+ ChatColor.YELLOW + ((punisher instanceof Player) ? punisher.getName() : "서버") + "\n" + ChatColor.GRAY
				+ "사유: " + ChatColor.YELLOW + reason + "\n" + ChatColor.GRAY + "만료: " + ChatColor.YELLOW + "영구" + "\n"
				+ "\n" + ChatColor.YELLOW + "이의 제기는 http://cafe.naver.com/evitaonline";
		Bukkit.getBanList(Type.NAME).addBan(name, formattedReason, null, punisher.getName());
		Bukkit.getPluginManager().callEvent(new EvitaBanEvent(Type.NAME,
				((punisher instanceof Player) ? punisher.getName() : "서버"), name, reason, null));

		Player player = Bukkit.getPlayer(name);
		if (player != null)
			player.kickPlayer(formattedReason);
	}

	public void ban(String name, CommandSender punisher, String reason, Date expire) {
		String formattedReason = ChatColor.RED + "관리자에 의해 밴 처리되었습니다!" + "\n" + "\n" + ChatColor.GRAY + "처리자: "
				+ ChatColor.YELLOW + ((punisher instanceof Player) ? punisher.getName() : "서버") + "\n" + ChatColor.GRAY
				+ "사유: " + ChatColor.YELLOW + reason + "\n" + ChatColor.GRAY + "만료: " + ChatColor.YELLOW
				+ expire.toString() + "\n" + "\n" + ChatColor.YELLOW + "이의 제기는 http://cafe.naver.com/evitaonline";
		Bukkit.getBanList(Type.NAME).addBan(name, formattedReason, expire, punisher.getName());
		Bukkit.getPluginManager().callEvent(new EvitaBanEvent(Type.NAME,
				((punisher instanceof Player) ? punisher.getName() : "서버"), name, reason, expire));

		Player player = Bukkit.getPlayer(name);
		if (player != null)
			player.kickPlayer(formattedReason);
	}

	public void banip(String ip, CommandSender punisher, String reason) {
		String formattedReason = ChatColor.RED + "관리자에 의해 아이피 밴 처리되었습니다!" + "\n" + "\n" + ChatColor.GRAY + "처리자: "
				+ ChatColor.YELLOW + ((punisher instanceof Player) ? punisher.getName() : "서버") + "\n" + ChatColor.GRAY
				+ "사유: " + ChatColor.YELLOW + reason + "\n" + ChatColor.GRAY + "만료: " + ChatColor.YELLOW + "영구" + "\n"
				+ "\n" + ChatColor.YELLOW + "이의 제기는 http://cafe.naver.com/evitaonline";
		Bukkit.getBanList(Type.NAME).addBan(ip, formattedReason, null, punisher.getName());
		Bukkit.getPluginManager().callEvent(new EvitaBanEvent(Type.IP,
				((punisher instanceof Player) ? punisher.getName() : "서버"), ip, reason, null));
	}

	public void banip(String ip, CommandSender punisher, String reason, Date expire) {
		String formattedReason = ChatColor.RED + "관리자에 의해 아이피 밴 처리되었습니다!" + "\n" + "\n" + ChatColor.GRAY + "처리자: "
				+ ChatColor.YELLOW + ((punisher instanceof Player) ? punisher.getName() : "서버") + "\n" + ChatColor.GRAY
				+ "사유: " + ChatColor.YELLOW + reason + "\n" + ChatColor.GRAY + "만료: " + ChatColor.YELLOW
				+ expire.toString() + "\n" + "\n" + ChatColor.YELLOW + "이의 제기는 http://cafe.naver.com/evitaonline";
		Bukkit.getBanList(Type.IP).addBan(ip, formattedReason, expire, punisher.getName());
		Bukkit.getPluginManager().callEvent(new EvitaBanEvent(Type.IP,
				((punisher instanceof Player) ? punisher.getName() : "서버"), ip, reason, expire));
	}

	public void unban(String name, CommandSender expirer) {
		Bukkit.getBanList(Type.NAME).pardon(name);
		Bukkit.getPluginManager().callEvent(
				new EvitaUnbanEvent(Type.NAME, ((expirer instanceof Player) ? expirer.getName() : "서버"), name));
	}

	public void unbanIp(String ip, CommandSender expirer) {
		Bukkit.getBanList(Type.IP).pardon(ip);
		Bukkit.getPluginManager()
				.callEvent(new EvitaUnbanEvent(Type.IP, ((expirer instanceof Player) ? expirer.getName() : "서버"), ip));
	}

	public boolean isBanned(String name) {
		return Bukkit.getBanList(Type.NAME).isBanned(name);
	}

	public boolean isBannedIp(String ip) {
		return Bukkit.getBanList(Type.IP).isBanned(ip);
	}

	public void warn(OfflinePlayer player, CommandSender punisher) {
		if (!warnLists.containsKey(player.getUniqueId())) {
			warnLists.put(player.getUniqueId(), new WarnList(player.getUniqueId()));
		}

		Date now = new Date();

		warnLists.get(player.getUniqueId()).addWarn(new Warn(player.getUniqueId(), punisher.getName(), now));
		EvitaCoreMain.getInstance().getDB().query("INSERT INTO warns (uuid, punisher, date) VALUES ('"
				+ player.getUniqueId().toString() + "', '" + punisher.getName() + "', " + now.getTime() + ");");
		Bukkit.getPluginManager().callEvent(new EvitaWarnEvent(punisher.getName(), player.getName()));
	}

	public void mute(OfflinePlayer player, String punisher, Date expire) {
		if (mutedPlayers.containsKey(player.getUniqueId())) {
			unmute(player);
		}

		mutedPlayers.put(player.getUniqueId(), expire);
		muteExpireTimers.put(player.getUniqueId(), new Timer());
		muteExpireTimers.get(player.getUniqueId()).schedule(new TimerTask() {

			@Override
			public void run() {
				unmute(player);
			}
		}, expire);
		EvitaCoreMain.getInstance().getDB().query("INSERT INTO mutes (uuid, punisher, expire) VALUES ('"
				+ player.getUniqueId().toString() + "', '" + punisher + "', " + expire.getTime() + ");");
		Bukkit.getPluginManager().callEvent(new EvitaMuteEvent(punisher, player.getName(), expire));
	}

	public void unmute(OfflinePlayer player) {
		if (mutedPlayers.containsKey(player.getUniqueId())) {
			mutedPlayers.remove(player.getUniqueId());
			muteExpireTimers.get(player.getUniqueId()).cancel();
			muteExpireTimers.remove(player.getUniqueId());
			EvitaCoreMain.getInstance().getDB()
					.query("DELETE FROM mutes WHERE uuid = '" + player.getUniqueId().toString() + "';");
		}
	}

	public boolean isMuted(OfflinePlayer player) {
		return mutedPlayers.containsKey(player.getUniqueId());
	}

	public WarnList getWarnList(UUID uuid) {
		return warnLists.get(uuid);
	}

	// Listeners

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncChatHookEvent e) {
		Player player = e.getPlayer();
		if (mutedPlayers.containsKey(player.getUniqueId())) {
			e.getAsyncPlayerChatEvent().setCancelled(true);
			long millisec = mutedPlayers.get(player.getUniqueId()).getTime() - System.currentTimeMillis();
			String time = String.format("%02d시간 %02d분 %02d초", TimeUnit.MILLISECONDS.toHours(millisec),
					TimeUnit.MILLISECONDS.toMinutes(millisec) % TimeUnit.HOURS.toMinutes(1),
					TimeUnit.MILLISECONDS.toSeconds(millisec) % TimeUnit.MINUTES.toSeconds(1));
			player.sendMessage(
					Resources.tagServer + ChatColor.RED + "채팅 금지 제재를 받았기 때문에 채팅을 보낼 수 없습니다.(남은 시간: " + time + ")");
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBan(EvitaBanEvent e) {
		ArrayList<String> messages = new ArrayList<>();

		switch (e.getType()) {
		case NAME:
			messages.add(ChatColor.STRIKETHROUGH + ChatTools.formatLine());
			messages.add(ChatColor.RED + "유저 " + e.getPunishee() + " 가 관리자에 의해 밴 처리되었습니다!");
			messages.add(ChatColor.RESET.toString());
			messages.add(ChatColor.GRAY + "처리자: " + ChatColor.YELLOW + e.getPunisher());
			messages.add(ChatColor.GRAY + "사유: " + ChatColor.YELLOW + e.getReason());
			messages.add(ChatColor.GRAY + "만료: " + ChatColor.YELLOW
					+ ((e.getExpire() == null) ? "영구" : e.getExpire().toString()));
			messages.add(ChatColor.STRIKETHROUGH + ChatTools.formatLine());

			for (String s : messages) {
				Bukkit.broadcastMessage(s);
			}
			break;
		case IP:
			messages.add(ChatColor.STRIKETHROUGH + ChatTools.formatLine());
			messages.add(ChatColor.RED + "아이피 " + formatIp(e.getPunishee()) + " 가 관리자에 의해 밴 처리되었습니다!");
			messages.add(ChatColor.RESET.toString());
			messages.add(ChatColor.GRAY + "처리자: " + ChatColor.YELLOW + e.getPunisher());
			messages.add(ChatColor.GRAY + "사유: " + ChatColor.YELLOW + e.getReason());
			messages.add(ChatColor.GRAY + "만료: " + ChatColor.YELLOW
					+ ((e.getExpire() == null) ? "영구" : e.getExpire().toString()));
			messages.add(ChatColor.STRIKETHROUGH + ChatTools.formatLine());

			for (String s : messages) {
				Bukkit.broadcastMessage(s);
			}
			break;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onMute(EvitaMuteEvent e) {
		ArrayList<String> messages = new ArrayList<>();

		messages.add(ChatColor.STRIKETHROUGH + ChatTools.formatLine());
		messages.add(ChatColor.RED + "관리자가 유저 " + e.getPunishee() + " 에게 채팅금지를 부여했습니다.");
		messages.add(ChatColor.RESET.toString());
		messages.add(ChatColor.GRAY + "처리자: " + ChatColor.YELLOW + e.getPunisher());
		messages.add(ChatColor.GRAY + "만료: " + ChatColor.YELLOW + e.getExpire().toString());
		messages.add(ChatColor.STRIKETHROUGH + ChatTools.formatLine());

		for (String s : messages) {
			Bukkit.broadcastMessage(s);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWarn(EvitaWarnEvent e) {
		ArrayList<String> messages = new ArrayList<>();

		messages.add(ChatColor.STRIKETHROUGH + ChatTools.formatLine());
		messages.add(ChatColor.RED + "유저 " + e.getPunishee() + " 가 관리자에게 경고를 받았습니다.");
		messages.add(ChatColor.RESET.toString());
		messages.add(ChatColor.GRAY + "처리자: " + ChatColor.YELLOW + e.getPunisher());
		messages.add(ChatColor.GRAY + "만료: " + ChatColor.YELLOW + "7일 후");
		messages.add(ChatColor.STRIKETHROUGH + ChatTools.formatLine());

		for (String s : messages) {
			Bukkit.broadcastMessage(s);
		}
	}

	private String formatIp(String ip) {
		return ip.split(".")[0] + ".***." + ip.split(".")[2] + ".***";
	}
}
