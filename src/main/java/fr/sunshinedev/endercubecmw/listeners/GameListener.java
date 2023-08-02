package fr.sunshinedev.endercubecmw.listeners;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.api.CMWGame;
import fr.sunshinedev.endercubecmw.api.CMWGame.GameState;
import fr.sunshinedev.endercubecmw.api.CMWPlayer;
import fr.sunshinedev.endercubecmw.api.CMWTeam;
import fr.sunshinedev.endercubecmw.managers.TeamsManager;
import fr.sunshinedev.endercubecmw.managers.WorldGuardManager;

public class GameListener implements Listener {

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        CMWPlayer cmwPlayer = TeamsManager.getPlayerFromTeam(event.getPlayer());
        if (EnderCubeCMW.INSTANCE.getGame() != null) {
            p.setGameMode(GameMode.SURVIVAL);
            if (cmwPlayer != null) {
                cmwPlayer.setSpigotPlayer(event.getPlayer());
                cmwPlayer.taskCooldown.cancel();
                EnderCubeCMW.INSTANCE.getGame().getBossBar().addPlayer(p);
                p.getInventory().setArmorContents(cmwPlayer.getBackupInventoryArmor());
                p.getInventory().setContents(cmwPlayer.getBackupInventoryContent());
                p.setScoreboard(cmwPlayer.getScoreBoard());
            }
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        CMWPlayer cmwPlayer = TeamsManager.getPlayerFromTeam(event.getPlayer());
        if (EnderCubeCMW.INSTANCE.getGame() != null) {
            if (cmwPlayer != null) {
                Optional<CMWTeam> cmwTeam = TeamsManager.getTeamFromPlayer(cmwPlayer);
                cmwTeam.ifPresent(team -> team.removePlayer(cmwPlayer));
                EnderCubeCMW.INSTANCE.getGame().getBossBar().removePlayer(event.getPlayer());
                cmwPlayer.setBackupInventoryArmor(p.getInventory().getArmorContents());
                cmwPlayer.setBackupInventoryContent(p.getInventory().getContents());
                p.getInventory().clear();
                cmwPlayer.cooldownCrashLog();
            }
        }
    }

