package fr.sunshinedev.endercubecmw.managers;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.api.CMWGame;
import fr.sunshinedev.endercubecmw.api.CMWRound;
import fr.sunshinedev.endercubecmw.api.CMWTeam;

public class GameManager {

	
	public static boolean checkIfGameIsReady() {
		CMWGame game = EnderCubeCMW.INSTANCE.getGame();
		
		if(game.getArena() == null) return false;
		
		if(game.getArena().getMobsLoc().size() < 1) return false;
		if(game.getArena().getPlayersSpawn() == null) return false;
		
		int iTeamsReady = 0;
		int iRoundsReady = 0;

		if(game.getTeams().size() < 1) return false;
		
		for(CMWTeam team : game.getTeams()) {
			if(team.getKit() == null) return false;
			if(team.getPlayers().size() < 1) return false;
			iTeamsReady++;
		}
		
		for(CMWRound round : game.getRounds()) {
			if(round.getMobsRound().size() < 1) return false;
			iRoundsReady++;
		}
		
		if(iTeamsReady < game.getTeams().size())  return false;
		if(iRoundsReady < 1)  return false;
		
		return true;
	}
	
}
