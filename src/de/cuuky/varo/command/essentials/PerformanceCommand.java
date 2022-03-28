package de.cuuky.varo.command.essentials;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import de.cuuky.varo.Main;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.configuration.configurations.language.languages.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.threads.LagCounter;

public class PerformanceCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		VaroPlayer vp = (sender instanceof Player ? VaroPlayer.getPlayer((Player) sender) : null);
		if (!sender.hasPermission("varo.performance")) {
			sender.sendMessage(ConfigMessages.NOPERMISSION_NO_PERMISSION.getValue(vp));
			return false;
		}

		if (args.length == 0) {
			sender.sendMessage(Main.getPrefix() + "§c§lPerformance Command§7§l:");
			sender.sendMessage(Main.getPrefix() + "§c/performance clear §8- §7Enables a RAM-Cleaner");
			sender.sendMessage(Main.getPrefix() + "§c/performance help §8- §7Shows methods for Performanceimprovements");
			sender.sendMessage(Main.getPrefix() + "§c/performance entityclear §8- §7Removes all items from floor etc. (exept player, armorstands, animals)");
			sender.sendMessage(Main.getPrefix() + "§cTIPP: §7/usage zeigt die Ausnutzung deines Servers");
			return false;
		}

		if (args[0].equalsIgnoreCase("improve") || args[0].equalsIgnoreCase("clear")) {
			Runtime r = Runtime.getRuntime();
			double ramUsage = (r.totalMemory() - r.freeMemory()) / 1048576;
			System.gc();
			double ramCleared = ramUsage - (r.totalMemory() - r.freeMemory()) / 1048576;
			sender.sendMessage(Main.getPrefix() + "RAM was cleared §c" + ramCleared);
		} else if (args[0].equalsIgnoreCase("help")) {
			sender.sendMessage(Main.getPrefix() + "Current TPS: §c" + Math.round(LagCounter.getTPS()) + "§7 - Normal §c18-20 §7TPS");

			sender.sendMessage(Main.getPrefix() + "The following settings may decrease performance - turning them off may increase performance:");
			sender.sendMessage(Main.getPrefix());
			for (ConfigSetting ce : ConfigSetting.values())
				if (ce.isReducingPerformance() && (ce.getValue() instanceof Boolean && ce.getValueAsBoolean()))
					sender.sendMessage(Main.getPrefix() + "- §7The Setting §c" + ce.getPath() + " §7decreases the performance");

			int entities = 0;
			for (World world : Bukkit.getWorlds())
				for (Entity entity : world.getEntities())
					if (!(entity.getType().toString().contains("ARMOR_STAND")) && !(entity instanceof LivingEntity))
						entities++;

			sender.sendMessage(Main.getPrefix() + "There are §c" + entities + " §7Entities (except players, ArmorStands, animals) loaded - removing all non-players could increase performance");
			sender.sendMessage(Main.getPrefix() + "There are §c" + Bukkit.getPluginManager().getPlugins().length + " §7Plugins activated - please remove all unnecessary");
		} else if (args[0].equalsIgnoreCase("entityclear")) {
			for (World world : Bukkit.getWorlds())
				for (Entity entity : world.getEntities())
					if (!(entity.getType().toString().contains("ARMOR_STAND")) && !(entity instanceof LivingEntity))
						entity.remove();

			sender.sendMessage(Main.getPrefix() + "All non- player,animal or ArmorStands cleared!");
		}

		return false;
	}
}