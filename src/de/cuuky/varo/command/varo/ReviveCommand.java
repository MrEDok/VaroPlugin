package de.cuuky.varo.command.varo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.player.stats.stat.PlayerState;

public class ReviveCommand extends VaroCommand {

	public ReviveCommand() {
		super("revive", "Revives a player", "varo.revive", "unkill");
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " <Player> - Revives a player");
			return;
		}

		VaroPlayer target = VaroPlayer.getPlayer(args[0]);
		if (target == null) {
			sender.sendMessage(Main.getPrefix() + "Player could not be found!");
			return;
		}

		if (target.getStats().isAlive()) {
			sender.sendMessage(Main.getPrefix() + "This player is already alive!");
			return;
		}

		target.getStats().setState(PlayerState.ALIVE);

		sender.sendMessage(Main.getPrefix() + "Player successfully revived!");
	}
}