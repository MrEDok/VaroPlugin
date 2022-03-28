package de.cuuky.varo.command.varo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.game.VaroGame;

public class StartCommand extends VaroCommand {

	public StartCommand() {
		super("start", "Start the Varo", "varo.start");
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		VaroGame game = Main.getVaroGame();
		if (game.isStarting()) {
			sender.sendMessage(Main.getPrefix() + "The game is already starting!");
			return;
		}

		if (game.hasStarted()) {
			sender.sendMessage(Main.getPrefix() + "The game has already been started!");
			return;
		}

		game.prepareStart();
		sender.sendMessage(Main.getPrefix() + "Game started successfully!");
	}
}