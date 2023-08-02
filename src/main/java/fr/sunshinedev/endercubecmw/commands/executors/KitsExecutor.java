package fr.sunshinedev.endercubecmw.commands.executors;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.api.CMWGame;
import fr.sunshinedev.endercubecmw.api.CMWKit;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom;
import fr.sunshinedev.endercubecmw.api.CMWTeam;
import fr.sunshinedev.endercubecmw.gui.KitGui;
import fr.sunshinedev.endercubecmw.managers.ArenaManager;
import fr.sunshinedev.endercubecmw.managers.KitsManager;
import fr.sunshinedev.endercubecmw.managers.MobsManager;
import fr.sunshinedev.endercubecmw.managers.TeamsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class KitsExecutor extends IExecutor {

    public KitsExecutor() {
        super("kits", "ecmw.admin.kits", new String[]{});
    }

    public KitsExecutor(String name, String permission, String[] aliases) {
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

                    if(KitsManager.getKitFromName(name).isPresent()) {
                        Utils.sendPlayerMessageError(playerSender, "Un kit porte déjà ce nom.");
                        return;
                    }

                    CMWKit kit = new CMWKit(name);
                    kit.setId(UUID.randomUUID());
                    
                    KitsManager.addKit(kit);

                    KitGui kitGui = new KitGui(playerSender, kit);
                    kitGui.getWindow().open();

                    Utils.sendPlayerMessageSuccess(playerSender, "Le kit a bien été créé");

                }else {
					Utils.sendPlayerMessageError(playerSender, "/cmw kit create <name>");
                }
            }else if(action.equalsIgnoreCase("edit")) {
                if(args.length > 1) {
                    String name = args[1];

                    Optional<CMWKit> kit = KitsManager.getKitFromName(name);
                    if(!kit.isPresent()) {
                    	Utils.sendPlayerMessageError(playerSender, "Le kit n'existe pas :'(");
                        return;
                    }

                    KitGui kitGui = new KitGui(kit.get());
                    kitGui.show(playerSender);

                }
            } else if (action.equalsIgnoreCase("remove")) {
                if (args.length > 1) {
                    String name = args[1];
                    if (EnderCubeCMW.INSTANCE.getGame() != null) {
                        if(EnderCubeCMW.INSTANCE.getGame().getGameState() != CMWGame.GameState.NO_READY ||
                                EnderCubeCMW.INSTANCE.getGame().getGameState() != CMWGame.GameState.END)
                        Utils.sendPlayerMessageError(playerSender,
                                "Vous ne pouvez pas exécuter cette action car une partie a été démarré");
                        return;
                    }

                    Optional<CMWKit> kitOpt = KitsManager.getKitFromName(name);
                    if(!kitOpt.isPresent()) {
                        Utils.sendPlayerMessageError(playerSender, "Le kit %s n'existe pas :'(".formatted(name));
                        return;
                    }

                    CMWKit kit = kitOpt.get();

                    if(EnderCubeCMW.INSTANCE.getGame() != null) {
                        Utils.sendPlayerMessageSuccess(playerSender, "Les équipes liées au kit %s ont été retirées de la partie.".formatted(kit.getName()));
                        EnderCubeCMW.INSTANCE.getGame().getTeams().removeIf(teamGame -> teamGame.getKit().getId().equals(kit.getId()));
                    }

                    Utils.sendPlayerMessageSuccess(playerSender, "Le kit %s a été retiré de la configuration des équipes.".formatted(kit.getName()));

                    for(int i=0;i<EnderCubeCMW.INSTANCE.getTeamsConfigList().size();i++) {
                        CMWTeam team = EnderCubeCMW.INSTANCE.getTeamsConfigList().get(i);
                        if (kit.getId().equals(team.getKit().getId())) {
                            System.out.println("Team attribute kit set null");
                            TeamsManager.attributeKit(team, null);
                        }
                    }

                    KitsManager.removeKit(kitOpt.get());

                    Utils.sendPlayerMessageSuccess(playerSender, "Le kit a bien été supprimé");
                }else {
                    Utils.sendPlayerMessageError(playerSender, "/cmw kit remove <name>");
                }
            }
        }else{
        	Utils.sendPlayerMessageError(playerSender, "/cmw kits <create|edit|remove>");
        }
    }

    @Override
    public @Nullable List<String> tabCompleter(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return Arrays.asList("create", "edit", "remove").stream().filter(s -> s.startsWith(args[0])).toList();

        if (args[0].equalsIgnoreCase("edit")) {
            if (args.length == 2) {
                return EnderCubeCMW.INSTANCE.getKits().stream()
                        .map(CMWKit::getName)
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }else if (args.length == 3) {
                return null;
            }
        } else  if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 2) {
                return EnderCubeCMW.INSTANCE.getKits().stream()
                        .map(CMWKit::getName)
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }else if (args.length == 3) {
                return null;
            }
        }

        return List.of("");
    }

}
