package com.tistory.hornslied.evitaonline.report;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.tistory.hornslied.evitaonline.core.EvitaCoreMain;
import com.tistory.hornslied.evitaonline.events.EvitaReportEvent;
import com.tistory.hornslied.evitaonline.mod.EvitaModerationMain;
import com.tistory.hornslied.evitaonline.punish.PunishManager;
import com.tistory.hornslied.evitaonline.utils.Resources;

import net.md_5.bungee.api.ChatColor;

public class ReportManager implements Listener {
	private volatile static ReportManager instance;

	private ReportManager() {
		Bukkit.getPluginManager().registerEvents(this, EvitaModerationMain.getInstance());
	}

	public static ReportManager getInstance() {
		if (instance == null) {
			synchronized (PunishManager.class) {
				if (instance == null) {
					instance = new ReportManager();
				}
			}
		}

		return instance;
	}

	public void freeze(Player player) {
		player.setMetadata("freeze", new FixedMetadataValue(EvitaModerationMain.getInstance(), null));
	}

	public void unfreeze(Player player) {
		player.removeMetadata("freeze", EvitaModerationMain.getInstance());
	}

	public void report(String reporter, String reportee, String reason) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String date = df.format(new Date(System.currentTimeMillis()));

		EvitaCoreMain.getInstance().getDB().query("INSERT INTO reports (date, reporter, reportee, reason) VALUES ('"
				+ date + "', '" + reporter + "', '" + reportee + "');");

		Bukkit.getPluginManager().callEvent(new EvitaReportEvent(reporter, reportee, reason));

		Bukkit.broadcast(
				Resources.tagServer + ChatColor.AQUA + reporter + " 님이 " + reportee + " 를 신고하셨습니다. 사유: " + reason,
				"evita.mod");
	}

	// Listeners

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getPlayer().hasMetadata("freeze"))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getPlayer().hasMetadata("frozen"))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (e.getPlayer().hasMetadata("freeze")) {
			e.getPlayer().sendMessage(Resources.tagServer + ChatColor.RED + "프리즈 상태에서는 텔레포트를 할수 없습니다.");
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamage(EntityDamageEvent e) {
		Entity entity = e.getEntity();

		if (entity instanceof Player) {
			if (((Player) entity).hasMetadata("freeze"))
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (e.getPlayer().hasMetadata("freeze"))
			PunishManager.getInstance().ban(e.getPlayer().getName(), Bukkit.getConsoleSender(), "스크린 쉐어 회피");
	}
}
