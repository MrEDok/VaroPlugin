package de.cuuky.varo.command.varo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.configuration.configurations.language.languages.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.game.world.setup.AutoSetup;

public class AutoSetupCommand extends VaroCommand {

	public AutoSetupCommand() {
		super("autosetup", "Automatically sets the server up", "varo.autosetup");
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("run")) {
				if (!ConfigSetting.AUTOSETUP_ENABLED.getValueAsBoolean()) {
					sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSETUP_NOT_SETUP_YET.getValue(vp));
					return;
				}

				new AutoSetup(()-> {
					for (VaroPlayer player : VaroPlayer.getOnlinePlayer())
						player.saveTeleport(Main.getVaroGame().getVaroWorldHandler().getTeleportLocation());

					sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSETUP_FINISHED.getValue(vp));
				});
				return;
			}
		}

		sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSETUP_HELP.getValue(vp));
		sender.sendMessage(Main.getPrefix() + ConfigMessages.VARO_COMMANDS_AUTOSETUP_ATTENTION.getValue(vp));
	}
}