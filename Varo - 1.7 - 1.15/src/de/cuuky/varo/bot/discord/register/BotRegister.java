package de.cuuky.varo.bot.discord.register;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import de.cuuky.varo.Main;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.configuration.configurations.messages.language.languages.defaults.ConfigMessages;
import de.cuuky.varo.entity.player.VaroPlayer;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class BotRegister {

	/*
	 * OLD CODE
	 */

	private static final String TABLE = "verify";

	private static ArrayList<BotRegister> register;

	static {
		register = new ArrayList<>();

		loadAll();
	}

	private boolean bypass;
	private int code;
	private long userId;
	private String uuid, name;

	public BotRegister(String uuid, boolean start) {
		this.uuid = uuid;
		this.userId = -1;
		this.code = -1;

		if (start)
			if (code == -1)
				this.code = generateCode();

		register.add(this);
	}

	public void delete() {
		if (getPlayer() != null)
			getPlayer().kickPlayer("§cBotRegister §7unregistered");

		register.remove(this);
	}

	public int generateCode() {
		int code = new Random().nextInt(1000000) + 1;
		for (BotRegister br : register)
			if (!br.equals(this))
				if (br.getCode() == code)
					return generateCode();

		return code;
	}

	public int getCode() {
		return this.code;
	}

	public String getKickMessage(VaroPlayer vp) {
		return ConfigMessages.BOTS_DISCORD_NOT_REGISTERED_DISCORD.getValue(vp, vp).replace("%code%", String.valueOf(getCode()));
	}

	public Member getMember() {
		try {
			return Main.getBotLauncher().getDiscordbot().getMainGuild().getMemberById(this.userId);
		} catch (Exception e) {
			return null;
		}
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(UUID.fromString(uuid));
	}

	public String getPlayerName() {
		return this.name;
	}

	public long getUserId() {
		return this.userId;
	}

	public String getUUID() {
		return this.uuid;
	}

	public boolean isActive() {
		if (bypass)
			return true;

		return userId > 0;
	}

	public boolean isBypass() {
		return bypass;
	}

	public void setBypass(boolean bypass) {
		this.bypass = bypass;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setPlayerName(String name) {
		this.name = name;
	}

	public void setUserId(long user) {
		this.userId = user;
	}

	private static void loadAll() {
		if (!ConfigSetting.DISCORDBOT_VERIFYSYSTEM.getValueAsBoolean())
			return;

		if (ConfigSetting.DISCORDBOT_USE_VERIFYSTSTEM_MYSQL.getValueAsBoolean()) {
			if (!Main.getDataManager().getMysqlClient().isConnected()) {
				System.err.println(Main.getConsolePrefix() + "Failed to load BotRegister!");
				return;
			}

			ResultSet rs = Main.getDataManager().getMysqlClient().getQuery("SELECT * FROM " + TABLE);

			try {
				while (rs.next()) {
					String uuid = rs.getString("uuid");
					BotRegister reg = new BotRegister(uuid, false);

					try {
						reg.setUserId(rs.getLong("userid"));
					} catch (Exception e) {
						reg.setUserId(-1);
					}

					reg.setCode(rs.getInt("code"));
					reg.setBypass(rs.getBoolean("bypass"));
					reg.setPlayerName(rs.getString("name"));

					Player player = Bukkit.getPlayer(UUID.fromString(uuid));
					if (player != null && !reg.isActive())
						player.kickPlayer(reg.getKickMessage(VaroPlayer.getPlayer(player)));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} else {
			File file = new File("plugins/Varo", "registrations.yml");
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

			for (String key : cfg.getKeys(true)) {
				if (!key.contains(".userId"))
					continue;

				String uuid = key.replace(".userId", "");
				BotRegister reg = new BotRegister(uuid, false);

				try {
					reg.setUserId(cfg.getLong(uuid + ".userId"));
				} catch (Exception e) {
					reg.setUserId(-1);
				}

				reg.setBypass(cfg.getBoolean(uuid + ".bypass"));
				reg.setCode(cfg.getInt(uuid + ".code"));
				reg.setPlayerName(cfg.getString(uuid + ".name"));

				Player player = Bukkit.getPlayer(UUID.fromString(uuid));
				if (player != null && !reg.isActive())
					player.kickPlayer(reg.getKickMessage(VaroPlayer.getPlayer(player)));
			}
		}
	}

	public static ArrayList<BotRegister> getBotRegister() {
		return register;
	}

	public static BotRegister getBotRegisterByPlayerName(String name) {
		for (BotRegister br : register)
			if (br.getPlayerName() != null)
				if (br.getPlayerName().equals(name))
					return br;
		return null;
	}

	public static BotRegister getRegister(String uuid) {
		for (BotRegister br : register)
			if (br.getUUID().equals(uuid))
				return br;

		return null;
	}

	public static BotRegister getRegister(User user) {
		for (BotRegister br : register)
			if (br.getUserId() == user.getIdLong())
				return br;
		return null;
	}

	public static void saveAll() {
		if (!ConfigSetting.DISCORDBOT_VERIFYSYSTEM.getValueAsBoolean())
			return;

		if (ConfigSetting.DISCORDBOT_USE_VERIFYSTSTEM_MYSQL.getValueAsBoolean()) {
			if (!Main.getDataManager().getMysqlClient().isConnected())
				return;

			Main.getDataManager().getMysqlClient().update("TRUNCATE TABLE " + TABLE + ";");

			for (final BotRegister reg : register) {
				Main.getDataManager().getMysqlClient().update("INSERT INTO " + TABLE + " (uuid, userid, code, bypass, name) VALUES ('" + reg.getUUID() + "', " + (reg.getUserId() != -1 ? reg.getUserId() : "null") + ", " + reg.getCode() + ", " + reg.isBypass() + ", '" + (reg.getPlayerName() == null ? "null" : reg.getPlayerName()) + "');");
			}
		} else {
			File file = new File("plugins/Varo", "registrations.yml");
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

			for (String s : cfg.getKeys(true))
				cfg.set(s, null);

			for (BotRegister reg : register) {
				cfg.set(reg.getUUID() + ".userId", reg.getUserId() != -1 ? reg.getUserId() : "null");
				cfg.set(reg.getUUID() + ".code", reg.getCode());
				cfg.set(reg.getUUID() + ".bypass", reg.isBypass());
				cfg.set(reg.getUUID() + ".name", reg.getPlayerName() == null ? "null" : reg.getPlayerName());
			}

			try {
				cfg.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}