package fr.sunshinedev.endercubecmw.managers;

import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class WorldGuardManager {

	public static Map<String, ProtectedRegion> getListRegions(World world) {
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		@Nonnull
		RegionManager regions = container.get(new BukkitWorld(world));
		return regions.getRegions();
	}

	public static ProtectedRegion getRegion(World world, String id) {
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		@Nonnull
		RegionManager regions = container.get(new BukkitWorld(world));
		return regions.getRegion(id);
	}

	public static boolean PlayerInRegion(Player player, ProtectedRegion region) {
	
		int x = player.getLocation().getBlockX();
		int y = player.getLocation().getBlockY();
		int z = player.getLocation().getBlockZ();
		if (region.contains(BlockVector3.at(x, y, z))) {
			return true;
		} else {
			return false;
		}
	}

}
