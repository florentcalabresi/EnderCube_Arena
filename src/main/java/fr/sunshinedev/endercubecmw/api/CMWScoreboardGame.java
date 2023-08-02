package fr.sunshinedev.endercubecmw.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.sunshinedev.endercubecmw.Utils;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.*;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;

import static fr.sunshinedev.endercubecmw.Utils.getPrimaryColor;
import static fr.sunshinedev.endercubecmw.Utils.getSecondaryColor;

public class CMWScoreboardGame {


	private final CMWTeam team;
	private final Team teamScoreboard;
	private Objective obj;
	private Scoreboard scoreboard;
	private String displayName = ChatColor.translateAlternateColorCodes('&', getSecondaryColor() + "<< " + getPrimaryColor() + " EnderCubeCMW " + getSecondaryColor() + ">>");
	private List<String> entrys = new ArrayList<>();

	public CMWScoreboardGame(Scoreboard scoreboard, CMWTeam team) {
		this.scoreboard = scoreboard;
		this.team = team;
		this.teamScoreboard = this.scoreboard.registerNewTeam(team.getName());
		this.teamScoreboard.setPrefix("[%s] ".formatted(team.getName()));
		this.teamScoreboard.setDisplayName("[%s] ".formatted(team.getChatName()));
		initObj();
	}
	
	public void initObj() {
		obj = scoreboard.registerNewObjective("EnderCubeCMW", "dummy", displayName);
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
	
	public void refreshEntrys() {
		/*Score s = obj.getScore(entry);
		s.setScore(0);*/
		
		for(int i=0;i<entrys.size();i++) 
			obj.getScore(entrys.get(i)).setScore(i);
	}
	
	public void addOrUpdateEntry(CMWPlayer player, boolean isUpdate) {
		CMWGame game = EnderCubeCMW.INSTANCE.getGame();
		if(game != null) {
			
			if(isUpdate) {
				scoreboard.resetScores(player.getSpigotPlayer());
				entrys.clear();
				if(scoreboard.getObjective("EnderCubeCMW") != null)
					scoreboard.getObjective("EnderCubeCMW").unregister();
				obj = scoreboard.registerNewObjective("EnderCubeCMW", "dummy", displayName);
				obj.setDisplaySlot(DisplaySlot.SIDEBAR);
			}

			if(!this.teamScoreboard.hasPlayer(player.getSpigotPlayer()))
				this.teamScoreboard.addPlayer(player.getSpigotPlayer());

			game.getTeams().forEach(team -> {
				boolean isYourTeam = team.getPlayers().contains(player);
				entrys.add(ChatColor.translateAlternateColorCodes('&', team.getChatName() + getPrimaryColor() + (isYourTeam ? " (Vous) " : "") + ": " + getSecondaryColor() + team.getPoints()));
			});
			entrys.add(ChatColor.translateAlternateColorCodes('&', getPrimaryColor() +"Vos points: " + getSecondaryColor() + player.getPoint()));
			entrys.add(ChatColor.translateAlternateColorCodes('&', getPrimaryColor() + "Manche(s): " + getSecondaryColor() + " %s sur %s".formatted(game.getIndexRoundCurrent(), game.getRoundsMax())));
			entrys.add("");
			
			refreshEntrys();

			player.getSpigotPlayer().setScoreboard(scoreboard);
		}
	}
	
}
