package fr.sunshinedev.endercubecmw.commands.executors;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;

public class ReloadExecutor extends IExecutor {

	public ReloadExecutor() {
		super("reload", "ecmw.admin.reload", new String[] {});
	}

	@Override
	public void run(CommandExecutor command, Player playerSender, String[] args) {
		if(EnderCubeCMW.INSTANCE.getGame() != null) EnderCubeCMW.INSTANCE.getGame().finish();
		EnderCubeCMW.INSTANCE.getMapCommandECMWExecutor().clear();
		EnderCubeCMW.INSTANCE.initPlugin(true);
		Utils.sendPlayerMessageSuccess(playerSender, "EnderCubeCMW vient d'être reload.");
	}

	@Override
	public @Nullable List<String> tabCompleter(CommandSender sender, Command cmd, String alias, String[] args) {
		return null;
	}

}
