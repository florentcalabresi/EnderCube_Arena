package fr.sunshinedev.endercubecmw.managers;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.network.chat.ChatHexColor;
import org.json.JSONObject;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.api.CMWArena;
import fr.sunshinedev.endercubecmw.api.CMWGame;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom;
import fr.sunshinedev.endercubecmw.api.CMWRound;
import fr.sunshinedev.endercubecmw.api.CMWGame.GameState;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom.TypeMob;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONArray;

public class ArenaManager {

	public static void saveArena(CMWArena arena) {
		EnderCubeCMW.INSTANCE.getArenas().add(arena);
		
		JSONObject arenaJson = new JSONObject();
		arenaJson.put("name", arena.getName());
		arenaJson.put("world", arena.getWorld().getName());
		arenaJson.put("region", arena.getRegion().getId());
		arenaJson.put("playersSpawn", new JSONObject());
		arenaJson.put("spawnMonstersLocations", new JSONArray());
		
		JSONArray config = config();
		config.put(arenaJson);

        try {
            FileWriter kitWriter = new FileWriter(EnderCubeCMW.INSTANCE.arenasFile);
            kitWriter.write(config.toString());
            kitWriter.close();
            
            EnderCubeCMW.INSTANCE.reloadArenas();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
	public static JSONArray config() {
        try {
            InputStream targetStream = new FileInputStream(EnderCubeCMW.INSTANCE.arenasFile);
            return new JSONArray(IOUtils.toString(targetStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	public static Optional<CMWArena> getArena(String name) {
		return EnderCubeCMW.INSTANCE.getArenas().stream().filter(cmwArena -> cmwArena.getName().equalsIgnoreCase(name)).findFirst();
	}

	public static void addMobsLoc(Location loc, CMWArena cmwArena) {
		
		cmwArena.getMobsLoc().add(new Location(cmwArena.getWorld(), loc.getX(), loc.getY(), loc.getZ()));
		
		JSONArray arenas = config();
        for(int i = 0; i<arenas.length(); i++) {
            JSONObject arena = (JSONObject) arenas.get(i);
            if(arena.getString("name").equalsIgnoreCase(cmwArena.getName())){
            	
            	JSONArray mobsLocList = arena.getJSONArray("spawnMonstersLocations");
            	JSONObject mobLocArena = new JSONObject();
            	mobLocArena.put("x", loc.getX());
            	mobLocArena.put("y", loc.getY());
            	mobLocArena.put("z", loc.getZ());
            	
            	mobsLocList.put(mobLocArena);
            	
            	arenas.put(i, arena);
                break;
            }
        }
        
        try {
            FileWriter arenaWriter = new FileWriter(EnderCubeCMW.INSTANCE.arenasFile);
            arenaWriter.write(arenas.toString());
            arenaWriter.close();
            
            EnderCubeCMW.INSTANCE.reloadArenas();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}

	public static void summonRounds(CMWMobCustom mobTarget, int number, CMWArena arena) {
		for (int i = 0; i < number; i++) 
        {
            int index = new Random().nextInt(arena.getMobsLoc().size());
			if(mobTarget.getType() == TypeMob.BASIC)
				MobsManager.summonMob(arena.getMobsLoc().get(index), mobTarget);
			else if(mobTarget.getType() == TypeMob.ASSEMBLY)
				MobsManager.rideMobs(arena.getMobsLoc().get(index), mobTarget);
        }
	}
	
	public static void summonRounds(CMWRound round, CMWArena arena) {
		round.getMobsRound().forEach(mob -> {
			Optional<CMWMobCustom> mobCustom = MobsManager.getMobFromId(mob.getId());
			if(!mobCustom.isPresent()) return;
			summonRounds(mobCustom.get(), mob.getNumber(), arena);
		});
		if(EnderCubeCMW.INSTANCE.getGame() != null) {
			CMWGame game = EnderCubeCMW.INSTANCE.getGame();
			round.setMaxMobs(game.getMobsAlive().size());
			game.setState(GameState.START);
			BukkitRunnable taskRounds = new BukkitRunnable() {
			    @Override
			    public void run() {
					if(game.gameState == CMWGame.GameState.END) this.cancel();
			    	if(game.getMobsAlive().size() == 0) {
			    		game.getRounds().remove(0);
			    		if(game.getRounds().size() < 1) {
			    			game.finish();
			    		}else {
			    			startNextRounds();
			    		}
			    		this.cancel();
			    	}
			    }
			 };
			 taskRounds.runTaskTimerAsynchronously(EnderCubeCMW.INSTANCE, 0, 40);
		}
	}
	
	public static void startNextRounds() {
		if(EnderCubeCMW.INSTANCE.getGame() != null) {
			CMWGame game = EnderCubeCMW.INSTANCE.getGame();
			game.setStartTimeOut(20);
			game.incrementIndexRoundCurrent();
		    game.updateBoards();
			game.setState(GameState.LOAD_ROUND);
			BukkitRunnable taskStart = new BukkitRunnable() {
			    @Override
			    public void run() {
				    if(game.getGameState() == CMWGame.GameState.END) this.cancel();
			    	if(game.getStartTimeOut() > 0) {
			    		if(game.getGameState() == GameState.PAUSE) return;
			    		game.setStartTimeOut(game.getStartTimeOut() - 1);
			    		game.setTitleBossBar("Lancement de la prochaine manche dans %s secondes..".formatted(game.getStartTimeOut()));
			    		game.getBossBar().setProgress((double) game.getStartTimeOut()/20*100/100);
			    		return;
			    	}
				    CMWRound round = game.getRounds().get(0);
				    ArenaManager.summonRounds(round, game.getArena());
				    game.updateBossBarRound();
				    this.cancel();
			    }
			 };
			 taskStart.runTaskTimer(EnderCubeCMW.INSTANCE, 0, 20);
		}
	}

	public static void setPlayerSpawn(Location loc, CMWArena cmwArena) {
		cmwArena.setPlayersSpawn(new Location(cmwArena.getWorld(), loc.getX(), loc.getY(), loc.getZ()));
		
		JSONArray arenas = config();
        for(int i = 0; i<arenas.length(); i++) {
            JSONObject arena = (JSONObject) arenas.get(i);
            if(arena.getString("name").equalsIgnoreCase(cmwArena.getName())){
            	
            	JSONObject playersLocSpawn = new JSONObject();
            	playersLocSpawn.put("x", loc.getX());
            	playersLocSpawn.put("y", loc.getY());
            	playersLocSpawn.put("z", loc.getZ());
            	
            	arena.put("playersSpawn", playersLocSpawn);
            	
            	arenas.put(i, arena);
                break;
            }
        }
        
        try {
            FileWriter arenaWriter = new FileWriter(EnderCubeCMW.INSTANCE.arenasFile);
            arenaWriter.write(arenas.toString());
            arenaWriter.close();
            
            EnderCubeCMW.INSTANCE.reloadArenas();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
}
