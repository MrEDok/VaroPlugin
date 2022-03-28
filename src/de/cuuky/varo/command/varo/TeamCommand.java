package de.cuuky.varo.command.varo;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.cuuky.cfw.utils.UUIDUtils;
import de.cuuky.cfw.utils.chat.PageableChatBuilder;
import de.cuuky.varo.Main;
import de.cuuky.varo.command.VaroChatListMessages;
import de.cuuky.varo.command.VaroCommand;
import de.cuuky.varo.configuration.configurations.config.ConfigSetting;
import de.cuuky.varo.entity.player.VaroPlayer;
import de.cuuky.varo.entity.team.VaroTeam;
import de.cuuky.varo.gui.team.TeamGUI;

public class TeamCommand extends VaroCommand {

    private PageableChatBuilder<VaroTeam> listBuilder;

    public TeamCommand() {
        super("team", "Main command for managing the teams", "varo.teams", "teams");

        this.listBuilder = new PageableChatBuilder<>(VaroTeam::getTeams)
                .messages(new VaroChatListMessages<>(team -> {
                    StringBuilder message = new StringBuilder();
                    message.append(Main.getPrefix() + Main.getColorCode() + " §l" +
                            team.getId() + "§7; " + Main.getColorCode() + team.getName() + "\n");
                    String list = new ArrayList<>(team.getMember()).stream()
                            .map(VaroPlayer::getName).collect(Collectors.joining(", "));
                    message.append(Main.getPrefix() + "  " + list + "\n");
                    message.append(Main.getPrefix());
                    return message.toString();
                }, "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team list", "List aller Teams"));
    }

