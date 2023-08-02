package fr.sunshinedev.endercubecmw.commands.executors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.sunshinedev.endercubecmw.api.CMWMobCustom;
import fr.sunshinedev.endercubecmw.managers.MobsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.api.CMWKit;
import fr.sunshinedev.endercubecmw.api.CMWPlayer;
import fr.sunshinedev.endercubecmw.api.CMWTeam;
import fr.sunshinedev.endercubecmw.managers.KitsManager;
import fr.sunshinedev.endercubecmw.managers.TeamsManager;

public class TeamExecutor extends IExecutor {
	public TeamExecutor() {
		super("team", "ecmw.admin.team", new String[] {});
	}

	public TeamExecutor(String name, String permission, String[] aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void run(CommandExecutor command, Player playerSender, String[] args) {
		super.run(command, playerSender, args);

		if (args.length > 0) {
			String action = args[0];
			if (action.equalsIgnoreCase("create")) {
				if (args.length > 1) {
					String name = args[1];
					if(TeamsManager.getTeamConfig(name).isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Une équipe porte déjà ce nom.");
						return;
					}

					TeamsManager.saveTeam(new CMWTeam(name));
					Utils.sendPlayerMessageSuccess(playerSender, "L'équipe %s a bien été sauvegardée".formatted(name));
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw team create <name>");
				}
			} else if (action.equalsIgnoreCase("remove")) {
				if (args.length > 1) {
					String name = args[1];

					Optional<CMWTeam> teamOpt = TeamsManager.getTeamConfig(name);
					if(!teamOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "L'équipe %s n'existe pas :'(".formatted(name));
						return;
					}

					TeamsManager.remove(teamOpt.get());
					
					Utils.sendPlayerMessageSuccess(playerSender, "L'équipe %s a bien été retiré à la partie".formatted(name));
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw team remove <name>");
				}
			} else if (action.equalsIgnoreCase("addPlayer")) {
				if (args.length > 2) {
					if (EnderCubeCMW.INSTANCE.getGame() == null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
						return;
					}
					String team = args[1];
					String player = args[2];

					Optional<CMWTeam> teamTarget = TeamsManager.getTeam(team);
					if (teamTarget.isPresent()) {
						CMWTeam cmwTeam = teamTarget.get();
						Player playerTarget = Bukkit.getPlayer(player);
						if (playerTarget == null) {
							Utils.sendPlayerMessageError(playerSender, "Le joueur n'est plus en ligne :(");
							return;
						}
						if (TeamsManager.checkPlayerIsInTeam(playerTarget)) {
							Utils.sendPlayerMessageError(playerSender, "Ce joueur est déjà dans une équipe %s".formatted(cmwTeam.getChatName()));
							return;
						}
						if (cmwTeam.getPlayers().stream().anyMatch(cmwPlayer -> cmwPlayer.getName().equalsIgnoreCase(playerTarget.getName()))) {
							Utils.sendPlayerMessageError(playerSender, "Ce joueur est déjà dans l'équipe %s".formatted(cmwTeam.getChatName()));
							return;
						}
						cmwTeam.addPlayer(new CMWPlayer(playerTarget));
						Utils.sendPlayerMessageSuccess(playerSender, "%s a bien été ajouté à l'équipe %s"
								.formatted(playerTarget.getName(), cmwTeam.getChatName()));

						EnderCubeCMW.INSTANCE.getLogger().info("Players Count: " + cmwTeam.getPlayers().size());
					} else {
						Utils.sendPlayerMessageError(playerSender, "L'équipe n'existe pas");
					}
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw team add <team> <player>");
				}
			} else if (action.equalsIgnoreCase("removePlayer")) {
				if (args.length > 2) {
					if (EnderCubeCMW.INSTANCE.getGame() == null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
						return;
					}
					String team = args[1];
					String player = args[2];

					Optional<CMWTeam> teamTarget = TeamsManager.getTeam(team);
					if (!teamTarget.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "L'équipe n'existe pas");
						return;
					}
					CMWTeam cmwTeam = teamTarget.get();
					Optional<CMWPlayer> cmwPlayerOpt = cmwTeam.getPlayers().stream().filter(cmwPlayerCheck -> cmwPlayerCheck.getName().equalsIgnoreCase(player)).findFirst();
					if(cmwPlayerOpt.isEmpty()) {
						Utils.sendPlayerMessageError(playerSender, "Le joueur ne fais pas partie de l'équipe");
						return;
					}
					cmwTeam.removePlayer(cmwPlayerOpt.get());
					Utils.sendPlayerMessageSuccess(playerSender, "Le joueur a été retiré de l'équipe");
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw team removePlayer <team> <player>");
				}
			} else if (action.equalsIgnoreCase("setKit")) {
				if (args.length > 2) {
					String team = args[1];
					String kit = args[2];

					Optional<CMWTeam> teamTarget = TeamsManager.getTeamFromConfig(team);
					Optional<CMWKit> kitTarget = KitsManager.getKitFromName(kit);
					if (!teamTarget.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "L'équipe n'existe pas");
						return;
					}
					if (!kitTarget.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Le kit n'existe pas");
						return;
					}

					CMWTeam cmwTeam = teamTarget.get();
					CMWKit cmwKit = kitTarget.get();
					TeamsManager.attributeKit(cmwTeam, cmwKit);
					Utils.sendPlayerMessageSuccess(playerSender, "Le kit %s a bien été attribué à l'équipe %s"
							.formatted(cmwKit.getName(), cmwTeam.getChatName()));
					EnderCubeCMW.INSTANCE.getLogger().info("Kit attribute team: " + cmwTeam.getKit());
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw team setkit <team> <kit>");
				}
			} else if (action.equalsIgnoreCase("setColor")) {
				if (args.length > 2) {
					String team = args[1];
					String color = args[2];

					Optional<CMWTeam> teamTarget = TeamsManager.getTeamFromConfig(team);
					if (!teamTarget.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "L'équipe n'existe pas");
						return;
					}
					if (!Utils.checkStringIfColorHex(color)) {
						Utils.sendPlayerMessageError(playerSender, "La couleur hex ne semble pas valide.");
						return;
					}

					CMWTeam cmwTeam = teamTarget.get();
					TeamsManager.setColor(cmwTeam, color);
					Utils.sendPlayerMessageSuccess(playerSender, "La couleur %s a bien été attribué à l'équipe %s"
							.formatted(color, cmwTeam.getChatName()));
					EnderCubeCMW.INSTANCE.getLogger().info("Color attribute team: " + cmwTeam.getColor());
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw team setColor <team> <colorHex>");
				}
			}
		} else {
			Utils.sendPlayerMessageError(playerSender, "/cmw team <create|setKit|addPlayer|remove|removePlayer|setColor>");
		}
	}

	@Override
	public @Nullable List<String> tabCompleter(@NotNull CommandSender sender, @NotNull Command cmd,
			@NotNull String alias, @NotNull String[] args) {
		if (args.length == 1)
			return Arrays.asList("create", "setKit", "addPlayer", "setColor", "removePlayer", "remove").stream().filter(s -> s.startsWith(args[0])).toList();;

		if (args[0].equalsIgnoreCase("addPlayer")) {
			if (args.length == 2) {
				if (EnderCubeCMW.INSTANCE.getGame() == null)
					return null;
				return EnderCubeCMW.INSTANCE.getGame().getTeams().stream().map(CMWTeam::getName)
						.collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		} else if (args[0].equalsIgnoreCase("removePlayer")) {
			if (args.length == 2) {
				if (EnderCubeCMW.INSTANCE.getGame() == null)
					return null;
				return EnderCubeCMW.INSTANCE.getGame().getTeams().stream().map(CMWTeam::getName)
						.collect(Collectors.toList());
			} else if (args.length == 3) {
				Optional<CMWTeam> team = TeamsManager.getTeam(args[1]);
				if(team == null) return null;
				if(!team.isPresent()) return null;
				return team.get().getPlayers().stream().map(CMWPlayer::getName)
						.collect(Collectors.toList());
			}
		} else if (args[0].equalsIgnoreCase("setKit")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getTeamsConfigList().stream().map(CMWTeam::getName)
						.collect(Collectors.toList());
			} else if (args.length == 3) {
				return EnderCubeCMW.INSTANCE.getKits().stream().map(CMWKit::getName).collect(Collectors.toList());
			} else if (args.length == 4) {
				return null;
			}
		} else if (args[0].equalsIgnoreCase("setColor")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getTeamsConfigList().stream().map(CMWTeam::getName)
						.collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getTeamsConfigList().stream().map(CMWTeam::getName)
						.collect(Collectors.toList());
			}
		}

		return List.of("");
	}

}
