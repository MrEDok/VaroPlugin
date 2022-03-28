package de.cuuky.varo.command.essentials;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.cuuky.varo.Main;
import de.cuuky.varo.configuration.configurations.language.languages.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;

public class SpeedCommand implements CommandExecutor {

	private float getRealMoveSpeed(final float userSpeed, final boolean isFly) {
		float defaultSpeed = isFly ? 0.1f : 0.2f;
		float maxSpeed = 1f;

		if (userSpeed < 1f) {
			return defaultSpeed * userSpeed;
		} else {
			float ratio = ((userSpeed - 1) / 9) * (maxSpeed - defaultSpeed);
			return ratio + defaultSpeed;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		VaroPlayer vp = (sender instanceof Player ? VaroPlayer.getPlayer((Player) sender) : null);
		if (!sender.hasPermission("varo.speed")) {
			sender.sendMessage(ConfigMessages.NOPERMISSION_NO_PERMISSION.getValue(vp));
			return false;
		}

		if (args.length == 1) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Main.getPrefix() + "§7Either '/speed <Speed> [Player/@a]' oder be a player!");
				return false;
			}

			Player p = (Player) sender;
			Float speed;
			try {
				speed = Float.valueOf(args[0]);
				speed = getRealMoveSpeed(Float.valueOf(args[0]), p.isFlying());
			} catch (Exception e) {
				sender.sendMessage(Main.getPrefix() + "§7You did not give a valid §bSpeed");
				return false;
			}

			if (Float.valueOf(args[0]) > 10 || Float.valueOf(args[0]) < 0) {
				sender.sendMessage(Main.getPrefix() + "§7The speed must be 0-10!");
				return false;
			}

			if (p.isFlying())
				p.setFlySpeed(speed);
			else
				p.setWalkSpeed(speed);
			sender.sendMessage(Main.getPrefix() + "§7Your " + Main.getColorCode() + (p.isFlying() ? "flight" : "walk") + "-Speed §7is now " + args[0] + "!");
		} else if (args.length == 2) {
			try {
				if (Float.valueOf(args[0]) > 10 || Float.valueOf(args[0]) < 0) {
					sender.sendMessage(Main.getPrefix() + "§7The speed must be 0-10!");
					return false;
				}
			} catch (Exception e) {
				sender.sendMessage(Main.getPrefix() + "§7You did not give a valid §bSpeed");
				return false;
			}

			if (args[1].equalsIgnoreCase("@a")) {
				for (Player pl : Bukkit.getOnlinePlayers()) {
					Float speed = null;
					try {
						speed = Float.valueOf(args[0]);
						speed = getRealMoveSpeed(Float.valueOf(args[0]), pl.isFlying());
					} catch (Exception e) {
						sender.sendMessage(Main.getPrefix() + "§7You did not give a valid Speed");
						return false;
					}

					if (pl.isFlying())
						pl.setFlySpeed(speed);
					else
						pl.setWalkSpeed(speed);
				}
				sender.sendMessage(Main.getPrefix() + "§7Speed successfully set for all!");
				return false;
			}

			Player to = Bukkit.getPlayerExact(args[1]);
			if (to == null) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + args[1] + "§7 not found!");
				return false;
			}

			Float speed = null;
			try {
				speed = Float.valueOf(args[0]);
				speed = getRealMoveSpeed(Float.valueOf(args[0]), to.isFlying());
			} catch (Exception e) {
				sender.sendMessage(Main.getPrefix() + "§7You did not give a valid Speed");
				return false;
			}

			if (to.isFlying())
				to.setFlySpeed(speed);
			else
				to.setWalkSpeed(speed);
			sender.sendMessage(Main.getPrefix() + "§7" + to.getName() + "'s " + Main.getColorCode() + (to.isFlying() ? "flight" : "walk") + "-Speed §7is now " + args[0] + "!");
		} else
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/speed §7<Speed> [Player]");
		return false;
	}
}
