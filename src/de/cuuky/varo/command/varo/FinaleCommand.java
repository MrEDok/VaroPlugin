package de.cuuky.varo.command.varo;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.player.stats.stat.PlayerState;
import de.cuuky.varo.game.start.ProtectionTime;
import de.cuuky.varo.listener.helper.cancelable.CancelAbleType;
import de.cuuky.varo.listener.helper.cancelable.VaroCancelAble;
import de.cuuky.varo.logger.logger.EventLogger.LogType;

public class FinaleCommand extends VaroCommand {

	private enum FinalState {
		COUNTDOWN_PHASE,
		JOIN_PHASE,
		NONE,
		STARTED
	}

	private BukkitTask startScheduler;
	private int countdown;

	private FinalState status;

	public FinaleCommand() {
		super("finale", "Main command for managing the finals", "varo.finale");

		this.status = FinalState.NONE;
	}

	private void finaleStart() {
		status = FinalState.STARTED;

		Bukkit.broadcastMessage(Main.getPrefix() + "§cDAS FINALS STARTED!");
		if (ConfigSetting.FINALE_PROTECTION_TIME.getValueAsInt() > 0) {
			Bukkit.broadcastMessage(Main.getPrefix() + "§7There are " + ConfigSetting.FINALE_PROTECTION_TIME.getValueAsInt() + " Seconds protection time.");
			Main.getVaroGame().setProtection(new ProtectionTime(ConfigSetting.FINALE_PROTECTION_TIME.getValueAsInt()));
		} else {
			Bukkit.broadcastMessage(Main.getPrefix() + "§7There is no protection time");
		}

		for (VaroPlayer player : VaroPlayer.getVaroPlayers()) {
			VaroCancelAble.removeCancelAble(player, CancelAbleType.FREEZE);
			if (player.getPlayer() != null) {
				if (player.getPlayer().isOnline()) {
					player.saveTeleport(Main.getVaroGame().getVaroWorldHandler().getMainWorld().getWorld().getSpawnLocation());
					continue;
				}
			}
			if (ConfigSetting.PLAYER_SPECTATE_IN_FINALE.getValueAsBoolean()) {
				player.getStats().setState(PlayerState.SPECTATOR);
			} else {
				player.getStats().setState(PlayerState.DEAD);
			}
		}

		Main.getVaroGame().getVaroWorldHandler().setBorderSize(ConfigSetting.BORDER_SIZE_IN_FINALE.getValueAsInt(), 0, null);
		Main.getVaroGame().setFinaleJoinStart(false);

		int playerNumber = VaroPlayer.getOnlinePlayer().size();
		Main.getDataManager().getVaroLoggerManager().getEventLogger().println(LogType.ALERT, "THE FINALS STARTET!\nThere are " + playerNumber + "players competing.");
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		if (args.length == 0 || (!args[0].equalsIgnoreCase("joinstart") && !args[0].equalsIgnoreCase("hauptstart") && !args[0].equalsIgnoreCase("abort") && !args[0].equalsIgnoreCase("abbruch") && !!args[0].equalsIgnoreCase("abbrechen") && !!args[0].equalsIgnoreCase("stop"))) {
			sender.sendMessage(Main.getPrefix() + Main.getProjectName() + " §7Finale Befehle:");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " finals joinStart");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " finals hauptStart [Countdown]");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " finals abort");
			return;
		}

		if (args[0].equalsIgnoreCase("joinstart")) {
			if (status == FinalState.JOIN_PHASE) {
				sender.sendMessage(Main.getPrefix() + "The JoinStart has already been activated.");
				return;
			} else if (status == FinalState.COUNTDOWN_PHASE) {
				sender.sendMessage(Main.getPrefix() + "The final countdown has already been activated.");
				return;
			} else if (status == FinalState.STARTED) {
				sender.sendMessage(Main.getPrefix() + "The final has already been started.");
				return;
			}

			for (VaroPlayer player : VaroPlayer.getOnlineAndAlivePlayer()) {
				Player pl = player.getPlayer();
				if (pl.isOp()) {
					continue;
				}

				new VaroCancelAble(CancelAbleType.FREEZE, player);

				if (pl.isOnline())
					player.sendMessage(Main.getPrefix() + "The finals will start soon. Until the start of the final, all have been frozen.");
			}

			Main.getVaroGame().setFinaleJoinStart(true);
			status = FinalState.JOIN_PHASE;
			ConfigSetting.PLAY_TIME.setValue(-1, true);

			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Now everyone can join the final.");
			sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "It is recommended to wait at least 5 minutes before starting the final.");
			sender.sendMessage(Main.getPrefix() + "§c§lWARNING: §cWhen starting with §7§l/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " finals mainStart§7 all players who are not online will be killed.");

			Main.getDataManager().getVaroLoggerManager().getEventLogger().println(LogType.ALERT, "You can now join the finals!");

			return;
		} else if (args[0].equalsIgnoreCase("hauptstart") || args[0].equalsIgnoreCase("mainstart")) {
			if (status == FinalState.NONE) {
				sender.sendMessage(Main.getPrefix() + "The join start has not yet been activated. This must be done before the main start.");
				return;
			} else if (status == FinalState.COUNTDOWN_PHASE) {
				sender.sendMessage(Main.getPrefix() + "The final countdown is already running.");
				return;
			} else if (status == FinalState.STARTED) {
				sender.sendMessage(Main.getPrefix() + "The final has already been started.");
				return;
			}

			countdown = 0;
			if (args.length != 1) {
				try {
					countdown = Integer.parseInt(args[1]);
				} catch (NumberFormatException e) {
					countdown = 0;
				}
			}
			if (countdown != 0) {
				status = FinalState.COUNTDOWN_PHASE;
				startScheduler = new BukkitRunnable() {
					@Override
					public void run() {
						if (countdown != 0) {
							Bukkit.broadcastMessage(Main.getPrefix() + "Finals start in " + countdown + " seconds!");
						} else {
							finaleStart();
							startScheduler.cancel();
						}
						countdown--;
					}
				}.runTaskTimer(Main.getInstance(), 0L, 20L);
			} else {
				finaleStart();
			}

			return;
		} else if (args[0].equalsIgnoreCase("abort") || args[0].equalsIgnoreCase("abbruch") || args[0].equalsIgnoreCase("abbrechen") || args[0].equalsIgnoreCase("stop")) {
			if (status == FinalState.NONE || status == FinalState.JOIN_PHASE) {
				sender.sendMessage(Main.getPrefix() + "There is no countdown to abort.");
				return;
			} else if (status == FinalState.STARTED) {
				sender.sendMessage(Main.getPrefix() + "The final has already been started.");
				return;
			}

			startScheduler.cancel();
			status = FinalState.JOIN_PHASE;
			Bukkit.broadcastMessage("§7The final start was §ccanceled§7!");
		}
	}
}