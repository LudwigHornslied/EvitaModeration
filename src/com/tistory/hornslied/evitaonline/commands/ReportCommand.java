package com.tistory.hornslied.evitaonline.commands;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tistory.hornslied.evitaonline.report.ReportManager;
import com.tistory.hornslied.evitaonline.utils.ChatTools;
import com.tistory.hornslied.evitaonline.utils.Resources;

public class ReportCommand implements CommandExecutor {
	private HashMap<CommandSender, Integer> reportCooldown;

	public ReportCommand() {
		reportCooldown = new HashMap<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		switch (cmd.getLabel()) {
		case "report":
			if (!(sender instanceof Player)) {
				sender.sendMessage(Resources.messageConsole);
				break;
			}

			if (reportCooldown.containsKey(sender)) {
				sender.sendMessage(
						Resources.tagServer + ChatColor.RED + "명령어 재사용 대기시간: " + reportCooldown.get(sender) + "초");
				break;
			}

			if (args.length < 2) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /신고 <플레이어> <사유>");
				break;
			}
			
			if(args[0].length() > 20) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "플레이어 닉네임은 20자 이내로 적어 주십시오.");
				break;
			}

			StringBuilder reason = new StringBuilder();

			for (int i = 1; i < args.length; i++) {
				reason.append(args[i] + " ");
			}

			if (reason.toString().length() > 40) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "사유는 40자 이내로 적어 주십시오.");
				break;
			}
			
			sender.sendMessage(Resources.tagServer + ChatColor.AQUA + "신고해 주셔서 감사합니다. 신고 정보는 DB에 저장됩니다.");
			ReportManager.getInstance().report(sender.getName(), args[0], reason.toString());
			reportCooldown.put(sender, 60);

			break;
		case "freeze":
			if (!(sender.hasPermission("evita.mod"))) {
				sender.sendMessage(Resources.messagePermission);
				break;
			}

			if (args.length == 0) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "명령어 사용 방법: /프리즈 <플레이어>");
				break;
			}
			Player player = Bukkit.getPlayer(args[0]);

			if (player == null) {
				sender.sendMessage(Resources.tagServer + ChatColor.RED + "해당 플레이어가 접속중이지 않거나 존재하지 않습니다.");
				break;
			}

			if (player.hasMetadata("freeze")) {
				ReportManager.getInstance().unfreeze(player);

				sender.sendMessage(
						Resources.tagServer + ChatColor.AQUA + "플레이어 " + player.getName() + " 의 프리즈 상태가 해제되었습니다.");
				player.sendMessage(Resources.tagServer + ChatColor.AQUA + "당신의 프리즈 상태가 해제되었습니다.");
			} else {
				ReportManager.getInstance().freeze(player);

				String[] messages = new String[8];
				messages[0] = ChatColor.STRIKETHROUGH + ChatTools.formatLine();
				messages[1] = ChatColor.RESET.toString();
				messages[2] = ChatColor.RED + "당신은 핵으로 의심받아 관리자에게 프리즈가 걸린 상태입니다.";
				messages[3] = ChatColor.RED + "모든 행동이 불가능하며, 서버에서 퇴장할시 " + ChatColor.DARK_RED + "밴 처리 " + ChatColor.RED
						+ "됩니다.";
				messages[4] = ChatColor.RED + "관리자의 지시에 따라 스크린쉐어를 해 주시기 바랍니다.";
				messages[5] = ChatColor.RED + "서버 디스코드: " + ChatColor.AQUA + "https://discord.gg/BF5fNhV";
				messages[6] = ChatColor.RESET.toString();
				messages[7] = ChatColor.STRIKETHROUGH + ChatTools.formatLine();

				sender.sendMessage(
						Resources.tagServer + ChatColor.AQUA + "플레이어 " + player.getName() + " 에게 프리즈를 걸었습니다.");
				player.sendMessage(messages);
			}
			break;
		}
		return false;
	}
}
