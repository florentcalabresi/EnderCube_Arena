package fr.sunshinedev.endercubecmw.commands.executors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.sunshinedev.endercubecmw.api.*;
import fr.sunshinedev.endercubecmw.managers.TeamsManager;
import net.minecraft.network.chat.ChatHexColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.api.CMWGame.GameState;
import fr.sunshinedev.endercubecmw.managers.ArenaManager;
import fr.sunshinedev.endercubecmw.managers.GameManager;
import fr.sunshinedev.endercubecmw.managers.RoundsManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class GameExecutor extends IExecutor {

	public GameExecutor() {
		super("game", "ecmw.admin.game", new String[] {});
	}

	public GameExecutor(String name, String permission, String[] aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void run(CommandExecutor command, Player playerSender, String[] args) {
		super.run(command, playerSender, args);

		if (args.length > 0) {
			String action = args[0];
			if (action.equalsIgnoreCase("init")) {
				if (EnderCubeCMW.INSTANCE.getGame() != null) {
					Utils.sendPlayerMessageError(playerSender, "Une game a déjà été initiée");
					return;
				}
				EnderCubeCMW.INSTANCE.setGame(new CMWGame());
				Utils.sendPlayerMessageSuccess(playerSender,
						"La partie a été initiée. Faites /cmw game infos pour voir les informations et les configurations manquantes.");
			} else if (action.equalsIgnoreCase("setArena")) {
				if (args.length == 2) {
					String name = args[1];
					if (EnderCubeCMW.INSTANCE.getGame() == null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
						return;
					}
					Optional<CMWArena> optArena = ArenaManager.getArena(name);
					if (!optArena.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Cette arène n'existe pas");
						return;
					}
					CMWArena arena = optArena.get();
					if (arena.getPlayersSpawn() == null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous devez ajouter un point de spawn pour les joueurs avant de l'associer à la partie");
						return;
					}
					if (arena.getMobsLoc().size() < 1) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous devez ajouter un point de spawn pour les mobs avant de l'associer à la partie");
						return;
					}
					EnderCubeCMW.INSTANCE.getGame().setArena(optArena.get());
					Utils.sendPlayerMessageSuccess(playerSender, "L'arène a été associé à la partie");
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw game setArena <name>");
				}
			} else if (action.equalsIgnoreCase("addRound")) {
				if (args.length == 2) {
					String name = args[1];
					if (EnderCubeCMW.INSTANCE.getGame() == null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
						return;
					}
					if(EnderCubeCMW.INSTANCE.getGame().getGameState() != GameState.NO_READY) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car une partie est en cours.");
						return;
					}
					Optional<CMWRound> optRound = RoundsManager.getRoundFromName(name);
					if (!optRound.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Cette manche n'existe pas");
						return;
					}
					CMWRound round = optRound.get();
					if (round.getMobsRound().size() < 1) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous devez ajouter au moins 1 mob à cette manche pour l'ajouter à la partie.");
						return;
					}
					EnderCubeCMW.INSTANCE.getGame().getRounds().add(round);
					Utils.sendPlayerMessageSuccess(playerSender, "Cette manche a été ajoutée à la partie");
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw game addRound <name>");
				}
			}  else if (action.equalsIgnoreCase("removeRound")) {
				if (args.length == 2) {
					String name = args[1];
					if (EnderCubeCMW.INSTANCE.getGame() == null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
						return;
					}
					if(EnderCubeCMW.INSTANCE.getGame().getGameState() != GameState.NO_READY) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car une partie est en cours.");
						return;
					}
					CMWGame game = EnderCubeCMW.INSTANCE.getGame();
					Optional<CMWRound> roundOpt = game.getRounds().stream().filter(cmwRound -> cmwRound.getName().equalsIgnoreCase(name)).findFirst();
					if (!roundOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender,
								"Cette manche n'est pas dans la partie");
						return;
					}
					EnderCubeCMW.INSTANCE.getGame().getRounds().removeIf(roundCheck -> roundCheck.getId().equals(roundOpt.get().getId()));
					Utils.sendPlayerMessageSuccess(playerSender, "Cette manche a été retiré à la partie");
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw game removeRound <name>");
				}
			} else if (action.equalsIgnoreCase("addTeam")) {
				if (args.length == 2) {
					String name = args[1];
					if (EnderCubeCMW.INSTANCE.getGame() == null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
						return;
					}
					if(EnderCubeCMW.INSTANCE.getGame().getGameState() != GameState.NO_READY) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car une partie est en cours.");
						return;
					}
					Optional<CMWTeam> optTeam = TeamsManager.getTeamFromConfig(name);
					if (!optTeam.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Cette équipe n'existe pas");
						return;
					}
					CMWTeam team = optTeam.get();
					if (team.getKit() == null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous devez attribuer un kit pour cette équipe avant de l'ajouter à la partie.");
						return;
					}
					if (TeamsManager.getTeam(team.getName()).isPresent()) {
						Utils.sendPlayerMessageError(playerSender,
								"Cette équipe est déjà dans la partie");
						return;
					}
					EnderCubeCMW.INSTANCE.getGame().getTeams().add(team);
					Utils.sendPlayerMessageSuccess(playerSender, "Cette équipe a été ajoutée à la partie");
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw game addTeam <name>");
				}
			} else if (action.equalsIgnoreCase("removeTeam")) {
				if (args.length == 2) {
					String name = args[1];
					if (EnderCubeCMW.INSTANCE.getGame() == null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
						return;
					}
					if(EnderCubeCMW.INSTANCE.getGame().getGameState() != GameState.NO_READY) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car une partie est en cours.");
						return;
					}
					Optional<CMWTeam> team = TeamsManager.getTeam(name);
					if (!team.isPresent()) {
						Utils.sendPlayerMessageError(playerSender,
								"Cette équipe n'est pas dans la partie");
						return;
					}
					EnderCubeCMW.INSTANCE.getGame().getTeams().removeIf(teamCheck -> teamCheck.getId().equals(team.get().getId()));
					Utils.sendPlayerMessageSuccess(playerSender, "Cette équipe a été retiré à la partie");
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw game removeTeam <name>");
				}
			} else if (action.equalsIgnoreCase("infos")) {
				if (EnderCubeCMW.INSTANCE.getGame() == null) {
					Utils.sendPlayerMessageError(playerSender,
							"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
					return;
				}

				CMWGame game = EnderCubeCMW.INSTANCE.getGame();
				boolean arenaExist = game.getArena() != null;

				int iTeamsReady = 0;
				int iRoundsReady = 0;

				for (CMWTeam team : game.getTeams()) {
					if (team.getKit() == null)
						continue;
					if (team.getPlayers().size() < 1)
						continue;
					iTeamsReady++;
				}

				for (CMWRound round : game.getRounds()) {
					if (round.getMobsRound().size() < 1)
						continue;
					iRoundsReady++;
				}

				playerSender.sendMessage(ChatColor.AQUA + "================================");
				playerSender.sendMessage(ChatColor.AQUA + "Arène: "
						+ (arenaExist ? ChatColor.GREEN + game.getArena().getName() : ChatColor.RED + "Aucune"));
				playerSender.sendMessage(ChatColor.AQUA + "Nombre d'équipe prêtes: "
						+ (iTeamsReady > 0 ? ChatColor.GREEN + String.valueOf(iTeamsReady) : ChatColor.RED + "Aucune"));
				playerSender.sendMessage(ChatColor.AQUA + "Nombre de manches: "
						+ (iRoundsReady > 0 ? ChatColor.GREEN + String.valueOf(iRoundsReady)
								: ChatColor.RED + "Aucune"));
				playerSender.sendMessage(ChatColor.AQUA + "Equipe: ");
				game.getTeams().forEach(team -> {

					TextComponent textComponent = new TextComponent(ChatColor.GRAY + "   - %s avec le kit %s"
							.formatted(team.getName(), (team.getKit() == null ? "(Aucun)" : team.getKit().getName())));

					StringBuilder playersName = new StringBuilder(ChatColor.AQUA + "Players: \n");
					team.getPlayers().forEach(p -> {
						playersName.append(ChatColor.GRAY + p.getSpigotPlayer().getName() + "\n");
					});

					textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder(playersName.toString()).create()));

					playerSender.spigot().sendMessage(textComponent);
				});
				playerSender.sendMessage(ChatColor.AQUA + "Rounds: ");
				game.getRounds().forEach(round -> {
					playerSender.sendMessage("");
					playerSender.sendMessage(ChatColor.GRAY + "   - %s avec %s mobs".formatted(round.getName(), round.getMobsRound().size()));
					playerSender.sendMessage(ChatColor.GRAY + "   - Liste des mobs: ");
					RoundsManager.sendDataRoundChat(playerSender, round);
					playerSender.sendMessage("");
				});
				playerSender.sendMessage(ChatColor.AQUA + "================================");
			} else if (action.equalsIgnoreCase("start")) {
				if (EnderCubeCMW.INSTANCE.getGame() == null) {
					Utils.sendPlayerMessageError(playerSender,
							"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
					return;
				}

				CMWGame game = EnderCubeCMW.INSTANCE.getGame();
				if (game.getGameState() == GameState.PAUSE || game.getGameState() == GameState.START || game.getGameState() == GameState.LOAD_ROUND) {
					Utils.sendPlayerMessageSuccess(playerSender,
							"La partie doit être non démarrer pour exécuter cette commande.");
					return;
				}

				if (!GameManager.checkIfGameIsReady()) {
					Utils.sendPlayerMessageError(playerSender,
							"Vous ne pouvez pas exécuter cette action car la partie n'est pas prête. (Voir /cmw game infos)");
					return;
				}

				game.setState(GameState.LOAD_ROUND);
				game.start();
			} else if (action.equalsIgnoreCase("pause")) {

				if (EnderCubeCMW.INSTANCE.getGame() == null) {
					Utils.sendPlayerMessageError(playerSender,
							"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
					return;
				}

				CMWGame game = EnderCubeCMW.INSTANCE.getGame();
				if (game.getGameState() != GameState.START || game.getGameState() == GameState.LOAD_ROUND) {
					Utils.sendPlayerMessageSuccess(playerSender,
							"La partie doit être en cours pour exécuter cette commande.");
					return;
				}

				game.pause();
				Utils.sendPlayerMessageSuccess(playerSender, "La partie est en pause.");

			} else if (action.equalsIgnoreCase("resume")) {

				if (EnderCubeCMW.INSTANCE.getGame() == null) {
					Utils.sendPlayerMessageError(playerSender,
							"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
					return;
				}

				CMWGame game = EnderCubeCMW.INSTANCE.getGame();
				if (game.getGameState() != GameState.PAUSE) {
					Utils.sendPlayerMessageSuccess(playerSender,
							"La partie doit être en pause pour exécuter cette commande.");
					return;
				}

				game.resume();
				Utils.sendPlayerMessageSuccess(playerSender, "La partie n'est plus en pause.");

			} else if (action.equalsIgnoreCase("stop")) {
				if (EnderCubeCMW.INSTANCE.getGame() == null) {
					Utils.sendPlayerMessageError(playerSender,
							"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
					return;
				}

				CMWGame game = EnderCubeCMW.INSTANCE.getGame();
				if (game.gameState != GameState.START || game.getGameState() == GameState.LOAD_ROUND) {
					Utils.sendPlayerMessageError(playerSender,
							"Vous ne pouvez pas exécuter cette action car la partie n'est pas en cours.");
					return;
				}
				game.getMobsAlive().clear();
				game.getArena().getWorld().getEntities().forEach(entity -> {
					if (entity instanceof LivingEntity) {
						LivingEntity livEntity = (LivingEntity) entity;
						if (entity.getPersistentDataContainer().has(new NamespacedKey(EnderCubeCMW.INSTANCE, "cmwMob"), PersistentDataType.STRING)) {
							livEntity.setHealth(0);
						}
					}
				});
				game.finish();
			} else if (action.equalsIgnoreCase("preset")) {
				if (args.length == 2) {
					String name = args[1];
					if (EnderCubeCMW.INSTANCE.getGame() == null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car aucune partie n'a été initiée");
						return;
					}

					CMWGame game = EnderCubeCMW.INSTANCE.getGame();
					if (game.gameState == GameState.START || game.getGameState() == GameState.START) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car la partie est en cours.");
						return;
					}

					if (!GameManager.checkIfGameIsReady()) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car la partie n'est pas prête. (Voir /cmw game infos)");
						return;
					}

					if (EnderCubeCMW.INSTANCE.getGamesPresetList().containsKey(name)) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car le nom du preset existe déjà.");
						return;
					}

					EnderCubeCMW.INSTANCE.getGamesPresetList().put(name, game);
					EnderCubeCMW.INSTANCE.setGame(null);

					Utils.sendPlayerMessageSuccess(playerSender,
							"La partie à été sauvegardés sous le nom %s. Pour la charger, faites /cmw game load <NomDuPreset>");
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw game preset <name>");
				}

			} else if (action.equalsIgnoreCase("load")) {
				if (args.length == 2) {
					String name = args[1];
					if (EnderCubeCMW.INSTANCE.getGame() != null) {
						Utils.sendPlayerMessageError(playerSender,
								"Vous ne pouvez pas exécuter cette action car une partie est déjà chargée, initiée ou lancée");
						return;
					}

					CMWGame gamePreset = EnderCubeCMW.INSTANCE.getGamesPresetList().get(name);
					gamePreset.setIndexRoundCurrent(1);

					EnderCubeCMW.INSTANCE.setGame(gamePreset);

					Utils.sendPlayerMessageSuccess(playerSender,
							"La partie à été chargée avec succès. Pour lancer la partie, faites /cmw game start");
				} else {
					Utils.sendPlayerMessageError(playerSender, "/cmw game preset <name>");
				}

			} else if (action.equalsIgnoreCase("skipRound")) {
				if (EnderCubeCMW.INSTANCE.getGame() == null) {
					Utils.sendPlayerMessageError(playerSender, "Vous ne pouvez pas exécuter cette action car aucune partie est lancée");
					return;
				}

				if (EnderCubeCMW.INSTANCE.getGame().getGameState() != GameState.START) {
					Utils.sendPlayerMessageError(playerSender, "Vous ne pouvez pas exécuter cette action car aucune partie est lancée ou aucune manche n'est en cours.");
					return;
				}

				EnderCubeCMW.INSTANCE.getGame().getArena().getWorld().getEntities().forEach(entity -> {
					if (entity instanceof LivingEntity) {
						LivingEntity livEntity = (LivingEntity) entity;
						if (entity.getPersistentDataContainer().has(new NamespacedKey(EnderCubeCMW.INSTANCE, "cmwMob"), PersistentDataType.STRING)) {
							livEntity.setHealth(0);
						}
					}
				});
				EnderCubeCMW.INSTANCE.getGame().getMobsAlive().clear();

				Utils.sendPlayerMessageSuccess(playerSender, "La manche en cours a été skip avec succès.");

			} else if (action.equalsIgnoreCase("givePower")) {
				if (args.length == 2) {
					if(Arrays.stream(CMWGame.Power.values()).noneMatch(ageCheck -> ageCheck.name().equalsIgnoreCase(args[1]))) {
						Utils.sendPlayerMessageError(playerSender, "Le povuoir n'existe pas");
						return;
					}
					CMWGame.Power power = CMWGame.Power.valueOf(args[1]);
                    ItemStack itemPower = new ItemStack(power.getMaterial());
                    ItemMeta itemMeta = itemPower.getItemMeta();
                    assert itemMeta != null;
                    itemMeta.getPersistentDataContainer().set(new NamespacedKey(EnderCubeCMW.INSTANCE, power.getName()), PersistentDataType.BOOLEAN, true);
                    itemPower.setItemMeta(itemMeta);

                    playerSender.getInventory().addItem(itemPower);

					Utils.sendPlayerMessageSuccess(playerSender, "L'item " + power.name() + " vous a été give avec succès.");
                }else{
					Utils.sendPlayerMessageError(playerSender, "/cmw game givePower <PowerName>");
				}

			}
		} else {
			Utils.sendPlayerMessageError(playerSender,
					"/cmw game <init|setArena|addRound|removeRound|addTeam|removeTeam|infos|start|pause|resume|stop|preset|load|skipRound|givePower>");
		}
	}

	@Override
	public @Nullable List<String> tabCompleter(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
		if (args.length == 1)
			return Arrays.asList("init", "setArena", "infos", "addRound", "removeRound", "start", "pause", "resume", "stop", "preset",
					"load", "skipRound", "addTeam", "removeTeam", "givePower").stream().filter(s -> s.startsWith(args[0])).toList();;
		if (args[0].equalsIgnoreCase("setArena")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getArenas().stream().map(CMWArena::getName).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		} else if (args[0].equalsIgnoreCase("addRound")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getRounds().stream().map(CMWRound::getName).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		} else if (args[0].equalsIgnoreCase("removeRound")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getRounds().stream().map(CMWRound::getName).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		}else if (args[0].equalsIgnoreCase("load")) {
			if (args.length == 2)
				return EnderCubeCMW.INSTANCE.getGamesPresetList().keySet().stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());

			return null;
		} else if (args[0].equalsIgnoreCase("addTeam")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getTeamsConfigList().stream().map(CMWTeam::getName).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		}else if (args[0].equalsIgnoreCase("removeTeam")) {
			if (args.length == 2) {
				if(EnderCubeCMW.INSTANCE.getGame() != null)
					return EnderCubeCMW.INSTANCE.getGame().getTeams().stream().map(CMWTeam::getName).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		}else if (args[0].equalsIgnoreCase("givePower")) {
			if (args.length == 2)
				return Arrays.stream(CMWGame.Power.values()).map(CMWGame.Power::name).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			return null;
		}
		return null;
	}

}
