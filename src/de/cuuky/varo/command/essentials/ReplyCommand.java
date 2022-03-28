package de.cuuky.varo.command.essentials;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.cuuky.cfw.utils.JavaUtils;
import de.cuuky.varo.Main;

public class ReplyCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// VaroPlayer vp = (sender instanceof Player ?
		// VaroPlayer.getPlayer((Player) sender) : null);
		if (args.length == 0) {
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/r §7<Message>");
			return false;
		}

		if (!MessageCommand.lastChat.containsKey(sender.getName())) {
			sender.sendMessage(Main.getPrefix() + "Last chat " + Main.getColorCode() + "not §7found.");
			return false;
		}

		String to1 = MessageCommand.lastChat.get(sender.getName());
		Player to = Bukkit.getPlayerExact(to1);

		if (to == null) {
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + to1 + " §7is no longer online!");
			return false;
		}

		String message = JavaUtils.getArgsToString(args, " ");
		to.sendMessage(Main.getColorCode() + sender.getName() + " §8-> §7You§8: §f" + message);
		sender.sendMessage("§7You §8-> " + Main.getColorCode() + to.getName() + "§8: §f" + message);
		if (MessageCommand.lastChat.containsKey(to.getName()))
			MessageCommand.lastChat.remove(to.getName());

		MessageCommand.lastChat.put(to.getName(), sender.getName());
		return false;
	}
}