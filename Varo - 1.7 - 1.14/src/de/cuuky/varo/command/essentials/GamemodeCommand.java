package de.cuuky.varo.command.essentials;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.cuuky.varo.Main;
import de.cuuky.varo.config.messages.ConfigMessages;
import de.cuuky.varo.version.BukkitVersion;
import de.cuuky.varo.version.VersionUtils;

public class GamemodeCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("varo.gamemode")) {
			sender.sendMessage(ConfigMessages.OTHER_NO_PERMISSION.getValue());
			return false;
		}

		Player player;
		if(args.length <= 2 && args.length != 0) {
			if(args.length == 1) {
				if(!(sender instanceof Player)) {
					sender.sendMessage(Main.getPrefix() + "§7/gamemode [Player/@a]");
					return false;
				}
				player = (Player) sender;

			} else if(args[1].equalsIgnoreCase("@a")) {
				player = null;
			} else {
				player = Bukkit.getPlayerExact(args[1]);
				if(player == null) {
					sender.sendMessage(Main.getPrefix() + "§7Spieler " + args[1] + "§7 nicht gefunden.");
					return false;
				}
			}

			int mode = 0;
			try {
				mode = Integer.valueOf(args[0]);
			} catch(Exception e) {
				sender.sendMessage(Main.getPrefix() + "§7Du hast keinen gültigen Gamemode angegeben!");
				return false;
			}

			GameMode gm;
			switch(mode) {
			case 0:
				gm = GameMode.SURVIVAL;
				break;
			case 1:
				gm = GameMode.CREATIVE;
				break;
			case 2:
				gm = GameMode.ADVENTURE;
				break;
			case 3:
				if(!VersionUtils.getVersion().isHigherThan(BukkitVersion.ONE_7)) {
					sender.sendMessage(Main.getPrefix() + "Nicht verfügbar vor der 1.8!");
					return false;
				}

				gm = GameMode.valueOf("SPECTATOR"); // Damit keine IDE Fehler
				// bei 1.7 kommen
				break;
			default:
				sender.sendMessage(Main.getPrefix() + "§7Die Zahl muss 0-3 betragen!");
				return false;
			}

			if(player != null) {
				player.setGameMode(gm);

				if(args.length == 1) {
					sender.sendMessage(Main.getPrefix() + "§7Du bist nun im Gamemode " + Main.getColorCode() + gm.toString() + "§7!");
				} else {
					sender.sendMessage(Main.getPrefix() + "§7" + player.getName() + " ist nun im Gamemode " + Main.getColorCode() + "" + gm.toString() + "§7!");
				}
			} else {
				for(Player p : Bukkit.getOnlinePlayers()) {
					p.setGameMode(gm);
				}
				sender.sendMessage(Main.getPrefix() + "§7Alle Spieler sind nun im Gamemode " + Main.getColorCode() + gm.toString() + "§7!");
			}

		} else
			sender.sendMessage(Main.getPrefix() + "§7/gamemode <Mode> [Player]");

		return false;
	}
}