package fr.sunshinedev.endercubecmw.commands.executors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class GamePresetExecutor extends IExecutor {


    public GamePresetExecutor() {
        super("gamePreset", "ecmw.admin.gamePreset", new String[]{});
    }

    @Override
    public void run(CommandExecutor command, Player playerSender, String[] args) {
        super.run(command, playerSender, args);

        if (args.length > 0) {
            String action = args[0];
            if (action.equalsIgnoreCase("save")) {

            }
        }
    }

    @Override
    public @Nullable List<String> tabCompleter(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return Arrays.asList("save").stream().filter(s -> s.startsWith(args[0])).toList();;
        return null;
    }
}
