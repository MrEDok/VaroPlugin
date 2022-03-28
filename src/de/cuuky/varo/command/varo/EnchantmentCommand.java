package de.cuuky.varo.command.varo;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.list.enchantment.EnchantmentList;

public class EnchantmentCommand extends VaroCommand {

	public EnchantmentCommand() {
		super("enchantment", "Enchantment list settings", "varo.enchantment", "enchantments", "enchant", "enchants");
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		if (vp == null) {
			sender.sendMessage(Main.getPrefix() + "You must be a player!");
			return;
		}

		if (args.length == 0) {
			sender.sendMessage(Main.getPrefix() + "§7----- " + Main.getColorCode() + "Enchantments §7-----");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " enchantment §7<enchantmentlist> [Remove / Add / List]");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " item §7list");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Tipp: §7The /" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " item command blocks all items");
			sender.sendMessage(Main.getPrefix());
			sender.sendMessage(Main.getPrefix() + "§7This command adds all enchantments of the item you are holding to the list.");
			sender.sendMessage(Main.getPrefix() + "§7Alternatively books are also possible");
			sender.sendMessage(Main.getPrefix() + "§7--------------------");
			return;
		}

		if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			sender.sendMessage(Main.getPrefix() + "List of all " + Main.getColorCode() + "Enchantmentlist§7:");
			for (EnchantmentList list : EnchantmentList.getEnchantmentLists())
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + list.getLocation());
			return;
		}

		if (args.length < 2) {
			sender.sendMessage(Main.getPrefix() + "False arguments! " + Main.getColorCode() + label + " enchant");
			return;
		}

		EnchantmentList list = EnchantmentList.getEnchantmentList(args[0]);
		if (list == null) {
			sender.sendMessage(Main.getPrefix() + "List " + args[0] + " not found");
			return;
		}

		if (args[1].equalsIgnoreCase("list")) {
			if (list.getEnchantments().size() < 1) {
				sender.sendMessage(Main.getPrefix() + "No enchantments found!");
				return;
			}

			sender.sendMessage(Main.getPrefix() + "List of all enchantments from " + Main.getColorCode() + list.getLocation() + "§7:");
			for (String enc1 : list.getEnchantments())
				sender.sendMessage(Main.getPrefix() + enc1);
			return;
		}

		Player player = vp.getPlayer();
		if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
			sender.sendMessage(Main.getPrefix() + "You don't have an item in your hand!");
			return;
		}

		ItemStack item = player.getItemInHand();
		Map<Enchantment, Integer> encs = null;
		if (item.getItemMeta() instanceof EnchantmentStorageMeta) {
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
			encs = meta.getStoredEnchants();
		} else
			encs = item.getItemMeta().getEnchants();

		if (encs.size() == 0) {
			sender.sendMessage(Main.getPrefix() + "No enchantments were found on your item/book!");
			return;
		}

		for (Enchantment enc : encs.keySet()) {
			if (args[1].contains("add")) {
				if (list.hasEnchantment(enc, encs.get(enc))) {
					sender.sendMessage(Main.getPrefix() + "Enchant '" + enc.getName() + " (" + encs.get(enc) + ")' is already on this list!");
					return;
				}

				list.addEnchantment(enc, encs.get(enc));
				sender.sendMessage(Main.getPrefix() + "Enchant " + enc.getName() + " (" + encs.get(enc) + ") successfully added to " + list.getLocation());
			} else if (args[1].equalsIgnoreCase("remove")) {
				if (!list.hasEnchantment(enc, encs.get(enc))) {
					sender.sendMessage(Main.getPrefix() + "Enchant '" + enc.getName() + " (" + encs.get(enc) + ")' is not on this list!");
					return;
				}

				list.removeEnchantment(enc, encs.get(enc));
				sender.sendMessage(Main.getPrefix() + "Enchant " + enc.getName() + " (" + encs.get(enc) + ") successfully removed from " + list.getLocation());
			} else if (args[1].equalsIgnoreCase("list")) {
				sender.sendMessage(Main.getPrefix() + "List of all enchantments from " + Main.getColorCode() + list.getLocation() + "§7:");
				for (String enc1 : list.getEnchantments())
					sender.sendMessage(Main.getPrefix() + enc1);
			} else
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " enchantment §7<enchantmentlist> [Remove / Add / List]");
		}
	}
}