package de.cuuky.varo.command.varo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.cuuky.cfw.utils.LocationFormat;
import de.cuuky.cfw.version.types.Materials;
import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.game.world.generators.SpawnGenerator;
import de.cuuky.varo.spawns.Spawn;

public class SpawnsCommand extends VaroCommand {

    public SpawnsCommand() {
        super("spawns", "Main command for the spawns in which players spawn", "varo.spawns", "spawnholes", "spawn", "holes");
    }

    @Override
    public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "§lSpawn Command§7§l:");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns§7 set <Count/Players>");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns§7 delete <Count/Players> - (Deletes the spawn entry and the spawn in the world)");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns§7 player <Count> <set/remove> [Players/@a]");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns§7 list");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns§7 generate <radius>/auto <amount>/player/team [Half-Step-Materiall] [Side-Block-Material]");
            sender.sendMessage(Main.getPrefix() + "------");
            sender.sendMessage(Main.getPrefix() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns generate can generate your spawns either by number or by the registered teams or players.\n" + "The difference between 'player' and 'team' is that with 'team' the teams are also considered in the sorting.");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Example for number: §7/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns generate 30 40 STONE_SLAB");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Example for players: §7/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns generate auto team");
            sender.sendMessage(Main.getPrefix() + "------");
            return;
        }

        if (args[0].equalsIgnoreCase("generate")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Main.getPrefix() + "You must be a player!");
                return;
            }

            if (args.length < 3) {
                sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns§7 generate <radius>/auto <amount>/player/team [Half-Step-Materiall] [Side-Block-Material]");
                return;
            }

            String material = null;
            if (args.length >= 4)
                material = args[3];

            String sideBlockMaterial = null;
            if (args.length >= 5)
                sideBlockMaterial = args[4];

            if (material != null && Materials.fromString(material) == null || sideBlockMaterial != null && Materials.fromString(sideBlockMaterial) == null ) {
                sender.sendMessage(Main.getPrefix() + "ID's of the blocks not found!");
                return;
            }

            try {
                Integer.valueOf(args[1]);
            } catch (Exception e) {
                try {
                    if (args[1].equalsIgnoreCase("auto") || args[1].equalsIgnoreCase("automatic")) {
                        if (args[2].equalsIgnoreCase("player") || args[2].equalsIgnoreCase("team"))
                            args[1] = String.valueOf((int) (VaroPlayer.getAlivePlayer().size() * 0.85));
                        else
                            args[1] = String.valueOf((int) (Integer.parseInt(args[2]) * 0.85));
                    }
                } catch (Exception e1) {
                    sender.sendMessage(Main.getPrefix() + "An error occurred while creating the spawns! Correct values specified?");
                    e1.printStackTrace();
                    return;
                }
            }

            try {
                if (args[2].equalsIgnoreCase("player") || args[2].equalsIgnoreCase("team"))
                    new SpawnGenerator(((Player) sender).getLocation(), Integer.parseInt(args[1]), args[2].equalsIgnoreCase("team"), material, sideBlockMaterial);
                else
                    new SpawnGenerator(((Player) sender).getLocation(), Integer.parseInt(args[1]), Integer.parseInt(args[2]), material, sideBlockMaterial);
            } catch (Exception e) {
                sender.sendMessage(Main.getPrefix() + "An error occurred while creating the spawns! Correct values specified?");
                e.printStackTrace();
                return;
            }

            sender.sendMessage(Main.getPrefix() + "§7DSpawns were created with " + Main.getColorCode() + args[2] + "§7radius " + Main.getColorCode() + args[1] + "§7Block-Material " + Main.getColorCode() + (args.length >= 4 ? args[3] : "STONE_BRICK_SLAB") + " §7and the side block material " + Main.getColorCode() + (args.length >= 5 ? args[4] : "DIRT"));
        } else if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("place")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Main.getPrefix() + "Not for console!");
                return;
            }

            Player player = (Player) sender;
            if (args.length == 1) {
                Spawn spawn = new Spawn(player.getLocation());
                player.sendMessage(Main.getPrefix() + Main.getColorCode() + "Spawn " + spawn.getNumber() + " §7created successfully!");
            } else if (args.length == 2) {
                int spawnNumber = -1;
                try {
                    spawnNumber = Integer.parseInt(args[1]);
                    if (!(spawnNumber > 0)) {
                        player.sendMessage(Main.getPrefix() + "Spawn number must be positive!");
                        return;
                    }
                } catch (NumberFormatException ignored) {
                }

                if (spawnNumber != -1) {
                    Spawn oldSpawn = Spawn.getSpawn(spawnNumber);
                    if (oldSpawn != null) {
                        oldSpawn.delete();
                        sender.sendMessage(Main.getPrefix() + "The old spawn with the ID " + Main.getColorCode() + spawnNumber + " §7was removed to make way for the new one.");
                    }

                    Spawn spawn = new Spawn(spawnNumber, player.getLocation());
                    sender.sendMessage(Main.getPrefix() + "Spawn " + Main.getColorCode() + spawn.getNumber() + " §7set!");
                } else {
                    VaroPlayer varoplayer = VaroPlayer.getPlayer(args[1]);
                    if (varoplayer == null) {
                        sender.sendMessage(Main.getPrefix() + "Player or number not valid!");
                        return;
                    }

                    Spawn oldSpawn = Spawn.getSpawn(varoplayer);
                    if (oldSpawn != null) {
                        oldSpawn.delete();
                        sender.sendMessage(Main.getPrefix() + "The old spawn with the ID " + Main.getColorCode() + oldSpawn.getNumber() + " §7was removed to make way for the new one.");
                    }

                    Spawn spawn = new Spawn(varoplayer, player.getLocation());
                    sender.sendMessage(Main.getPrefix() + "Playerspawn " + Main.getColorCode() + spawn.getNumber() + " §7for Player " + Main.getColorCode() + spawn.getPlayer().getName() + " §7set!");
                }
            } else
                sender.sendMessage(Main.getPrefix() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns set [Count/Player]");
        } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Main.getPrefix() + "Not for console!");
                return;
            }

            if (args.length != 2) {
                sender.sendMessage(Main.getPrefix() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns " + args[0] + " [Count/Player/@a]");
                return;
            }

            if (args[1].equalsIgnoreCase("@a")) {
                for (Spawn spawn : Spawn.getSpawnsClone())
                    spawn.delete();

                sender.sendMessage(Main.getPrefix() + "All spawns successfully removed!");
                return;
            }

            Player player = (Player) sender;
            int spawnNumber = -1;
            try {
                spawnNumber = Integer.parseInt(args[1]);
                if (!(spawnNumber > 0)) {
                    player.sendMessage(Main.getPrefix() + "Spawn number must be positive!");
                    return;
                }
            } catch (NumberFormatException ignored) {
            }

            Spawn spawn;
            if (spawnNumber != -1) {
                spawn = Spawn.getSpawn(spawnNumber);
                if (spawn == null) {
                    sender.sendMessage(Main.getPrefix() + "Spawn with the ID" + Main.getColorCode() + spawnNumber + " not found!");
                    return;
                }

                spawn.delete();
                sender.sendMessage(Main.getPrefix() + "Spawn " + Main.getColorCode() + spawn.getNumber() + " §7removed!");
            } else {
                VaroPlayer varoplayer = VaroPlayer.getPlayer(args[1]);
                if (varoplayer == null) {
                    sender.sendMessage(Main.getPrefix() + "Player or number not valid!");
                    return;
                }

                spawn = Spawn.getSpawn(varoplayer);
                if (spawn == null) {
                    sender.sendMessage(Main.getPrefix() + "Spawn from the player " + Main.getColorCode() + varoplayer.getName() + " not found!");
                    return;
                }

                sender.sendMessage(Main.getPrefix() + "Spawn of " + Main.getColorCode() + varoplayer.getName() + " §7removed!");
            }
            spawn.delete();
        } else if (args[0].equalsIgnoreCase("player")) {
            if (args.length < 3) {
                sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns§7 player <Number/@a> <set/remove> [Player]");
                return;
            }

            int spawnNumber = -1;
            try {
                spawnNumber = Integer.parseInt(args[1]);
                if (!(spawnNumber > 0)) {
                    sender.sendMessage(Main.getPrefix() + "Spawn number must be positive!");
                    return;
                }
            } catch (NumberFormatException ignored) {
            }

            Spawn spawn = Spawn.getSpawn(spawnNumber);
            if (spawn == null && (!args[1].equals("@a") && args[2].equalsIgnoreCase("set"))) {
                sender.sendMessage(Main.getPrefix() + "Spawn could not be found!");
                return;
            }

            if (args[2].equalsIgnoreCase("set")) {
                if (args.length < 4) {
                    sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns§7 player <Number> set [Player]");
                    return;
                }

                VaroPlayer varoplayer = VaroPlayer.getPlayer(args[3]);
                if (varoplayer == null) {
                    sender.sendMessage(Main.getPrefix() + "Player or number not valid!");
                    return;
                }

                spawn.setPlayer(varoplayer);
                sender.sendMessage(Main.getPrefix() + "Spawn players " + Main.getColorCode() + spawn.getNumber() + " §7set to " + Main.getColorCode() + spawn.getPlayer().getName());
            } else if (args[2].equalsIgnoreCase("remove")) {
                if (args[1].equals("@a")) {
                    for (Spawn spaw : Spawn.getSpawns())
                        spaw.setPlayer(null);

                    sender.sendMessage(Main.getPrefix() + "Player removed from all spawns!");
                    return;
                }

                spawn.setPlayer(null);

                sender.sendMessage(Main.getPrefix() + "Spawn players " + Main.getColorCode() + spawn.getNumber() + " §7successfully removed!");
            }

        } else if (args[0].equalsIgnoreCase("list")) {
            if (Spawn.getSpawns().isEmpty()) {
                sender.sendMessage(Main.getPrefix() + "No spawns found!");
                return;
            }

            sender.sendMessage(Main.getPrefix() + "§lList of all " + Main.getColorCode() + "§lSpawns§7§l:");
            for (Spawn spawn : Spawn.getSpawns()) {
                sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "Spawn " + spawn.getNumber() + "§7: ");
                sender.sendMessage(Main.getPrefix() + "§7Location: " + new LocationFormat(spawn.getLocation()).format("§7X§8: " + Main.getColorCode() + "x §7Y§8: " + Main.getColorCode() + "y §7Z§8: " + Main.getColorCode() + "z §7in " + Main.getColorCode() + "world"));
                sender.sendMessage(Main.getPrefix() + "§7Player: " + Main.getColorCode() + (spawn.getPlayer() != null ? spawn.getPlayer().getName() : "none"));
                sender.sendMessage(Main.getPrefix());
            }
        } else
            sender.sendMessage(Main.getPrefix() + "Not found! /" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " spawns");
    }
}