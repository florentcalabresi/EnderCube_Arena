package fr.sunshinedev.endercubecmw.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class CMWArena {
	
	public World world;
	public ProtectedRegion region;
	public String name;
	public Location playersSpawn;
	public List<Location> mobsLoc = new ArrayList<>();
	
	public CMWArena(String name) {
		this.name = name;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public ProtectedRegion getRegion() {
		return region;
	}

	public void setRegion(ProtectedRegion region) {
		this.region = region;
	}

	public String getName() {
		return name;
	}
	
	public List<Location> getMobsLoc() {
		return mobsLoc;
	}

	public Location getPlayersSpawn() {
		return playersSpawn;
	}

	public void setPlayersSpawn(Location playersSpawn) {
		this.playersSpawn = playersSpawn;
	}
	

}
