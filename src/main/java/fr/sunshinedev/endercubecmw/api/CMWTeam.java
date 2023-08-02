package fr.sunshinedev.endercubecmw.api;

import java.util.*;

import fr.sunshinedev.endercubecmw.Utils;
import net.minecraft.network.chat.ChatHexColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import static fr.sunshinedev.endercubecmw.Utils.setPrimaryToSecondaryString;

public class CMWTeam implements Comparable<CMWTeam> {

    public ArrayList<CMWPlayer> players;

    public UUID id;
    public String name;
    public String colorHex;
    public CMWKit kit;

    public CMWTeam(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.players = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public String getChatName() {
        return net.md_5.bungee.api.ChatColor.of(getColor()) + name + ChatColor.RESET;
    }

    public ArrayList<CMWPlayer> getPlayers() {
        return players;
    }

    public void attributeKit(CMWKit akit) {
        kit = akit;
    }

    public CMWKit getKit() {
        return kit;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getColor() {
        return colorHex;
    }

    public void setColor(String colorHex) {
        this.colorHex = colorHex;
    }

    public int getPoints() {
    	int points = 0;
    	for(CMWPlayer p : getPlayers())
    		points = points + p.getPoint();
    	return points;
    }

    public void clearPlayers() {
        getPlayers().clear();
    }

	@Override
	public int compareTo(CMWTeam teamCompare) {
		int compare= teamCompare.getPoints();
        return this.getPoints()-compare;
    }

    public void addPlayer(CMWPlayer p) {
        getPlayers().add(p);
    }

    public void removePlayer(CMWPlayer cmwPlayer) {
        getPlayers().removeIf(p -> p.getSpigotPlayer().getUniqueId() == cmwPlayer.getSpigotPlayer().getUniqueId());
    }

    public void sendRankPlayers() {
        List<CMWPlayer> players = getPlayers();
        Collections.sort(players);
        Collections.reverse(players);
        for(int i=0;i<players.size();i++) {
            CMWPlayer player = getPlayers().get(i);
            Utils.sendPlayerMessageWithoutPrefix(player.getSpigotPlayer(), Utils.getSecondaryColor() + "    " + (i + 1) + ". " + setPrimaryToSecondaryString(player.getName()) + " - " + setPrimaryToSecondaryString(player.getPoint()) + " Points");
        };
    }
}
