package com.tistory.hornslied.evitaonline.commands;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

import org.bukkit.BanEntry;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.tistory.hornslied.evitaonline.punish.PunishManager;
import com.tistory.hornslied.evitaonline.utils.ChatTools;
import com.tistory.hornslied.evitaonline.utils.Resources;

public class PunishCommand implements CommandExecutor {

	class BanComparator implements Comparator<BanEntry> {

		@Override
		public int compare(BanEntry o1, BanEntry o2) {
			if (o1.getCreated().before(o2.getCreated())) {
				return -1;
			} else {
				return 1;
			}
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("evita.mod")) {
			sender.sendMessage(Resources.messagePermission);
			return false;
		}

		switch (cmd.getLabel()) {
		case "ban":
			if (args.length < 3) {
				sender.sendMessage(
						Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /밴 <플레이어> <기간> <사유> (영구 밴은 기간: -1)");
				break;
			}

			if (Bukkit.getOfflinePlayer(args[0]) == null) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "존재하지 않는 플레이어입니다!");
				break;
			}

			int period1;
			try {
				period1 = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(
						Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /밴 <플레이어> <기간(일 단위)> <사유> (영구 밴은 기간: -1)");
				break;
			}

			if (period1 == 0) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "기간에 0을 넣을수 없습니다!");
				break;
			}

			StringBuilder reason1 = new StringBuilder();

			for (int i = 2; i < args.length; i++) {
				reason1.append(args[i] + " ");
			}

