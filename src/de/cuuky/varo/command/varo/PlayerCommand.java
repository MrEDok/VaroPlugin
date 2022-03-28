package de.cuuky.varo.command.varo;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.cuuky.cfw.utils.UUIDUtils;
import de.cuuky.cfw.utils.chat.PageableChatBuilder;
import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroChatListMessages;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.configuration.configurations.language.languages.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.player.stats.stat.PlayerState;
import de.cuuky.varo.gui.player.PlayerGUI;

public class PlayerCommand extends VaroCommand {

	private PageableChatBuilder<VaroPlayer> listBuilder;

	public PlayerCommand() {
		super("player", "Manages the players", "varo.player");

		this.listBuilder = new PageableChatBuilder<>(VaroPlayer::getVaroPlayers)
				.messages(new VaroChatListMessages<>(player ->
						Main.getPrefix() + Main.getColorCode() + "§l" + (player.getId() + 1) + "§7: " + Main.getColorCode() + player.getName(),
						"/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " player list", "List aller Spieler"));
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(Main.getPrefix() + "§7----- " + Main.getColorCode() + "Player §7-----");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " player §7<Spieler>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " player add §7<Spieler1> <Spieler2> ...");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " player remove §7<Spieler / @a>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " player respawn §7<Spieler / @a>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " player kill §7<Spieler / @a>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " player reset §7<Spieler / @a>");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " player list");
			sender.sendMessage(Main.getPrefix() + "§7------------------");
			return;
		}

		if (args.length == 1 && VaroPlayer.getPlayer(args[0]) != null) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Main.getPrefix() + "§7You must be a player to use this command!");
				return;
			}

			VaroPlayer vps = VaroPlayer.getPlayer(args[0]);
			if (vps == null) {
				sender.sendMessage(Main.getPrefix() + "Player not found!");
				return;
			}

			new PlayerGUI((Player) sender, vps);
			return;
		}

		VaroPlayer vps = null;
		if (args.length > 1)
			vps = VaroPlayer.getPlayer(args[1]);

		if (args[0].equalsIgnoreCase("reset")) {
			if (args.length >= 2 && args[1].equalsIgnoreCase("@a")) {
				for (VaroPlayer pl : VaroPlayer.getVaroPlayers()) {
					if (pl.isOnline())
						pl.getPlayer().kickPlayer("§cYour account has been reset!\n§7Join again to register.");
					pl.getStats().loadDefaults();
					if (pl.getTeam() != null)
						pl.getTeam().removeMember(pl);
				}
				return;
			}

			if (vps == null) {
				sender.sendMessage(Main.getPrefix() + "§7Player not found!");
				return;
			}

			if (vps.isOnline())
				vps.getPlayer().kickPlayer("§7Your account has been reset!\nJoin again to register.");

			if (vps.isOnline())
				vps.getPlayer().kickPlayer("§cYour account has been reset!\n§7Join again to register.");
			vps.getStats().loadDefaults();
			if (vps.getTeam() != null)
				vps.getTeam().removeMember(vps);
			sender.sendMessage(Main.getPrefix() + "§7Account of §c" + vps.getName() + " §7was successfully reset!");
			return;
		} else if (args[0].equalsIgnoreCase("kill")) {
			if (args.length >= 2 && args[1].equalsIgnoreCase("@a")) {
				for (VaroPlayer pl : VaroPlayer.getVaroPlayers())
					if (pl.isOnline())
						pl.getPlayer().setHealth(0);
					else
						pl.getStats().setState(PlayerState.DEAD);
				return;
			}

			if (vps == null) {
				sender.sendMessage(Main.getPrefix() + "§7Player not found!");
				return;
			}

			if (vps.getStats().getState() == PlayerState.DEAD) {
				sender.sendMessage(Main.getPrefix() + "This player is already dead!");
				return;
			}

			if (vps.isOnline())
				vps.getPlayer().setHealth(0);
			else
				vps.getStats().setState(PlayerState.DEAD);

			sender.sendMessage(Main.getPrefix() + "§7" + vps.getName() + " §7successfully killed!");
			return;
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length >= 2 && args[1].equalsIgnoreCase("@a")) {
				for (VaroPlayer pl : new ArrayList<>(VaroPlayer.getVaroPlayers())) {
					if (pl.isOnline())
						pl.getPlayer().kickPlayer(ConfigMessages.JOIN_KICK_NOT_USER_OF_PROJECT.getValue(vp));

					pl.delete();
				}
				return;
			}

			if (vps == null) {
				sender.sendMessage(Main.getPrefix() + "§7Player not found!");
				return;
			}

			if (vps.isOnline())
				vps.getPlayer().kickPlayer(ConfigMessages.JOIN_KICK_NOT_USER_OF_PROJECT.getValue(vp));

			vps.delete();
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + args[1] + " §7was successfully removed from " + Main.getColorCode() + Main.getProjectName());
			return;
		} else if (args[0].equalsIgnoreCase("add")) {
			if (args.length <= 1) {
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " player add §7<Player1> <Player2> ...");
				return;
			}

			for (String arg : args) {
				if (arg.equals(args[0]))
					continue;

				if (VaroPlayer.getPlayer(arg) != null) {
					sender.sendMessage(Main.getPrefix() + Main.getColorCode() + arg + " §7already exists!");
					continue;
				}

				String uuid;
				try {
					uuid = Main.getInstance().getUUID(arg).toString();
				} catch (Exception e) {
					sender.sendMessage(Main.getPrefix() + "§c" + arg + " not found!");
					String newName;
					try {
						newName = UUIDUtils.getNamesChanged(arg);
						sender.sendMessage(Main.getPrefix() + "§cA player who in the last 30 days was named " + arg + " is now: §7" + newName);
						sender.sendMessage(Main.getPrefix() + "Use \"/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team add\", to add this person to a team.");
					} catch (Exception f) {
						sender.sendMessage(Main.getPrefix() + "§cIn the last 30 days there was no player with this name.");
					}
					continue;
				}

				new VaroPlayer(arg, uuid);
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + arg + " §7was successfully added to " + Main.getColorCode() + Main.getProjectName());
			}
		} else if (args[0].equalsIgnoreCase("respawn")) {
			if (args.length >= 2 && args[1].equalsIgnoreCase("@a")) {
				for (VaroPlayer pl : VaroPlayer.getVaroPlayers())
					pl.getStats().setState(PlayerState.ALIVE);
				sender.sendMessage(Main.getPrefix() + "§7Successfully revived all players!");
				return;
			}

			if (vps == null) {
				sender.sendMessage(Main.getPrefix() + "§7Player not found!");
				return;
			}

			if (vps.getStats().isAlive()) {
				sender.sendMessage(Main.getPrefix() + "§a" + vps.getName() + " §7already lives!");
				return;
			}

			vps.getStats().setState(PlayerState.ALIVE);
			sender.sendMessage(Main.getPrefix() + "§a" + vps.getName() + " §7successfully revived!");
			return;
		} else if (args[0].equalsIgnoreCase("list")) {
            this.listBuilder.page(args.length >= 2 ? args[1] : "1").build().send(sender);
		} else
			sender.sendMessage(Main.getPrefix() + "§7Player/Command not found! §7Type /player for more.");
		return;
	}
}