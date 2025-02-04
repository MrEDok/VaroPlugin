package de.cuuky.varo.spigot.updater;

import de.cuuky.varo.Main;
import de.cuuky.varo.alert.Alert;
import de.cuuky.varo.alert.AlertType;
import de.cuuky.varo.spigot.updater.VaroUpdateResultSet.UpdateResult;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class VaroUpdater {

	private enum VersionCompareResult {
		VERSION1GREATER,
		VERSION2GREATER,
		VERSIONS_EQUAL;
	}

	private static final String UPDATE_LINK = "https://api.spiget.org/v2/resources/%version%/versions/latest";

	private VaroUpdateResultSet lastResult;
	private String updateLink, currentVersion;
	private int resourceId;
	private Runnable finishHook;

	public VaroUpdater(int resourceId, String currentVersion, Runnable finishedHook) {
		this.resourceId = resourceId;
		this.currentVersion = currentVersion;
		this.updateLink = UPDATE_LINK.replace("%version%", String.valueOf(resourceId));
		this.finishHook = finishedHook;

		checkUpdate();
	}

	private VersionCompareResult compareVersions(String version1, String version2) {
		try {
			String version1Use = version1.split("-BETA")[0], version2Use = version2.split("-BETA")[0];
			if (!version1Use.matches("[0-9]+(\\.[0-9]+)*")
					|| !version2Use.matches("[0-9]+(\\.[0-9]+)*"))
				throw new IllegalArgumentException("Invalid version format");

			String[] version1Parts = version1Use.split("\\.");
			String[] version2Parts = version2Use.split("\\.");

			for (int i = 0; i < Math.max(version1Parts.length, version2Parts.length); i++) {
				int version1Part = i < version1Parts.length ? Integer.parseInt(version1Parts[i]) : 0;
				int version2Part = i < version2Parts.length ? Integer.parseInt(version2Parts[i]) : 0;
				if (version1Part < version2Part)
					return VersionCompareResult.VERSION2GREATER;
				if (version1Part > version2Part)
					return VersionCompareResult.VERSION1GREATER;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(Main.getConsolePrefix() + "Failed to compare versions of plugin id " + resourceId);
			Main.getInstance().fail();
		}

		if (version1.contains("BETA")) return VersionCompareResult.VERSION2GREATER;
		if (version2.contains("BETA")) return VersionCompareResult.VERSION1GREATER;
		return VersionCompareResult.VERSIONS_EQUAL;
	}

	private void checkUpdate() {
		new BukkitRunnable() {
			@Override
			public void run() {
				checkForUpdates();
			}
		}.runTaskLaterAsynchronously(Main.getInstance(), 20L);
	}

	public void printResults() {
		if (this.lastResult == null)
			return;

		System.out.println(Main.getConsolePrefix() + "Updater: " + lastResult.getUpdateResult().getMessage());

		for (Alert upAlert : Alert.getAlerts(AlertType.UPDATE_AVAILABLE))
			if (upAlert.isOpen() && upAlert.getMessage().contains(lastResult.getVersionName()))
				return;

		if (lastResult.getUpdateResult() == UpdateResult.UPDATE_AVAILABLE)
			new Alert(AlertType.UPDATE_AVAILABLE, "§cA newer version of the plugin ( " + lastResult.getVersionName() + ") is available!\n§7Usually you can update without any loss of data\nwe recommend you reading the update logs anyway!");
	}

	public VaroUpdateResultSet checkForUpdates() {
		UpdateResult result = UpdateResult.NO_UPDATE;
		String version = "", id = "";

		try {
			Scanner scanner = new Scanner(new URL(updateLink).openStream());
			String all = "";
			while (scanner.hasNextLine()) {
				all += scanner.nextLine();
			}
			scanner.close();

			JSONObject scannerJSON = (JSONObject) JSONValue.parseWithException(all);
			version = scannerJSON.get("name").toString();
			id = scannerJSON.get("id").toString();
			switch (compareVersions(version, this.currentVersion)) {
			case VERSION1GREATER:
				result = UpdateResult.UPDATE_AVAILABLE;
				break;
			case VERSIONS_EQUAL:
				result = UpdateResult.NO_UPDATE;
				break;
			case VERSION2GREATER:
				result = UpdateResult.TEST_BUILD;
				break;
			}
		} catch (IOException e) {
			result = UpdateResult.FAIL_SPIGOT;
		} catch (ParseException | IllegalArgumentException e) {
			e.getSuppressed();
			System.out.println(Main.getConsolePrefix() + "Failed to fetch server version!");
		}

		this.lastResult = new VaroUpdateResultSet(result, version, id);

		if (finishHook != null)
			this.finishHook.run();

		return lastResult;
	}

	public VaroUpdateResultSet getLastResult() {
		return this.lastResult;
	}

	public int getResourceId() {
		return this.resourceId;
	}
}