			if (period1 < 0) {
				PunishManager.getInstance().ban(args[0], sender, reason1.toString());
			} else {
				Calendar now = Calendar.getInstance();
				now.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) + period1);
				PunishManager.getInstance().ban(args[0], sender, reason1.toString(), now.getTime());
			}
			break;
		case "ban-ip":
			if (args.length < 3) {
				sender.sendMessage(
						Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /아이피밴 <아이피> <기간(일 단위)> <사유> (영구 밴은 기간: -1)");
				break;
			}

			int period2;
			try {
				period2 = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(
						Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /아이피밴 <아이피> <기간(일 단위)> <사유> (영구 밴은 기간: -1)");
				break;
			}

			if (period2 == 0) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "기간에 0을 넣을수 없습니다!");
				break;
			}

			StringBuilder reason2 = new StringBuilder();

			for (int i = 2; i < args.length; i++) {
				reason2.append(args[i] + " ");
			}

			if (period2 < 0) {
				PunishManager.getInstance().banip(args[0], sender, reason2.toString());
			} else {
				Calendar now = Calendar.getInstance();
				now.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) + period2);
				PunishManager.getInstance().banip(args[0], sender, reason2.toString(), now.getTime());
			}
			break;
		case "banlist":
			if (args.length < 2) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /밴목록 [플레이어/아이피] <페이지>");
				break;
			}

			int index;

			try {
				index = Integer.parseInt(args[1]);

				if (index < 1) {
					sender.sendMessage(Resources.tagServer + ChatColor.RED + "페이지는 양수여야 합니다!");
					break;
				}
			} catch (NumberFormatException e) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /밴목록 [플레이어/아이피] <페이지>");
				break;
			}

			ArrayList<String> out = new ArrayList<String>();
			ArrayList<BanEntry> bans = null;

			switch (args[0].toLowerCase()) {
			case "플레이어":
			case "player":
				bans = new ArrayList<>(Bukkit.getBanList(Type.NAME).getBanEntries());
				out.add(ChatTools.formatTitle("밴 목록(" + index + " 페이지)"));
				out.add(ChatColor.GREEN + "(날짜)" + ChatColor.DARK_GRAY + " - " + ChatColor.AQUA + "(닉네임)"
						+ ChatColor.DARK_GRAY + " - " + ChatColor.RED + "(만료)" + ChatColor.DARK_GRAY + " - "
						+ ChatColor.LIGHT_PURPLE + "(처리자)");
				break;
			case "아이피":
			case "ip":
				bans = new ArrayList<>(Bukkit.getBanList(Type.IP).getBanEntries());
				out.add(ChatTools.formatTitle("밴 목록(" + index + " 페이지)"));
				out.add(ChatColor.GREEN + "(날짜)" + ChatColor.DARK_GRAY + " - " + ChatColor.AQUA + "(아이피)"
						+ ChatColor.DARK_GRAY + " - " + ChatColor.RED + "(만료)" + ChatColor.DARK_GRAY + " - "
						+ ChatColor.LIGHT_PURPLE + "(처리자)");
				break;
			default:
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /밴목록 [플레이어/아이피] <페이지>");
				break;
			}

			bans.sort(new BanComparator());

			if (bans.size() < (index - 1) * 6 + 1) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "해당 페이지가 없습니다!");
				break;
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");

			for (int i = 0; i < 6 && i + (index - 1) * 6 < bans.size(); i++) {
				String expire;
				
				BanEntry ban = bans.get(i + (index - 1) * 6);
				if(ban.getExpiration() == null) {
					expire = "영구";
				} else {
					expire = sdf.format(ban.getExpiration());
				}
				
				out.add(ChatColor.GREEN + sdf.format(ban.getCreated()) + ChatColor.DARK_GRAY + " - " + ChatColor.AQUA
						+ ban.getTarget() + ChatColor.DARK_GRAY + " - " + ChatColor.RED
						+ expire + ChatColor.DARK_GRAY + " - " + ChatColor.LIGHT_PURPLE
						+ ban.getSource());
			}

			for (String s : out) {
				sender.sendMessage(s);
			}

			break;
		case "warn":
			if (args.length < 1) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /경고 <플레이어>");
				break;
			}
			
			OfflinePlayer player2 = Bukkit.getOfflinePlayer(args[0]);

			if (player2 == null) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "존재하지 않는 플레이어입니다!");
				break;
			}
			
			PunishManager.getInstance().warn(player2, sender);
			break;
		case "mute":
			if (args.length < 2) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /채팅금지 <플레이어> <기간>");
				break;
			}

			OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);

			if (player == null) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "존재하지 않는 플레이어입니다!");
				break;
			}

			int period3;
			try {
				period3 = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /채팅금지 <플레이어> <기간>");
				break;
			}
			
			if(period3 < 1) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "기간은 양수여야 합니다!");
				break;
			}
			
			Calendar now = Calendar.getInstance();
			now.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) + period3);
			PunishManager.getInstance().mute(player, sender.getName(), now.getTime());
			break;
		case "unban":
			if (args.length < 1) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /밴해제 <플레이어>");
				break;
			}

			PunishManager punishManager1 = PunishManager.getInstance();

			if (!punishManager1.isBanned(args[0])) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "밴 처리된 플레이어가 아닙니다!");
				break;
			}

			punishManager1.unban(args[0], sender);
			sender.sendMessage(Resources.tagServer + ChatColor.AQUA + "유저 " + args[0] + " 의 밴이 해제되었습니다.");
			break;
		case "unban-ip":
			if (args.length < 1) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /아이피밴해제 <플레이어>");
				break;
			}

			PunishManager punishManager2 = PunishManager.getInstance();

			if (!punishManager2.isBannedIp(args[0])) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "밴 처리된 플레이어가 아닙니다!");
				break;
			}

			punishManager2.unbanIp(args[0], sender);
			sender.sendMessage(Resources.tagServer + ChatColor.AQUA + "아이피 " + args[0] + " 의 밴이 해제되었습니다.");
			break;
		case "unmute":
			if (args.length < 1) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /채팅금지해제 <플레이어>");
				break;
			}
			
			OfflinePlayer player1 = Bukkit.getOfflinePlayer(args[0]);
			
			if(player1 == null) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "존재하지 않는 플레이어입니다!");
				break;
			}
			
			PunishManager punishManager3 = PunishManager.getInstance();
			
			if(!punishManager3.isMuted(player1)) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "채팅금지를 받은 플레이어가 아닙니다!");
				break;
			}
			
			punishManager3.unmute(player1);
			sender.sendMessage(Resources.tagServer + ChatColor.AQUA + "플레이어 " + player1.getName() + " 의 채팅금지가 해제되었습니다.");
			break;
		}
		return false;
	}
}
