package de.cuuky.varo.command.varo;

import java.text.SimpleDateFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.player.stats.stat.Strike;

public class StrikeCommand extends VaroCommand {

	public StrikeCommand() {
		super("strike", "Use this command to strike a player", "varo.strike");
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(Main.getPrefix() + "§7------ " + Main.getColorCode() + "Strike §7-----");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " strike §7<Player> [Reason]");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " strike list §7<Player>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " strike remove §7<Player> <StrikeNumber>");
			sender.sendMessage(Main.getPrefix() + "§7-----------------------");
			return;
		}

		if (VaroPlayer.getPlayer(args[0]) != null) {
			VaroPlayer varoPlayer = VaroPlayer.getPlayer(args[0]);

			String reason = "";
			for (String key : args) {
				if (key.equals(args[0]))
					continue;
				reason += key;
			}

			if (reason.isEmpty()) {
				reason = "-";
			}

			Strike strike = new Strike(reason, varoPlayer, sender instanceof ConsoleCommandSender ? "CONSOLE" : "" + sender.getName());
			varoPlayer.getStats().addStrike(strike);
			sender.sendMessage(Main.getPrefix() + "Du hast " + varoPlayer.getName() + " strike!");
			return;
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length != 3) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/strike remove §7<Player> <Number>");
				return;
			}

			VaroPlayer varoPlayer = VaroPlayer.getPlayer(args[1]);

			if (varoPlayer == null) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + args[1] + " §7not found!");
				return;
			}

			if (varoPlayer.getStats().getStrikes().size() < 1) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + varoPlayer.getName() + " §7has no strikes!");
				return;
			}

			int num = -1;
			try {
				num = Integer.parseInt(args[2]) - 1;
			} catch (NumberFormatException e) {
				sender.sendMessage(Main.getPrefix() + args[2] + " is no Number!");
				return;
			}

			if (num >= varoPlayer.getStats().getStrikes().size()) {
				sender.sendMessage(Main.getPrefix() + "Strike " + args[2] + " not found!");
				return;
			}

			varoPlayer.getStats().removeStrike(varoPlayer.getStats().getStrikes().get(num));
			sender.sendMessage(Main.getPrefix() + "§7Du hast " + Main.getColorCode() + varoPlayer.getName() + " §7removed a strike! He still has " + Main.getColorCode() + varoPlayer.getStats().getStrikes().size() + " §7Strikes!");
		} else if (args[0].equalsIgnoreCase("list")) {
			if (args.length != 2) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/strike list §7<Spieler>");
				return;
			}

			VaroPlayer varoPlayer = VaroPlayer.getPlayer(args[1]);

			if (varoPlayer == null) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + args[1] + " §7not found!");
				return;
			}

			if (varoPlayer.getStats().getStrikes().size() < 1) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + varoPlayer.getName() + " §7has no Strikes!");
				return;
			}

			sender.sendMessage(Main.getPrefix() + "Strikes of " + Main.getColorCode() + varoPlayer.getName() + "§7:");
			for (Strike strike : varoPlayer.getStats().getStrikes()) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Strike Nr." + strike.getStrikeNumber() + "§8:");
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Reason: §7" + strike.getReason());
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Striker: §7" + strike.getStriker());
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Acquired: §7" + new SimpleDateFormat("dd:MM:yyy HH:mm").format(strike.getAcquiredDate()));
			}
		} else
			sender.sendMessage(Main.getPrefix() + "§7not found! " + Main.getColorCode() + "/strike §7for help.");
		return;
	}
}