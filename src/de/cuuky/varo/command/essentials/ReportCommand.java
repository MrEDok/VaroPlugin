package de.cuuky.varo.command.essentials;

import de.cuuky.varo.Main;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.gui.report.ReportGUI;
import de.cuuky.varo.gui.report.ReportListGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ReportCommand implements CommandExecutor {

	private Map<Player, Long> timings = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!ConfigSetting.REPORTSYSTEM_ENABLED.getValueAsBoolean()) {
			sender.sendMessage(Main.getPrefix() + "§cReports §7were deactivated in the Config!");
			return false;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(Main.getPrefix() + "Only for Player");
			return false;
		}

		Player player = Bukkit.getPlayerExact(sender.getName());

		if (args.length == 0 || args.length > 1) {
			sender.sendMessage(Main.getPrefix() + "§7------ §cReport §7------");
			sender.sendMessage(Main.getPrefix() + "§c/report §7<Player>");
			if (sender.hasPermission("varo.reports"))
				sender.sendMessage(Main.getPrefix() + "§c/report list");
			sender.sendMessage(Main.getPrefix() + "§7-------------------");
			return false;
		}

		if (args[0].equals("list") && player.hasPermission("varo.reports")) {
			new ReportListGUI(player);
			return false;
		}

		Player reported = Bukkit.getPlayer(args[0]);
		if (reported == null) {
			sender.sendMessage(Main.getPrefix() + "This player is not online!");
			return false;
		}

		if (reported == player) {
			sender.sendMessage(Main.getPrefix() + "Did you just try to report yourself? monkey.");
			return false;
		}

		if (!ConfigSetting.REPORT_STAFF_MEMBER.getValueAsBoolean() && reported.hasPermission("varo.reports")) {
			sender.sendMessage(Main.getPrefix() + "You are not allowed to report team members!");
			return false;
		}

		if (this.timings.containsKey(player)) {
			if ((System.currentTimeMillis() - this.timings.get(player)) / 1000 <= ConfigSetting.REPORT_SEND_DELAY.getValueAsInt()) {
				sender.sendMessage(Main.getPrefix() + "Wait between Reports " + ConfigSetting.REPORT_SEND_DELAY.getValueAsInt() + "s");
				return false;
			}
		}

		timings.put(player, System.currentTimeMillis());
		new ReportGUI(player, VaroPlayer.getPlayer(reported));
		return true;
	}
}