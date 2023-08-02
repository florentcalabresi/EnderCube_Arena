package fr.sunshinedev.endercubecmw.commands.executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class IExecutor {

    private final String name;
    private String permission;
    private String[] aliases = new String[]{};

    public IExecutor(String name){
        this.name = name;
    }
    public IExecutor(String name, String permission, String[] aliases){
        this.name = name;
        this.permission = permission;
        this.aliases = aliases;
    }
    public void run(CommandExecutor command, Player playerSender, String[] args){ }


    public abstract @Nullable List<String> tabCompleter(CommandSender sender, Command cmd, String alias, String[] args);

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getPermission() {
        return permission;
    }
}