    @Override
    public void onCommand(CommandSender sender, VaroPlayer vp, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Main.getPrefix() + Main.getProjectName() + " §7Team setup Befehle:");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team §7<Team/TeamID>");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team create §7<Team/TeamID> <Spieler 1, 2, 3...>");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team remove §7<Team/TeamID/Player/@a>");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team add §7<Team/TeamID> <Player>");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team rename §7<Team/TeamID> <Neuer Team-Name>");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team colorcode §7<Team/TeamID> remove/<Farbcode>");
            sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team list");
            return;
        }

        VaroTeam vteam = VaroTeam.getTeam(args[0]);
        if (vteam != null && sender instanceof Player) {
            new TeamGUI((Player) sender, vteam);
            return;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (!(args.length >= 2)) {
                sender.sendMessage(Main.getPrefix() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team create <Teamname> [Spieler 1, Spieler 2, Spieler 3...]");
                return;
            }

            if (!args[1].matches(VaroTeam.NAME_REGEX) || args[1].length() > ConfigSetting.TEAM_MAX_NAME_LENGTH.getValueAsInt()) {
                sender.sendMessage(Main.getPrefix() + "Invalid team name!");
                return;
            }

            VaroTeam team = VaroTeam.getTeam(args[1]);

            if (team != null) {
                boolean teamIdentical = true;
                for (int i = 2; i < args.length; i++) {
                    VaroPlayer player = VaroPlayer.getPlayer(args[i]);
                    if (!team.getMember().contains(player) || player == null) {
                        teamIdentical = false;
                    }
                }
                if (teamIdentical) {
                    sender.sendMessage(Main.getPrefix() + "This team is already registered.");
                } else {
                    sender.sendMessage(Main.getPrefix() + "§cThe team could not be registered, the team name is already occupied.");
                }
                return;
            }

            team = new VaroTeam(args[1]);
            sender.sendMessage(Main.getPrefix() + "Team " + Main.getColorCode() + team.getName() + " §7with the ID " + Main.getColorCode() + team.getId() + " §7created!");

            for (int i = 2; i < args.length; i++) {
                String arg = args[i];

                VaroPlayer varoplayer = VaroPlayer.getPlayer(arg);
                if (varoplayer == null) {
                    String uuid;
                    try {
                        uuid = Main.getInstance().getUUID(arg).toString();
                    } catch (Exception e) {
                        sender.sendMessage(Main.getPrefix() + "§c" + arg + " was not found.");
                        String newName;
                        try {
                            newName = UUIDUtils.getNamesChanged(arg);
                            sender.sendMessage(Main.getPrefix() + "§cEin Spieler, der in den letzten 30 Tagen " + arg + " hiess, hat sich in §7" + newName + " §cumbenannt.");
                            sender.sendMessage(Main.getPrefix() + "Benutze \"/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team add\", um diese Person einem Team hinzuzufuegen.");
                        } catch (Exception f) {
                            sender.sendMessage(Main.getPrefix() + "§cIn den letzten 30 Tagen gab es keinen Spieler mit diesem Namen.");
                        }
                        continue;
                    }

                    varoplayer = new VaroPlayer(arg, uuid);
                }

                team.addMember(varoplayer);
                sender.sendMessage(Main.getPrefix() + "Player " + Main.getColorCode() + varoplayer.getName() + " §7successfully added!");
            }
            return;
        } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
            if (args.length != 2) {
                sender.sendMessage(Main.getPrefix() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team remove <Team/TeamID/Player>");
                return;
            }

            VaroTeam team = VaroTeam.getTeam(args[1]);
            VaroPlayer varoplayer = VaroPlayer.getPlayer(args[1]);

            if (team != null) {
                team.delete();
                sender.sendMessage(Main.getPrefix() + "Team deleted");
            } else if (varoplayer != null) {
                varoplayer.getTeam().removeMember(varoplayer);
                sender.sendMessage(Main.getPrefix() + "Player " + Main.getColorCode() + varoplayer.getName() + " §7removed from his team!");
            } else if (args[1].equalsIgnoreCase("@a")) {
                while (VaroTeam.getTeams().size() > 0) {
                    VaroTeam.getTeams().get(0).delete();
                }
                sender.sendMessage(Main.getPrefix() + "All teams successfully deleted!");
            } else
                sender.sendMessage(Main.getPrefix() + "Team, TeamID or player not found!");
            return;
        } else if (args[0].equalsIgnoreCase("list")) {
            this.listBuilder.page(args.length >= 2 ? args[1] : "1").build().send(sender);
        } else if (args[0].equalsIgnoreCase("rename")) {
            if (args.length != 3) {
                sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team rename §7<Team/TeamID> <Team>");
                return;
            }

            if (!args[2].matches(VaroTeam.NAME_REGEX) || args[2].length() > ConfigSetting.TEAM_MAX_NAME_LENGTH.getValueAsInt()) {
                sender.sendMessage(Main.getPrefix() + "Invalid teamname");
                return;
            }

            VaroTeam team = VaroTeam.getTeam(args[1]);

            if (team == null) {
                sender.sendMessage(Main.getPrefix() + "Team not found!");
                return;
            }

            team.setName(args[2]);
            sender.sendMessage(Main.getPrefix() + "The team " + Main.getColorCode() + args[1] + " §7was renamed to " + Main.getColorCode() + team.getName());
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length != 3) {
                sender.sendMessage(Main.getPrefix() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team add <Team/TeamID> <Player>");
                return;
            }

            VaroPlayer varoplayer = VaroPlayer.getPlayer(args[2]);
            VaroTeam team = VaroTeam.getTeam(args[1]);

            if (team == null) {
                sender.sendMessage(Main.getPrefix() + "Team not found!");
                return;
            }

            if (varoplayer == null) {
                String uuid = null;
                try {
                    uuid = Main.getInstance().getUUID(args[2]).toString();
                } catch (Exception e) {
                    sender.sendMessage(Main.getPrefix() + args[2] + " has no minecraft account lul");
                    return;
                }

                varoplayer = new VaroPlayer(args[2], uuid);
            }

            if (varoplayer.getTeam() != null) {
                if (varoplayer.getTeam().equals(team)) {
                    sender.sendMessage(Main.getPrefix() + "This player is already in this team!");
                    return;
                }

                varoplayer.getTeam().removeMember(varoplayer);
                sender.sendMessage(Main.getPrefix() + Main.getColorCode() + varoplayer.getName() + " §7Has been removed from his current team!");
            }

            team.addMember(varoplayer);
            sender.sendMessage(Main.getPrefix() + "Player " + Main.getColorCode() + varoplayer.getName() + " §7successfully moved into the team " + Main.getColorCode() + team.getName());
            return;
        } else if (args[0].equalsIgnoreCase("colorcode")) {
            if (args.length != 3) {
                sender.sendMessage(Main.getPrefix() + Main.getColorCode() + "/" + ConfigSetting.COMMAND_VARO_NAME.getValueAsString() + " team colorcode §7<Team/TeamID> remove/<Farbcode>");
                return;
            }

            VaroTeam team = VaroTeam.getTeam(args[1]);
            if (team == null) {
                sender.sendMessage(Main.getPrefix() + "Team not found!");
                return;
            }

            if (args[2].equalsIgnoreCase("remove")) {
                team.setColorCode(null);
                sender.sendMessage(Main.getPrefix() + "Team color code from team " + team.getDisplay() + " §7removed");
                return;
            }

            team.setColorCode(args[2]);
            sender.sendMessage(Main.getPrefix() + "Team color code from team " + team.getDisplay() + " §7changed");
        } else
            sender.sendMessage(Main.getPrefix() + "§7Command not found! " + Main.getColorCode() + "/team");
        return;
    }
}
