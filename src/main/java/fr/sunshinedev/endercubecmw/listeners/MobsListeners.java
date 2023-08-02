package fr.sunshinedev.endercubecmw.listeners;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import fr.mrmicky.fastboard.FastBoard;
import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.api.CMWGame;
import fr.sunshinedev.endercubecmw.api.CMWGame.GameState;
import fr.sunshinedev.endercubecmw.api.CMWGame.MobAlive;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom;
import org.bukkit.plugin.RegisteredListener;

public class MobsListeners implements Listener {

	private void updateBoard(FastBoard board) {
		board.updateLines("", "Players: " + Bukkit.getServer().getOnlinePlayers().size(), "",
				"Kills: " + board.getPlayer().getStatistic(Statistic.PLAYER_KILLS), "");
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity.getPersistentDataContainer().has(new NamespacedKey(EnderCubeCMW.INSTANCE, "cmwMob"),
				PersistentDataType.STRING)) {
			if (!event.getDrops().isEmpty()) {
				List<ItemStack> l = event.getDrops();
                for (ItemStack item : new ArrayList<>(event.getDrops())) {
                    if (item != null)
                        l.remove(item);
                }
			}
			Player pKiller = event.getEntity().getKiller() != null ? event.getEntity().getKiller() : null;
			if(entity.getLastDamageCause() != null) {
				if(entity.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
					NamespacedKey namespacedKey = new NamespacedKey(EnderCubeCMW.INSTANCE, "cmwThunderSource");
					if(entity.getPersistentDataContainer().has(new NamespacedKey(EnderCubeCMW.INSTANCE, "cmwThunderSource"), PersistentDataType.STRING)) {
						UUID uuid = UUID.fromString(Objects.requireNonNull(entity.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING)));
						pKiller = Bukkit.getPlayer(uuid);
					}
				}
			}
			if (EnderCubeCMW.INSTANCE.getGame() != null) {
				CMWGame game = EnderCubeCMW.INSTANCE.getGame();
				if (game.getGameState() == GameState.START || game.getGameState() == GameState.PAUSE
						|| game.getGameState() == GameState.LOAD_ROUND
						|| game.getGameState() == GameState.IN_PROGRESS) {
					killMobCMW(entity, pKiller); // Killer is null, this is suicide.
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityExplode(ExplosionPrimeEvent event) {
		Entity entity = event.getEntity();
		if (entity.getPersistentDataContainer().has(new NamespacedKey(EnderCubeCMW.INSTANCE, "cmwMob"),
				PersistentDataType.STRING)) {
			if (EnderCubeCMW.INSTANCE.getGame() != null) {
				killMobCMW(entity, null);
			}
		}
	}

	@EventHandler
	public void onEntityCombust(EntityCombustEvent event) {
		if (event.getEntity().getPersistentDataContainer().has(new NamespacedKey(EnderCubeCMW.INSTANCE, "cmwMob"), PersistentDataType.STRING)) {
			if ((!(event instanceof EntityCombustByEntityEvent)) && (!(event instanceof EntityCombustByBlockEvent))) {
				event.setCancelled(true);
			}
		}
	}

	public void killMobCMW(Entity entity, Player killer) {
		CMWGame game = EnderCubeCMW.INSTANCE.getGame();
		Optional<MobAlive> mobCustom = game.getMobsAlive().stream()
				.filter(mob -> mob.getId().equals(entity.getUniqueId())).findFirst();
		if (mobCustom.isPresent() && killer != null) {
			game.incrementPointsPlayer(mobCustom.get(), killer);
		}
		game.getMobsAlive().removeIf(mobAlive -> mobAlive.getId().equals(entity.getUniqueId()));
		game.updateBossBarRound();
		game.updateBoards();
	}

}
