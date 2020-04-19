package de.cuuky.varo.gui.team;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import de.cuuky.cfw.item.ItemBuilder;
import de.cuuky.cfw.menu.SuperInventory;
import de.cuuky.cfw.menu.utils.PageAction;
import de.cuuky.varo.Main;
import de.cuuky.varo.entity.team.VaroTeam;

public class TeamGUI extends SuperInventory {

	private VaroTeam team;

	public TeamGUI(Player opener, VaroTeam team) {
		super("§7Team §2" + team.getId(), opener, 9, false);

		this.team = team;
		this.setModifier = true;
		Main.getCuukyFrameWork().getInventoryManager().registerInventory(this);
		open();
	}

	@Override
	public boolean onBackClick() {
		return false;
	}

	@Override
	public void onClick(InventoryClickEvent event) {}

	@Override
	public void onClose(InventoryCloseEvent event) {}

	@Override
	public void onInventoryAction(PageAction action) {}

	@Override
	public boolean onOpen() {
		linkItemTo(1, new ItemBuilder().displayname("§cRemove").itemstack(new ItemStack(Material.BUCKET)).build(), new Runnable() {

			@Override
			public void run() {
				team.delete();
			}
		});
		return true;
	}
}
