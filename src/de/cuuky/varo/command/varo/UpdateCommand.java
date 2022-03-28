package de.cuuky.varo.command.varo;

import java.io.File;
import java.net.URLDecoder;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.recovery.recoveries.VaroBackup;
import de.cuuky.varo.spigot.FileDownloader;
import de.cuuky.varo.spigot.updater.VaroUpdateResultSet;
import de.cuuky.varo.spigot.updater.VaroUpdateResultSet.UpdateResult;

public class UpdateCommand extends VaroCommand {

	private String oldFileName;
	private boolean pluginNameChanged, resetOldDirectory;

	public UpdateCommand() {
		super("update", "Automatically installs the latest version", "varo.update");
	}

	private void deleteDirectory(File file) {
		for (File listFile : file.listFiles()) {
			if (listFile.isDirectory())
				deleteDirectory(listFile);

			listFile.delete();
		}

		file.delete();
	}

	private void update(CommandSender sender, VaroUpdateResultSet resultSet) {
		// Step 1: Download new Version
		try {
			FileDownloader fd = new FileDownloader("http://api.spiget.org/v2/resources/" + Main.getResourceId() + "/download", "plugins/update/" + this.oldFileName);

			sender.sendMessage(Main.getPrefix() + "Start Download...");

			fd.startDownload();
		} catch (Exception e) {
			sender.sendMessage(Main.getPrefix() + "§cThere was a critical error when downloading the plugin.");
			sender.sendMessage(Main.getPrefix() + "§7Recommended is a manual update of the plugin: https://www.spigotmc.org/resources/71075/");
			System.out.println("There was a critical error when downloading the plugin");
			System.out.println("---------- Stack Trace ----------");
			e.printStackTrace();
			System.out.println("---------- Stack Trace ----------");
			return;
		}

		sender.sendMessage(Main.getPrefix() + "Update successfully installed");

		// Step 2: Deleting old directory if wanted
		if (resetOldDirectory) {
			System.out.println("The directory of the old plugin version will be deleted.");
			File directory = new File("plugins/Varo/");
			deleteDirectory(directory);
		}
//
//		// Step 3: Deleting old Version if existing
//		if (this.pluginNameChanged) {
//			System.out.println("Da sich der Pluginname veraendert hat, wird die alte Pluginversion geloescht.");
//			File oldPlugin = new File("plugins/" + this.oldFileName);
//		}

		Bukkit.getServer().shutdown();
	}

	@Override
	public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
		VaroUpdateResultSet resultSet = Main.getVaroUpdater().checkForUpdates();
		UpdateResult result = resultSet.getUpdateResult();
		String updateVersion = resultSet.getVersionName();

		if (args.length == 0 || (!args[0].equalsIgnoreCase("normal") && !args[0].equalsIgnoreCase("reset"))) {
			if (result == UpdateResult.UPDATE_AVAILABLE) {
				sender.sendMessage(Main.getPrefix() + "§c A newer version exists: " + updateVersion);
				sender.sendMessage("");
				sender.sendMessage(Main.getPrefix() + "§7§lUpdate Befehle:");
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " update normal §7- Updates the version, but keeps all data");
				sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " update reset §7- Updates the version and deletes all data");
				sender.sendMessage(Main.getPrefix() + "§cWichtig: §7The updater spiget.org sometimes has an old version as download. If the version has not changed after the update, you have to update manually.");
			} else {
				sender.sendMessage(Main.getPrefix() + "No new version was found. If this is an error, update manually.");
			}
			return;
		}

		if (args[0].equalsIgnoreCase("normal")) {
			resetOldDirectory = false;
		} else if (args[0].equalsIgnoreCase("reset")) {
			resetOldDirectory = true;
		}

		this.pluginNameChanged = false;
		this.oldFileName = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();

		try {
			oldFileName = URLDecoder.decode(oldFileName, "UTF-8");
		} catch (Exception e) {
			oldFileName = oldFileName.replace("%20", " ");
		}

		if (!this.oldFileName.equals(Main.getInstance().getDescription().getName() + ".jar"))
			this.pluginNameChanged = true;

		Main.getDataManager().setDoSave(false);

		if (result == UpdateResult.UPDATE_AVAILABLE) {
			sender.sendMessage(Main.getPrefix() + "§7Update wird installiert...");
			sender.sendMessage(Main.getPrefix() + "§7Backup wird created...");
			new VaroBackup();
			sender.sendMessage(Main.getPrefix() + "§7Under some circumstances the latest version is not downloaded, if this is the case, please install the new version manually.");
			update(sender, resultSet);
		} else {
			sender.sendMessage(Main.getPrefix() + "§7The plugin is already up to date!");
		}
	}
}