    @EventHandler
    public void onPlayerPreventDeath(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity livEntity = (LivingEntity) event.getEntity();
            if (EnderCubeCMW.INSTANCE.getGame() != null) {
                CMWGame game = EnderCubeCMW.INSTANCE.getGame();
                if (((livEntity.getHealth() - event.getFinalDamage()) <= 0) && livEntity instanceof Player) {
                    CMWPlayer player = TeamsManager.getPlayerFromTeam((Player) livEntity);
                    if (player != null) {
                        if (event.getEntity() instanceof Player) {
                            event.setCancelled(true);
                            return;
                        }
                        player.deathPlayerPoints();
                        game.updateBoards();
                        game.sendMessagePlayers("%s est mort. Il fait perdre 50 points à son équipe."
                                .formatted(player.getSpigotPlayer().getName()));
                        event.setCancelled(true);
                        game.healPlayers();
                        livEntity.teleport(game.getArena().getPlayersSpawn());
                        livEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, -1, 255));
                        livEntity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, -1, 128));
                        BukkitRunnable taskStart = new BukkitRunnable() {
                            @Override
                            public void run() {
                                livEntity.removePotionEffect(PotionEffectType.SLOW);
                                livEntity.removePotionEffect(PotionEffectType.JUMP);
                            }
                        };
                        taskStart.runTaskLater(EnderCubeCMW.INSTANCE, 300);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (EnderCubeCMW.INSTANCE.getGame() != null) {
            CMWGame game = EnderCubeCMW.INSTANCE.getGame();
            CMWPlayer cmwPlayer = TeamsManager.getPlayerFromTeam(event.getPlayer());
            if (game.getArena() != null) {
                if (cmwPlayer != null
                        && !WorldGuardManager.PlayerInRegion(event.getPlayer(), game.getArena().getRegion())) {
                    Optional<CMWTeam> cmwTeam = TeamsManager.getTeamFromPlayer(cmwPlayer);
                    if (cmwTeam.isPresent()) {
                        cmwTeam.get().removePlayer(cmwPlayer);
                        cmwPlayer.getSpigotPlayer().getInventory().clear();
                        EnderCubeCMW.INSTANCE.getGame().getBossBar().removePlayer(event.getPlayer());
                        cmwPlayer.getSpigotPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                        Utils.sendPlayerMessageError(event.getPlayer(),
                                "Vous avez quitter l'arène, vous avez été kick de la partie.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPowerHitProjectile(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Egg egg) {
            if (egg.getShooter() instanceof Player eggShooter) {
                Location locLigthning;
                if (Objects.requireNonNull(egg.getItem().getItemMeta()).getPersistentDataContainer().has(new NamespacedKey(EnderCubeCMW.INSTANCE, CMWGame.Power.STORM.getName()), PersistentDataType.BOOLEAN)) {
                    if (event.getHitBlock() != null)
                        locLigthning = event.getHitBlock().getLocation();
                    else if (event.getHitEntity() != null)
                        locLigthning = event.getHitEntity().getLocation();
                    else {
                        locLigthning = null;
                    }
                    assert locLigthning != null;
                    Objects.requireNonNull(locLigthning.getWorld()).getNearbyEntities(locLigthning, 10, 10, 10).forEach(entity -> {
                        if (!(entity instanceof Player) && (!(entity instanceof Item)) && (!(entity instanceof Projectile))) {
                            locLigthning.getWorld().strikeLightning(entity.getLocation());
                            entity.getPersistentDataContainer().set(new NamespacedKey(EnderCubeCMW.INSTANCE, CMWGame.Power.STORM.getName()), PersistentDataType.STRING, eggShooter.getUniqueId().toString());
                            EnderCubeCMW.INSTANCE.getCooldownPowerItem().add(CMWGame.Power.STORM.getName().toLowerCase() + ":" + eggShooter.getUniqueId());
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    public void onPowerHitUseItem(PlayerInteractEvent event) {

        boolean checkHasCooldown = false;
        if (event.getItem() != null) {
            if (event.getItem().getItemMeta() != null) {
                Optional<NamespacedKey> key = event.getItem().getItemMeta().getPersistentDataContainer().getKeys().stream().filter(namespacedKey -> namespacedKey.getNamespace().equalsIgnoreCase(EnderCubeCMW.INSTANCE.getName().toLowerCase(Locale.ROOT))).findFirst();
                if (key.isPresent()) {
                    Optional<CMWGame.Power> power = Arrays.stream(CMWGame.Power.values()).filter(power1 -> power1.getName().equalsIgnoreCase(key.get().getKey())).findFirst();
                    if (power.isPresent()) {
                        checkHasCooldown = EnderCubeCMW.INSTANCE.getCooldownPowerItem().stream().anyMatch(s -> s.equalsIgnoreCase(power.get().getName() + ":" + event.getPlayer().getUniqueId()));
                    }
                }
            }
        }

        if (checkHasCooldown) {
            Utils.sendPlayerMessageError(event.getPlayer(), "Vous pouvez utiliser cet item dans X secondes.");
            event.setCancelled(true);
            return;
        }

        if (event.hasItem() && event.getMaterial() == CMWGame.Power.EXPLOSION.getMaterial()) {
            ItemStack item = event.getItem();
            assert item != null;
            if (item.hasItemMeta()) {
                //EXPLOSION
                if (Objects.requireNonNull(event.getItem().getItemMeta()).getPersistentDataContainer().has(new NamespacedKey(EnderCubeCMW.INSTANCE, CMWGame.Power.EXPLOSION.getName()), PersistentDataType.BOOLEAN)) {
                    if (event.getItem().getAmount() > 1)
                        event.getItem().setAmount(event.getItem().getAmount() - 1);
                    else
                        event.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                    Location locExplode = event.getPlayer().getLocation();
                    if (locExplode.getWorld() != null)
                        locExplode.getWorld().createExplosion(locExplode.getX(), locExplode.getY(), locExplode.getZ(), 3, false, false, event.getPlayer());
                }

            }

        }
    }

    @EventHandler
    public void onPlayerDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getCause() == DamageCause.ENTITY_EXPLOSION) {
                if (event.getDamager() instanceof Player) {
                    if (EnderCubeCMW.INSTANCE.getGame() != null) {
                        EnderCubeCMW.INSTANCE.getGame().getTeams().forEach(team -> {
                            Optional<CMWPlayer> p = team.getPlayers().stream().filter(player -> player.getSpigotPlayer().getUniqueId().equals(event.getEntity().getUniqueId())).findFirst();
                            p.ifPresent(cmwPlayer -> event.setCancelled(true));
                        });
                    }
                }
            }
        }
    }

    @EventHandler
    public void OnEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player p) {
            if (EnderCubeCMW.INSTANCE.getGame() != null) {
                CMWPlayer cmwPlayer = TeamsManager.getPlayerFromTeam(p);
                if (cmwPlayer != null) {
                    if (event.getCause() == DamageCause.LIGHTNING || event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.BLOCK_EXPLOSION) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

}
