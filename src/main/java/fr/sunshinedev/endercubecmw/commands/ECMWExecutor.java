package fr.sunshinedev.endercubecmw.commands;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.commands.executors.IExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ECMWExecutor implements CommandExecutor, TabCompleter {


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String aliases, @NotNull String[] args) {
        if(!(commandSender instanceof Player pSender)) {
            commandSender.sendMessage("Only Player");
            return true;
        }

        if(args.length >= 1){ 
            String execKey = args[0];

            IExecutor iExecutor = getExecutor(pSender, execKey, true);
            args = Arrays.copyOfRange(args, 1, args.length);

            if(iExecutor != null) {
            	if(iExecutor.getPermission() != null){
                    if(!pSender.hasPermission(iExecutor.getPermission())) {
                    	Utils.sendPlayerMessageError(pSender, "Vous n'avez pas la permission pour cette commande.");
                        return true;
                    }
                }
                iExecutor.run(this, pSender, args);
            }
            return true;
        }
        Utils.sendPlayerMessageError(pSender, "L'action n'existe pas.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            @NotNull String[] finalArgs = args;
            return EnderCubeCMW.INSTANCE.getMapCommandECMWAction().stream().filter(s -> s.startsWith(finalArgs[0])).toList();
        }

        if(!(sender instanceof Player pSender)) {
            return List.of("");
        }

        IExecutor iExecutor = getExecutor(pSender, args[0], false);
        if(iExecutor != null) {
            args = Arrays.copyOfRange(args, 1, args.length);
            return iExecutor.tabCompleter(sender, cmd, alias, args);
        }
        
        return null;
    }

    public IExecutor getExecutor(Player pSender, String execKey, boolean chatExecutorNull) {
        Optional<IExecutor> executor = EnderCubeCMW.INSTANCE.getMapCommandECMWExecutor().stream().filter((iExec) ->
                iExec.getName().equalsIgnoreCase(execKey) || Arrays.stream(iExec.getAliases()).anyMatch((str) -> str.equalsIgnoreCase(execKey))).findFirst();


        if(executor.isEmpty()) {
        	if(chatExecutorNull) Utils.sendPlayerMessageError(pSender, "L'action n'existe pas.");
            return null;
        }

        return executor.get();
    }

}
