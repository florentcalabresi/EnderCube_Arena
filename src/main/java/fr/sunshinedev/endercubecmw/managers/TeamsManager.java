package fr.sunshinedev.endercubecmw.managers;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.api.*;
import org.apache.commons.io.IOUtils;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public class TeamsManager {

    public static void saveTeam(CMWTeam team) {
        team.setId(UUID.randomUUID());
        EnderCubeCMW.INSTANCE.getTeamsConfigList().add(team);

        JSONObject arenaJson = new JSONObject();
        arenaJson.put("id", team.getId());
        arenaJson.put("name", team.getName());
        arenaJson.put("kit", "");
        arenaJson.put("color", "#6E7B8B");

        JSONArray config = config();
        config.put(arenaJson);

        saveConfig(config.toString());
    }

    public static void saveConfig(String jarray) {
        try {
            FileWriter kitWriter = new FileWriter(EnderCubeCMW.INSTANCE.teamsFile);
            kitWriter.write(jarray);
            kitWriter.close();

            EnderCubeCMW.INSTANCE.reloadTeams();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONArray config() {
        try {
            InputStream targetStream = new FileInputStream(EnderCubeCMW.INSTANCE.teamsFile);
            return new JSONArray(IOUtils.toString(targetStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<CMWTeam> getTeam(UUID id) {
        if(EnderCubeCMW.INSTANCE.getGame() == null) return null;
        return EnderCubeCMW.INSTANCE.getGame().getTeams().stream().filter(team -> team.getId() == id).findFirst();
    }

    public static Optional<CMWTeam> getTeam(String name) {
        if(EnderCubeCMW.INSTANCE.getGame() == null) return null;
        return EnderCubeCMW.INSTANCE.getGame().getTeams().stream().filter(team -> team.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static Optional<CMWTeam> getTeamFromConfig(UUID id) {
        return EnderCubeCMW.INSTANCE.getTeamsConfigList().stream().filter(team -> team.getId() == id).findFirst();
    }

    public static Optional<CMWTeam> getTeamFromConfig(String name) {
        return EnderCubeCMW.INSTANCE.getTeamsConfigList().stream().filter(team -> team.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static Optional<CMWTeam> getTeamConfig(String name) {
        return EnderCubeCMW.INSTANCE.getTeamsConfigList().stream().filter(team -> team.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static Optional<CMWTeam> getTeamFromPlayer(CMWPlayer player) {
        return EnderCubeCMW.INSTANCE.getGame().getTeams().stream().filter(team -> team.getPlayers().contains(player)).findFirst();
    }
    
    public static CMWPlayer getPlayerFromTeam(Player player) {
    	if(EnderCubeCMW.INSTANCE.getGame() != null) {
    		CMWGame game = EnderCubeCMW.INSTANCE.getGame();
    		for(CMWTeam team : game.getTeams()) {
        		for(CMWPlayer p : team.getPlayers()) {
        			if(p.getSpigotPlayer().getUniqueId().equals(player.getUniqueId())) {
        				return p;
        			}
        		}
        	}
    	}
    	return null;
    }

    public static boolean checkPlayerIsInTeam(Player player) {
        return getPlayerFromTeam(player) != null;
    }
    
    public static CMWPlayer removePlayer(Player player) {
    	for(CMWTeam team : EnderCubeCMW.INSTANCE.getGame().getTeams()) {
    		team.getPlayers().removeIf(p -> p.getSpigotPlayer().getUniqueId().equals(player.getUniqueId()));
    	}
    	return null;
    }

    public static void attributeKit(CMWTeam cmwTeam, CMWKit cmwKit) {
        cmwTeam.attributeKit(cmwKit);

        JSONArray teams = config();
        for (int i = 0; i < teams.length(); i++) {
            JSONObject team = (JSONObject) teams.get(i);
            if (UUID.fromString(team.getString("id")).equals(cmwTeam.getId())) {
                team.put("kit", cmwKit != null ? cmwKit.getId() : "");
                break;
            }
        }

        saveConfig(teams.toString());
    }

    public static void setColor(CMWTeam cmwTeam, String color) {
        cmwTeam.setColor(color);

        JSONArray teams = config();
        for (int i = 0; i < teams.length(); i++) {
            JSONObject team = (JSONObject) teams.get(i);
            if (UUID.fromString(team.getString("id")).equals(cmwTeam.getId())) {
                team.put("color", color);
                break;
            }
        }

        saveConfig(teams.toString());
    }

    public static void remove(CMWTeam team) {
        EnderCubeCMW.INSTANCE.getTeamsConfigList().removeIf(cmwTeam -> cmwTeam.getId().equals(team.getId()));

        JSONArray teams = config();
        for (int i = 0; i < teams.length(); i++) {
            JSONObject mobJson = (JSONObject) teams.get(i);
            if (UUID.fromString(mobJson.getString("id")).equals(team.getId())) {
                teams.remove(i);
                break;
            }
        }

        saveConfig(teams.toString());
    }

    public enum ColorTeam {

        BLUE(TrimMaterial.DIAMOND, TrimPattern.SILENCE),
        YELLOW(TrimMaterial.GOLD, TrimPattern.SILENCE),
        RED(TrimMaterial.REDSTONE, TrimPattern.SILENCE),
        GREEN(TrimMaterial.EMERALD, TrimPattern.SILENCE);

        public final ArmorTrim armorTrim;

        ColorTeam(TrimMaterial trimMaterial, TrimPattern trimPattern){
            this.armorTrim = new ArmorTrim(trimMaterial, trimPattern);

        }

        public ArmorTrim getArmorTrim() {
            return armorTrim;
        }
    }

}
