package de.cuuky.varo.command.varo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.configuration.configurations.language.languages.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.game.start.AutoStart;

public class AutoStartCommand extends VaroCommand {

	/*
	 * OLD CODE
	 */

	public AutoStartCommand() {
		super("autostart", "Starts the Varo automatically", "varo.autostart", "as");
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		if (Main.getVaroGame().hasStarted()) {
			sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_ALREADY_STARTED.getValue(vp));
			return;
		}

		if (args.length == 0) {
			sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_HELP_HEADER.getValue(vp).replace("%category%", "Autostart"));
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " autostart §7info");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " autostart §7set <Hour> <Minute> <Day> <Month> <Year>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " autostart §7remove");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " autostart §7delay <Minutes>");
			sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_HELP_FOOTER.getValue(vp));
			return;
		}

		if (args[0].equalsIgnoreCase("set")) {
			if (Main.getVaroGame().getAutoStart() != null) {
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_ALREADY_SETUP.getValue(vp));
				return;
			}

			if (args.length != 6) {
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_HELP_SET.getValue(vp));
				return;
			}

			if (args[5].length() == 2)
				args[5] = 20 + args[5];

			int min, hour, day, month, year;
			try {
				min = Integer.parseInt(args[2]);
				hour = Integer.parseInt(args[1]);
				day = Integer.parseInt(args[3]);
				month = Integer.parseInt(args[4]) - 1;
				year = Integer.parseInt(args[5]);
			} catch (NumberFormatException e) {
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_NO_NUMBER.getValue(vp));
				return;
			}

			Calendar start = new GregorianCalendar(year, month, day, hour, min, 0);
			if (new GregorianCalendar().after(start)) {
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_DATE_IN_THE_PAST.getValue(vp));
				return;
			}

			Main.getVaroGame().setAutoStart(new AutoStart(start));
			return;
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (Main.getVaroGame().getAutoStart() == null) {
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_NOT_SETUP_YET.getValue(vp));
				return;
			}

			Main.getVaroGame().getAutoStart().stop();
			sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_REMOVED.getValue(vp));
		} else if (args[0].equalsIgnoreCase("delay")) {
			if (Main.getVaroGame().getAutoStart() == null) {
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_NOT_SETUP_YET.getValue(vp));
				return;
			}

			if (args.length < 2) {
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_DELAY_HELP.getValue(vp));
				return;
			}

			int delay = -1;
			try {
				delay = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_ERROR_NO_NUMBER.getValue(vp).replace("%text%", args[1]));
				return;
			}

			if (delay < 1) {
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_DELAY_TO_SMALL.getValue(vp));
				return;
			}

			Main.getVaroGame().getAutoStart().delay(delay);
			sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_START_DELAYED.getValue(vp).replace("%delay%", String.valueOf(delay)));
		} else if (args[0].equalsIgnoreCase("info")) {
			if (Main.getVaroGame().getAutoStart() == null)
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_INFO_NOT_ACTIVE.getValue(vp));
			else {
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_INFO_ACTIVE.getValue(vp));
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_INFO_DATE.getValue(vp).replace("%date%", new SimpleDateFormat("dd.MM.yyyy HH.mm").format(Main.getVaroGame().getAutoStart().getStart().toString())));
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_INFO_ACTIVE.getValue(vp).replace("%active%", String.valueOf(ConfigSetting.DO_SORT_AT_START.getValueAsBoolean())));
				sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSTART_INFO_ACTIVE.getValue(vp).replace("%teamsize%", String.valueOf(ConfigSetting.DO_RANDOMTEAM_AT_START.getValueAsInt())));
			}
		} else
			sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_ERROR_USAGE.getValue(vp).replace("%command%", "autostart"));
		return;

	}
}