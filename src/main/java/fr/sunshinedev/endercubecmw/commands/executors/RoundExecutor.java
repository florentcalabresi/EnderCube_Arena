package fr.sunshinedev.endercubecmw.commands.executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.api.CMWArena;
import fr.sunshinedev.endercubecmw.api.CMWKit;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom;
import fr.sunshinedev.endercubecmw.api.CMWRound;
import fr.sunshinedev.endercubecmw.api.CMWRound.MobRound;
import fr.sunshinedev.endercubecmw.gui.KitGui;
import fr.sunshinedev.endercubecmw.managers.ArenaManager;
import fr.sunshinedev.endercubecmw.managers.KitsManager;
import fr.sunshinedev.endercubecmw.managers.MobsManager;
import fr.sunshinedev.endercubecmw.managers.RoundsManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.sunshinedev.endercubecmw.Utils.checkStringIfUUID;

public class RoundExecutor extends IExecutor {

	public RoundExecutor() {
		super("round", "ecmw.admin.rounds", new String[] {});
	}

	public RoundExecutor(String name, String permission, String[] aliases) {
		super(name, permission, aliases);
	}
  
	@Override
    public void run(CommandExecutor command, Player playerSender, String[] args) {
        super.run(command, playerSender, args);

        if(args.length > 0) {
        	String action = args[0];
            if(action.equalsIgnoreCase("create")) {
            	if(args.length > 1) {
                    String name = args[1];

					if(RoundsManager.getRoundFromName(name).isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Un round porte déjà ce nom.");
						return;
					}

                    CMWRound round = new CMWRound(name);
                    round.setId(UUID.randomUUID());
                    
                    RoundsManager.addRound(round);
                    
                    EnderCubeCMW.INSTANCE.reloadRounds();

                    Utils.sendPlayerMessageSuccess(playerSender, "Le roud a bien été créé");
                }else {
					Utils.sendPlayerMessageError(playerSender, "/cmw round create <name>");
                }
            }else if(action.equalsIgnoreCase("addMobs")) {
            	if(args.length >= 4) {
                    String name = args[1];
                    String mob = args[2];
                    String amount = args[3];
                    
                    Optional<CMWRound> roundOpt = RoundsManager.getRoundFromName(name);
                    Optional<CMWMobCustom> mobOpt = MobsManager.getMobFromName(mob);
					if(!roundOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce round n'existe pas");
						return;
					}
					if(!mobOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'existe pas");
						return;
					}
                    if (!Utils.checkStringIfInt(amount)) {
                    	Utils.sendPlayerMessageError(playerSender, "Vous devez indiquer un nombre de mobs valide.");
                        return;
                    }

					CMWRound round = roundOpt.get();
					CMWMobCustom mobCustom = mobOpt.get();

					if(round.getMobsRound().stream().anyMatch(mobRound -> mobRound.getId().equals(mobCustom.getId()))) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob est déjà présent dans la manche.");
						return;
					}
                    
                    round.getMobsRound().add(new MobRound(mobCustom.getId(), Integer.parseInt(amount)));
                    
                    RoundsManager.addMobInRound(round.getId(), mobCustom.getId(), Integer.parseInt(amount));

                    Utils.sendPlayerMessageSuccess(playerSender, "Le round a bien été mis à jour.");
                }else {
                	Utils.sendPlayerMessageError(playerSender, "/cmw rounds addMobs <name> <mob> <amount>");
                }
            } else if(action.equalsIgnoreCase("removeMobs")) {
				if(args.length > 2) {
					String name = args[1];
					String mobId = args[2];

					if(!checkStringIfUUID(mobId)) {
						Utils.sendPlayerMessageError(playerSender, "L'id du mob ne semble pas valide.");
						return;
					}

					Optional<CMWRound> roundOpt = RoundsManager.getRoundFromName(name);
					Optional<CMWMobCustom> mobOpt = MobsManager.getMobFromId(UUID.fromString(mobId));
					if(!roundOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce round n'existe pas");
						return;
					}
					if(!mobOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'existe pas");
						return;
					}

					CMWRound round = roundOpt.get();
					CMWMobCustom mobCustom = mobOpt.get();

					if(!round.getMobsRound().stream().anyMatch(mobRound -> mobRound.getId().equals(mobCustom.getId()))) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'est pas présent dans la manche.");
						return;
					}

					RoundsManager.removeMobInRound(round, mobCustom);

					Utils.sendPlayerMessageSuccess(playerSender, "Le mob a bien été retiré de la manche.");
				}else {
					Utils.sendPlayerMessageError(playerSender, "/cmw rounds removeMobs <name> <index>");
				}
			} else	if(action.equalsIgnoreCase("setAmount")) {
				if (args.length > 3) {
					String name = args[1];
					String mob = args[2];
					String amount = args[3];

					Optional<CMWRound> roundOpt = RoundsManager.getRoundFromName(name);
					if(!roundOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce round n'existe pas");
						return;
					}

					CMWRound round = roundOpt.get();
					Optional<MobRound> mobOpt = round.getMobsRound().stream().filter(mobRound -> mobRound.getMobCustom().getDisplayName().equalsIgnoreCase(mob)).findFirst();
					if(!mobOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce mob n'est pas présent dans cette manche.");
						return;
					}

					MobRound mobRound = mobOpt.get();

					if (!Utils.checkStringIfInt(amount)) {
						Utils.sendPlayerMessageError(playerSender, "Vous devez indiquer un nombre de mobs valide.");
						return;
					}

					RoundsManager.setAmountMob(round, mobRound, Integer.parseInt(amount));

					Utils.sendPlayerMessageSuccess(playerSender, "Le round a bien été mis à jour.");
				}else {
					Utils.sendPlayerMessageError(playerSender, "/cmw rounds setAmount <name> <mob> <amount>");
				}
			} else if(action.equalsIgnoreCase("summon")) {
	        	if(args.length >= 3) {
	                String roundName = args[1];
	                String arenaName = args[2];
	                
	                Optional<CMWRound> roundOpt = RoundsManager.getRoundFromName(roundName);
	                Optional<CMWArena> arenaOpt = ArenaManager.getArena(arenaName);
					if(!roundOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce round n'existe pas");
						return;
					}
					if(!arenaOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Cette arène n'existe pas");
						return;
					}
	
	                CMWRound round = roundOpt.get();
	                CMWArena arena = arenaOpt.get();
	                
	                round.getMobsRound().forEach(mob -> {
	                	Optional<CMWMobCustom> mobOpt = MobsManager.getMobFromId(mob.getId());
	                	if(!mobOpt.isPresent()) {
	                		Utils.sendPlayerMessageError(playerSender, "Ce mob semble ne plus exister pour ce round.");
							return;
						}
	                	
	                	ArenaManager.summonRounds(mobOpt.get(), mob.getNumber(), arena);
	                });
	                
	                Utils.sendPlayerMessageSuccess(playerSender, "La manche %s vient d'être summon dans l'arène %s.".formatted(round.getName(), arena.getName()));
	        	}else {
	        		Utils.sendPlayerMessageError(playerSender, "/cmw rounds summon <name> <mob> <number>");
                }
            }else if(action.equalsIgnoreCase("infos")) {
	        	if(args.length > 1) {
	                String roundName = args[1];
	                
	                Optional<CMWRound> roundOpt = RoundsManager.getRoundFromName(roundName);
					if(!roundOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce round n'existe pas");
						return;
					}
	
	                CMWRound round = roundOpt.get();
	                
	                playerSender.sendMessage(ChatColor.AQUA + "================================");
					playerSender.sendMessage(ChatColor.BLUE + "Nom du round: " + ChatColor.GRAY + round.getName());
					playerSender.sendMessage(ChatColor.BLUE + "Liste des mobs: ");
					RoundsManager.sendDataRoundChat(playerSender, round);
					playerSender.sendMessage(ChatColor.AQUA + "================================");
	        	}else {
	        		Utils.sendPlayerMessageError(playerSender, "/cmw rounds infos <name>");
                }
            }else if(action.equalsIgnoreCase("remove")) {
				if(args.length > 1) {
					String roundName = args[1];

					Optional<CMWRound> roundOpt = RoundsManager.getRoundFromName(roundName);
					if(!roundOpt.isPresent()) {
						Utils.sendPlayerMessageError(playerSender, "Ce round n'existe pas");
						return;
					}

					CMWRound round = roundOpt.get();
					RoundsManager.removeRound(round);

					Utils.sendPlayerMessageSuccess(playerSender, "La manche %s a bien été supprimée.".formatted(round.getName()));
				}else {
					Utils.sendPlayerMessageError(playerSender, "/cmw rounds remove <name>");
				}
			}
    	}else{
    		Utils.sendPlayerMessageError(playerSender, "/cmw rounds <create>");
        }
    }

	@Override
	public @Nullable List<String> tabCompleter(@NotNull CommandSender sender, @NotNull Command cmd,
			@NotNull String alias, @NotNull String[] args) {
		if (args.length == 1)
			return Arrays.asList("create", "addMobs", "removeMobs", "setAmount", "summon", "infos", "remove").stream().filter(s -> s.startsWith(args[0])).toList();
		if (args[0].equalsIgnoreCase("addMobs")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getRounds().stream().map(CMWRound::getName).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().map(CMWMobCustom::getDisplayName).filter(s -> s.startsWith(args[2]))
						.collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		}else if (args[0].equalsIgnoreCase("setAmount")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getRounds().stream().map(CMWRound::getName).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return EnderCubeCMW.INSTANCE.getMobsCustom().stream().map(CMWMobCustom::getDisplayName).filter(s -> s.startsWith(args[2]))
						.collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		}else if (args[0].equalsIgnoreCase("summon")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getRounds().stream().map(CMWRound::getName).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return EnderCubeCMW.INSTANCE.getArenas().stream().map(CMWArena::getName)
						.filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
			} else if (args.length == 4) {
				return null;
			}
		}else if (args[0].equalsIgnoreCase("infos")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getRounds().stream().map(CMWRound::getName).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		}else if (args[0].equalsIgnoreCase("remove")) {
			if (args.length == 2) {
				return EnderCubeCMW.INSTANCE.getRounds().stream().map(CMWRound::getName).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
			} else if (args.length == 3) {
				return null;
			}
		}
		return null;
	}

}
