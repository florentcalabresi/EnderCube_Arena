package fr.sunshinedev.endercubecmw.commands.executors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import fr.sunshinedev.endercubecmw.managers.TeamsManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.api.CMWArena;
import fr.sunshinedev.endercubecmw.managers.ArenaManager;
import fr.sunshinedev.endercubecmw.managers.WorldGuardManager;

public class ArenaExecutor extends IExecutor {

    public ArenaExecutor() {
        super("arena", "ecmw.admin.arena", new String[]{});
    }

    public ArenaExecutor(String name, String permission, String[] aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void run(CommandExecutor command, Player playerSender, String[] args) {
        super.run(command, playerSender, args);

        if(args.length > 0) {
            String action = args[0];
            if(action.equalsIgnoreCase("create")) {
                if(args.length == 3) {
                    String name = args[1];
                    String region = args[2];

                    if(ArenaManager.getArena(name).isPresent()) {
                        Utils.sendPlayerMessageError(playerSender, "Une arène porte déjà ce nom.");
                        return;
                    }
                    
                    CMWArena arena = new CMWArena(name);
                    arena.setWorld(playerSender.getWorld());
                    arena.setRegion(WorldGuardManager.getRegion(playerSender.getWorld(), region));
                    
                    ArenaManager.saveArena(arena);
                    
                    Utils.sendPlayerMessageSuccess(playerSender, "L'arène a bien été ajoutée");
                    
                    
                }else {
                	Utils.sendPlayerMessageError(playerSender, "/cmw arena create <name> <region_worldguard>");
                }
            }else if(action.equalsIgnoreCase("mobsloc")) {
            	if(args.length == 2) {
                    String name = args[1];
                    Location loc = playerSender.getLocation();
                    
                    Optional<CMWArena> optArena = ArenaManager.getArena(name);
                    if(!optArena.isPresent()) {
                    	Utils.sendPlayerMessageError(playerSender,"Cette arène n'existe pas");
						return;
					}

                    ArenaManager.addMobsLoc(loc, optArena.get());
                    
                    Utils.sendPlayerMessageSuccess(playerSender, "Vous avez ajouté une zone d'apparation pour les mobs pour l'arène %s (X:%s Z:%s)".formatted(name, loc.getX(), loc.getZ()));
                    
                    
                }else {
                	Utils.sendPlayerMessageError(playerSender, "/cmw arena create <name>");
                }
            }else if(action.equalsIgnoreCase("spawnPlayer")) {
            	if(args.length == 2) {
                    String name = args[1];
                    Location loc = playerSender.getLocation();
                    
                    Optional<CMWArena> optArena = ArenaManager.getArena(name);
                    if(!optArena.isPresent()) {
                    	Utils.sendPlayerMessageError(playerSender, "Cette arène n'existe pas");
						return;
					}
                    ArenaManager.setPlayerSpawn(loc, optArena.get());
                    Utils.sendPlayerMessageSuccess(playerSender, "Vous avez ajouté une zone d'apparation aux joueurs pour l'arène %s (X:%s Z:%s)".formatted(name, loc.getX(), loc.getZ()));
                    
                    
                }else {
                	Utils.sendPlayerMessageError(playerSender, "/cmw arena spawnPlayer <name>");
                }
            }
        }else{
        	Utils.sendPlayerMessageError(playerSender, "/cmw arena <create>");
        }
    }

    @Override
    public @Nullable List<String> tabCompleter(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return Arrays.asList("create", "mobsloc", "spawnPlayer").stream().filter(s -> s.startsWith(args[0])).toList();;

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 3) {
                return WorldGuardManager.getListRegions(((Player) sender).getWorld()).keySet().stream().collect(Collectors.toList());
            }else if (args.length == 3) {
                return null;
            }
        }else if (args[0].equalsIgnoreCase("mobsloc")) {
            if (args.length == 2) {
                return EnderCubeCMW.INSTANCE.getArenas().stream().map(CMWArena::getName).collect(Collectors.toList());
            }else if (args.length == 3) {
                return null;
            }
        }else if (args[0].equalsIgnoreCase("spawnPlayer")) {
            if (args.length == 2) {
                return EnderCubeCMW.INSTANCE.getArenas().stream().map(CMWArena::getName).collect(Collectors.toList());
            }else if (args.length == 3) {
                return null;
            }
        }
        
        return List.of("");
    }

}
