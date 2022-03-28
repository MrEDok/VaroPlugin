package de.cuuky.varo.command.varo;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.gui.items.ItemListInventory;
import de.cuuky.varo.list.VaroList;
import de.cuuky.varo.list.item.ItemList;

public class ItemCommand extends VaroCommand {

    public ItemCommand() {
        super("item", "Blocks or allows items", "varo.item", "itemlist", "items");
    }

    @Override
    public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
        if (vp == null) {
            sender.sendMessage(Main.getPrefix() + "You must be a player!");
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Main.getPrefix() + "§7----- " + Main.getColorCode() + "Item §7-----");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " item §7<itemlist> - Öffnet Inventar");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " item §7<itemlist> Add");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " item §7<itemlist> Remove");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " item §7<itemlist> list");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " item §7list");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Tipp: §7The /" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " enchant command blocks all enchantments that are on your current item.");
            sender.sendMessage(Main.getPrefix());
            sender.sendMessage(Main.getPrefix() + "§7This command adds the items to the lists you are holding.");
            sender.sendMessage(Main.getPrefix() + "§7-----------------");
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(Main.getPrefix() + "List of all " + Main.getColorCode() + "Itemlists§7:");
            for (VaroList list : ItemList.getItemLists())
                sender.sendMessage(Main.getPrefix() + Main.getColorCode() + list.getLocation());
            return;
        }

        ItemList list = ItemList.getItemList(args[0]);
        if (list == null) {
            sender.sendMessage(Main.getPrefix() + "List " + args[0] + " not found!");
            return;
        }

        Player player = vp.getPlayer();
        if (args.length == 1) {
            new ItemListInventory(player, list);
            return;
        }

        if (args[1].equalsIgnoreCase("list")) {
            if (list.getItems().size() < 1) {
                sender.sendMessage(Main.getPrefix() + "No items found!");
                return;
            }

            sender.sendMessage(Main.getPrefix() + "List of all items from " + Main.getColorCode() + list.getLocation() + "§7:");
            for (ItemStack stack : list.getItems()) sender.sendMessage(Main.getPrefix() + stack.toString());
            return;
        }

        ItemStack item = player.getItemInHand();
        if (item.getType() == Material.AIR) {
            sender.sendMessage(Main.getPrefix() + "You don't have an item in your hand!");
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            if (list.isUniqueType() && list.hasItem(item)) {
                sender.sendMessage(Main.getPrefix() + "An item cannot appear more than once on this list.\n" + Main.getPrefix() + "The item is already on this list.");
                return;
            }

            list.addItem(item);
            list.saveList();
            sender.sendMessage(Main.getPrefix() + "Item successful added to " + list.getLocation());
        } else if (args[1].equalsIgnoreCase("remove")) {
            if (!list.hasItem(item)) {
                sender.sendMessage(Main.getPrefix() + "Item is not on this list!");
                return;
            }

            while (list.hasItem(item)) list.removeItem(item);
            list.saveList();
            sender.sendMessage(Main.getPrefix() + "Item successfully completely removed from " + list.getLocation());
        } else {
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " item §7<itemlist> Add <Count>");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " item §7<itemlist> Remove [@a/Count]");
        }
    }
}