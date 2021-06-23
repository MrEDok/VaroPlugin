package de.cuuky.varo.gui.admin.discordbot.botregister;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import de.cuuky.cfw.item.ItemBuilder;
import de.cuuky.cfw.menu.utils.PageAction;
import de.cuuky.varo.Main;
import de.cuuky.varo.bot.discord.register.BotRegister;
import de.cuuky.varo.gui.VaroSuperInventory;

public class BotRegisterGUI extends VaroSuperInventory {

	private BotRegister register;

	public BotRegisterGUI(Player opener, BotRegister register) {
		super("§7BotRegister: §a" + register.getPlayerName(), opener, 18, false);

		this.register = register;
		this.setModifier = true;
		Main.getCuukyFrameWork().getInventoryManager().registerInventory(this);
		open();
	}

	@Override
	public boolean onBackClick() {
		new BotRegisterListGUI(opener);
		return true;
	}

	@Override
	public void onClick(InventoryClickEvent event) {}

	@Override
	public void onClose(InventoryCloseEvent event) {}

	@Override
	public void onInventoryAction(PageAction action) {}

	@Override
	public boolean onOpen() {
		linkItemTo(1, new ItemBuilder().displayname("§4Delete").itemstack(new ItemStack(Material.REDSTONE)).build(), new Runnable() {

			@Override
			public void run() {
				register.delete();
			}
		});

		linkItemTo(4, new ItemBuilder().displayname("§cUnregister").itemstack(new ItemStack(Material.COAL)).build(), new Runnable() {

			@Override
			public void run() {
				register.setUserId(-1);
			}
		});

		linkItemTo(7, new ItemBuilder().displayname((register.isBypass() ? "§cRemove" : "§aAllow") + " §7Bypass").itemstack(new ItemStack(register.isBypass() ? Material.ANVIL : Material.EMERALD)).build(), new Runnable() {

			@Override
			public void run() {
				register.setBypass(!register.isBypass());
			}
		});
		return true;
	}
}
