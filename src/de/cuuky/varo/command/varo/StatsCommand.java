package de.cuuky.varo.command.varo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.player.stats.StatType;
import de.cuuky.varo.entity.player.stats.stat.PlayerState;

public class StatsCommand extends VaroCommand {

    public StatsCommand() {
        super("stats", "Bearbeiten von Stats", "varo.stats", "stat");
    }

    private String doCommand(Function<VaroPlayer, Boolean> execute, String[] args, int start) {
        List<VaroPlayer> players = new ArrayList<>();
        if (args[start].equals("@a"))
            players.addAll(VaroPlayer.getVaroPlayers());
        else {
            for (int i = start; i < args.length; i++) {
                VaroPlayer vp = VaroPlayer.getPlayer(args[i]);
                if (vp == null) continue;
                players.add(VaroPlayer.getPlayer(args[i]));
            }
        }

        if (players.isEmpty()) return null;
        for (VaroPlayer vp : players)
            if (!execute.apply(vp))
                return null;

        return players.stream().map(VaroPlayer::getName).collect(Collectors.joining(", "));
    }

    @Override
    public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label + " stats §7<Spieler>");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label +
                " stats set/add*/decrease* §7<Stat> <Value> <Spieler.../@a> | Sets a stat to a specific value");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label +
                " stats remove §7<Stat> <Spieler.../@a> | Resets one or all value(s)");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label +
                " stats reset §7<Spieler.../@a> | Reset alle Stats");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label +
                " stats odvReset §7<Spieler.../@a> | Resets everything except Kills, Wins, Rank, Team and YT-Link");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + " *Only usable for values that are a number");
            sender.sendMessage(Main.getPrefix() + " Tip: The '...' means that you can specify multiple players.");
            return;
        }

        if (args.length == 1) {
            VaroPlayer target;
            if ((target = VaroPlayer.getPlayer(args[0])) == null) {
                sender.sendMessage(Main.getPrefix() + "Player not found!");
                return;
            }

            sender.sendMessage(Main.getPrefix() + "Stats of " + Main.getColorCode() + target.getName() + "§7:");
            sender.sendMessage(Main.getPrefix());
            for (String stat : target.getStats().getStatsListed())
                sender.sendMessage(Main.getPrefix() + stat);
            return;
        }

        if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("add") ||
            args[0].equalsIgnoreCase("decrease")) {
            if (args.length == 2) {
                String stats = Arrays.stream(StatType.values()).map(StatType::getArg).collect(Collectors.joining(", "));
                sender.sendMessage(Main.getPrefix() + "Stats available: " + stats);
                return;
            }

            StatType type = StatType.getByName(args[1]);
            boolean set = !args[0].equalsIgnoreCase("remove");
            int min = set ? 4 : 3;
            if (set) {
                if (args.length < min) {
                    sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label +
                        " stats set/add/decrease §7<Stat...> <Value> <Spieler.../@a> | Sets a stat to a specific value");
                    return;
                }
            } else {
                if (args.length < min) {
                    sender.sendMessage(Main.getPrefix() + Main.getColorCode() + label +
                        " stats remove §7<Stat> <Spieler.../@a> | Resets one or all value(s)");
                    return;
                }
            }

            if (type == null) {
                sender.sendMessage(
                    Main.getPrefix() + "Stat '" + Main.getColorCode() + args[1] + "§7' could not be found!");
                return;
            }

            String found = this.doCommand((target) -> {
                String toSet = args[2];
                boolean add = args[0].equalsIgnoreCase("add"), dec = args[0].equalsIgnoreCase("decrease");
                if (add || dec) {
                    try {
                        int from = Integer.parseInt(String.valueOf(type.get(target))), calc =
                            Integer.parseInt(toSet) * (dec ? -1 : 1);
                        toSet = String.valueOf(from + calc);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Main.getPrefix() + "Could not calculate numbers!");
                        return false;
                    }
                }

                if (!args[0].equalsIgnoreCase("remove")) {
                    if (!type.execute(toSet, target)) {
                        sender.sendMessage(Main.getPrefix() + "§7The Value '" + Main.getColorCode() + toSet +
                            "§7' §7could not be for " + this + " set!");
                    }
                } else type.remove(target);
                return true;
            }, args, min - 1);

            if (found == null)
                sender.sendMessage(Main.getPrefix() + "No players found!");
            else
                sender.sendMessage(
                    Main.getPrefix() + "Stat updated for " + Main.getColorCode() + found);
        } else if (args[0].equalsIgnoreCase("reset")) {
            String found = this.doCommand((target) -> {
                target.getStats().loadDefaults();
                return true;
            }, args, 1);

            if (found == null)
                sender.sendMessage(Main.getPrefix() + "No players found!");
            else
                sender.sendMessage(
                    Main.getPrefix() + "Spieler " + Main.getColorCode() + found + "§7 successfully reset!");
        } else if (args[0].equalsIgnoreCase("odvreset")) {
            String found = this.doCommand((target) -> {
                target.getStats().loadStartDefaults();
                target.getStats().setState(PlayerState.ALIVE);
                if (target.getTeam() != null)
                    target.getTeam().removeMember(target);
                return true;
            }, args, 1);

            sender.sendMessage(
                Main.getPrefix() + "Player " + Main.getColorCode() + found + "§7 successfully ODV reset!");
        } else
            sender.sendMessage(
                Main.getPrefix() + "Not found! Type " + Main.getColorCode() + label + " stats §7for help.");
    }
}