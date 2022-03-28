package de.cuuky.varo.command.varo;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.game.world.generators.LobbyGenerator;

public class LobbyCommand extends VaroCommand {

	private ArrayList<UUID> uuid;

	public LobbyCommand() {
		super("lobby", "Lobby settings", "varo.lobby");

		uuid = new ArrayList<>();
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Main.getPrefix() + "§7Only for Players");
			return;
		}

		if (args.length == 0) {
			sender.sendMessage(Main.getPrefix() + "§7------ " + Main.getColorCode() + "Lobby §7------");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/lobby build §7<size> <height>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/lobby setSpawn");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/lobby removeSpawn");
			sender.sendMessage(Main.getPrefix() + "§7-----------------------");
			return;
		}

		if (args[0].equalsIgnoreCase("build") || args[0].equalsIgnoreCase("create")) {
			if (args.length != 3) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/lobby build §7<size> <height>");
				return;
			}

			Player p = (Player) sender;
			if (!uuid.contains(p.getUniqueId())) {
				sender.sendMessage(Main.getPrefix() + "§7This command will create a " + Main.getColorCode() + "Lobby §7around you, you cannot undo this");
				sender.sendMessage(Main.getPrefix() + "For best result use this command " + Main.getColorCode() + "high in the air.");
				sender.sendMessage(Main.getPrefix() + "Reenter command to " + Main.getColorCode() + "confirm§7.");
				uuid.add(p.getUniqueId());
				return;
			}

			int height = 12;
			int size = 25;
			try {
				size = Integer.valueOf(args[1]);
				height = Integer.valueOf(args[2]);
			} catch (Exception e) {
				sender.sendMessage(Main.getPrefix() + "§7The height and the size must be a number!");
				return;
			}

			uuid.remove(p.getUniqueId());
			Main.getVaroGame().setLobby(p.getLocation());
			new LobbyGenerator(p.getLocation(), height, size);
		} else if (args[0].equalsIgnoreCase("setSpawn") || args[0].equalsIgnoreCase("set")) {
			Main.getVaroGame().setLobby(((Player) sender).getLocation());
			sender.sendMessage(Main.getPrefix() + "§7Location for " + Main.getColorCode() + "Lobby §7successfully set!");
			return;
		} else if (args[0].equalsIgnoreCase("removeSpawn") || args[0].equalsIgnoreCase("remove")) {
			Main.getVaroGame().setLobby(null);
			sender.sendMessage(Main.getPrefix() + "§7Location for " + Main.getColorCode() + "Lobby §7successfully removed!");
			return;
		} else
			sender.sendMessage(Main.getPrefix() + "§7Not found. §7Type /lobby for help.");
	}
}