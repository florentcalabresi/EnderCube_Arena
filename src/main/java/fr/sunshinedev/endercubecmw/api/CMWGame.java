package fr.sunshinedev.endercubecmw.api;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.json.JSONObject;

import fr.mrmicky.fastboard.FastBoard;
import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.managers.ArenaManager;
import fr.sunshinedev.endercubecmw.managers.KitsManager;

import static fr.sunshinedev.endercubecmw.Utils.getSecondaryColor;
import static fr.sunshinedev.endercubecmw.Utils.setPrimaryToSecondaryString;

public class CMWGame {

	public List<CMWTeam> teams = new ArrayList<>();

	public List<CMWRound> rounds = new ArrayList<>();

	public List<MobAlive> mobsAlive = new ArrayList<>();

	public Map<UUID, FastBoard> boards = new HashMap<>();

	public UUID idGame;

	public CMWArena arena;

	public GameState gameState;
	
	public int roundsMax = -1;

	private KeyedBossBar bossBar;

	int startTimeOut = 20;
	
	CMWTeam teamWin = null;
	
	Timestamp startTime;
	
	public String backupBossBar;
	
	private int indexRoundCurrent = 1;

	public CMWGame() {
		idGame = UUID.randomUUID();
		gameState = GameState.NO_READY;
		bossBar = Bukkit.createBossBar(new NamespacedKey(EnderCubeCMW.INSTANCE, "bbGameCMW"), "N/A", BarColor.PURPLE,
				BarStyle.SOLID);
		bossBar.setProgress(1.0);
	}

	public List<CMWTeam> getTeams() {
		return teams;
	}

	public UUID getID() {
		return idGame;
	}

	public CMWArena getArena() {
		return arena;
	}

	public void setArena(CMWArena arena) {
		this.arena = arena;
	}
 
	public GameState getGameState() {
		return gameState;
	}

	public void setState(GameState state) {
		this.gameState = state;
	}

	public List<CMWRound> getRounds() {
		return rounds;
	}

	public List<MobAlive> getMobsAlive() {
		return mobsAlive;
	}

	public KeyedBossBar getBossBar() {
		return bossBar;
	}

	public int getRoundsMax() {
		return roundsMax;
	}

	public void setRoundsMax(int roundsMax) {
		this.roundsMax = roundsMax;
	}

	public int getIndexRoundCurrent() {
		return indexRoundCurrent;
	}

	public void setIndexRoundCurrent(int indexRoundCurrent) {
		this.indexRoundCurrent = indexRoundCurrent;
	}
	
	public void incrementIndexRoundCurrent() {
		this.indexRoundCurrent++;
	}

	public enum GameState {

		NO_READY, START, LOAD_ROUND, PAUSE, IN_PROGRESS, END

	}

	public void sendMessagePlayers(String msg) {
		getTeams().forEach(team -> {
			team.getPlayers().forEach(player -> {
				Utils.sendPlayerMessage(player, ChatColor.translateAlternateColorCodes('&', msg));
			});
		});
	}
	
	public void sendMessagePlayersWithoutPrefix(String msg) {
		getTeams().forEach(team -> {
			team.getPlayers().forEach(player -> {
				Utils.sendPlayerMessageWithoutPrefix(player.getSpigotPlayer(), ChatColor.translateAlternateColorCodes('&', msg));
			});
		});
	}

	public void clearInventoryPlayers() {
		getTeams().forEach(team -> {
			team.getPlayers().forEach(player -> {
				player.getSpigotPlayer().getInventory().clear();
			});
		});
	}
	
	public void updateBoards() {
		getTeams().forEach(team -> {
			team.getPlayers().forEach(player -> {
				player.getCmwScoreBoardGame().addOrUpdateEntry(player, true);
			});
		});
	}
	
	public void updateBoards(CMWPlayer player) {
		player.getCmwScoreBoardGame().addOrUpdateEntry(player, true);
	}

	public void removeBoards() {
		getTeams().forEach(team -> {
			team.getPlayers().forEach(player -> {
				BukkitRunnable taskStart = new BukkitRunnable() {
					@Override
					public void run() {
						player.getSpigotPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
					}
				};
				taskStart.runTaskLater(EnderCubeCMW.INSTANCE, 0);
			});
		});
	}

	public void healPlayers() {
		getTeams().forEach(team -> {
			team.getPlayers().forEach(player -> {
				player.getSpigotPlayer().setHealth(20);
				player.getSpigotPlayer().setFoodLevel(20);
			});
		});
	}
	
	public void healPlayer(Player p) {
		p.setHealth(20);
		p.setFoodLevel(20);
	}

	public void incrementPointsPlayer(MobAlive mobCustom, Player killer) {
		getTeams().forEach(team -> {
			Optional<CMWPlayer> p = team.getPlayers().stream().filter(player -> player.getSpigotPlayer().getUniqueId().equals(killer.getUniqueId())).findFirst();
			if(p.isPresent()) {
				p.get().incrementPoint(mobCustom.getMob().getPoints());
			}
		});
	}

	public int getStartTimeOut() {
		return startTimeOut;
	}

	public void setStartTimeOut(int startTimeOut) {
		this.startTimeOut = startTimeOut;
	}

	public void updateBossBarRound() {
		CMWRound round = getRounds().get(0);
		setTitleBossBar("%s/%s Mobs restants - Manche(s) %s sur %s".formatted(getMobsAlive().size(), round.getMaxMobs(), getIndexRoundCurrent(), getRoundsMax()));
		bossBar.setProgress((double) getMobsAlive().size() / round.getMaxMobs() * 100 / 100);
	}
	
	public void setTitleBossBar(String s) {
		if(bossBar != null) {
			bossBar.setTitle(net.md_5.bungee.api.ChatColor.of("#7895DB") + s);
		}
	}

	public void start() {
		roundsMax = getRounds().size();
		getTeams().forEach(team -> {
			CMWKit kit = team.getKit();

			ItemStack armorHelmet = KitsManager.armorKit(kit, "helmet");
			ItemStack armorChestplate = KitsManager.armorKit(kit, "chestplate");
			ItemStack armorLeggings = KitsManager.armorKit(kit, "leggings");
			ItemStack armorBoots = KitsManager.armorKit(kit, "boots");
			ItemStack offHands = KitsManager.armorKit(kit, "offHands");

			team.getPlayers().forEach(player -> {
				
				player.initScoreboard(team);
				
				player.getSpigotPlayer().setPlayerListName(net.md_5.bungee.api.ChatColor.of(team.getColor()) + " %s ".formatted(player.getName()));

				player.getCmwScoreBoardGame().addOrUpdateEntry(player, true);
				
				player.getSpigotPlayer().teleport(getArena().getPlayersSpawn());
				player.getSpigotPlayer().getInventory().clear();
				player.getSpigotPlayer().setGameMode(GameMode.SURVIVAL);
				player.getSpigotPlayer().setHealth(20);
				player.getSpigotPlayer().setFoodLevel(20);

				player.getSpigotPlayer().getInventory().setHelmet(armorHelmet);
				player.getSpigotPlayer().getInventory().setChestplate(armorChestplate);
				player.getSpigotPlayer().getInventory().setLeggings(armorLeggings);
				player.getSpigotPlayer().getInventory().setBoots(armorBoots);
				player.getSpigotPlayer().getInventory().setItemInOffHand(offHands);
				
				player.getSpigotPlayer().getActivePotionEffects().forEach(potion -> {
					player.getSpigotPlayer().removePotionEffect(potion.getType());
				});

				getBossBar().addPlayer(player.getSpigotPlayer());

				for (int i = 0; i < kit.getInventory().length(); i++) {
					JSONObject itemObj = kit.getInventory().getJSONObject(i);
					if (itemObj.getInt("slot") >= 18) {
						Material material = Material.getMaterial(itemObj.getString("material"));
						String tag = "{}";
						if (itemObj.has("nbtTag"))
							tag = itemObj.getString("nbtTag");
						player.getSpigotPlayer().getInventory().addItem(NMSItem.applyNBTTagFromJson(
								new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")),
								tag));
					}
				}

				player.getSpigotPlayer().sendTitle(Utils.getPrimaryColor() + "EnderCubeCMW",
						Utils.getSecondaryColor() + "Lancement de la première vague dans 20 secondes..");
			});
		});
		
		BukkitRunnable taskStart = new BukkitRunnable() {
			@Override
			public void run() {
				if(gameState == CMWGame.GameState.END) this.cancel();
				if (startTimeOut > 0) {
					if(gameState == GameState.PAUSE) return;
					startTimeOut--;
					setTitleBossBar(net.md_5.bungee.api.ChatColor.of("#7895DB") + "Lancement de la partie dans %s secondes..".formatted(startTimeOut));
					bossBar.setProgress((double) startTimeOut / 20 * 100 / 100);
					return;
				}
				startTimeOut = 20;
				CMWRound round = getRounds().get(0);
				ArenaManager.summonRounds(round, getArena());
				updateBossBarRound();
				this.cancel();
			}
		};
		taskStart.runTaskTimer(EnderCubeCMW.INSTANCE, 0, 20);
	}
	
	public void pause() {
		setState(GameState.PAUSE);
		getMobsAlive().forEach(mob -> {
			getArena().getWorld().getEntities().forEach(entity -> {
				if(entity.getUniqueId().equals(mob.getId())) {
					if(entity instanceof LivingEntity) {
						((LivingEntity) entity).setAI(false);
						((LivingEntity) entity).setInvulnerable(true);
					}
				}
			});
		});
		backupBossBar = bossBar.getTitle();
		setTitleBossBar("La partie est actuellement en pause.");
	}
	


	public void resume() {
		setState(GameState.START);
		getMobsAlive().forEach(mob -> {
			getArena().getWorld().getEntities().forEach(entity -> {
				if(entity.getUniqueId().equals(mob.getId())) {
					if(entity instanceof LivingEntity) {
						((LivingEntity) entity).setAI(true);
						((LivingEntity) entity).setInvulnerable(false);
					}
				}
			});
		});
		setTitleBossBar(this.backupBossBar);
	}

	public void finish() {
		setState(GameState.END);
		for(CMWTeam team : getTeams()) {

			team.getPlayers().forEach(cmwPlayer -> {
				cmwPlayer.getSpigotPlayer().setPlayerListName(cmwPlayer.getName());
			});

			if(teamWin == null && team.getPoints() > 0) {
				teamWin = team;
				break;
			}
			if(teamWin != null) {
				if(teamWin.getPoints() < team.getPoints()) {
					teamWin = team;
					break;
				}
			}
		}
		
		if(teamWin != null) sendMessagePlayers("L'équipe %s a gagné la partie avec %s points !".formatted(teamWin.getName(), teamWin.getPoints()));
		else sendMessagePlayers("Aucune équipe n'a gagné la partie.");
		
		List<CMWTeam> teams = getTeams();
		Collections.sort(teams);
		Collections.reverse(teams);
		
		sendMessagePlayersWithoutPrefix(Utils.getPrimaryColor() + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
		sendMessagePlayersWithoutPrefix("                                 ");
		sendMessagePlayersWithoutPrefix(Utils.getPrimaryColor() + "    Classement équipes           ");
		sendMessagePlayersWithoutPrefix("                                 ");
		for(int i=0;i<teams.size();i++) {
			CMWTeam team = getTeams().get(i);
			sendMessagePlayersWithoutPrefix(Utils.getSecondaryColor() + "    " + (i + 1) + ". " + team.getChatName() + getSecondaryColor() + " - " + setPrimaryToSecondaryString(team.getPoints()) + " Points".formatted((i + 1), team.getName(), team.getPoints()));
		};
		sendMessagePlayersWithoutPrefix("                                 ");
		sendMessagePlayersWithoutPrefix(Utils.getSecondaryColor() + "    Classement des joueurs de votre équipe           ");
		sendMessagePlayersWithoutPrefix("                                 ");
		teams.forEach(team -> team.sendRankPlayers());
		sendMessagePlayersWithoutPrefix("                                 ");
		sendMessagePlayersWithoutPrefix(Utils.getPrimaryColor() + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");


		getBossBar().removeAll();
		clearInventoryPlayers();
		removeBoards();
		teams.forEach(team -> {
			team.clearPlayers();
		});
		EnderCubeCMW.INSTANCE.setGame(null);
	}
	
	public static class MobAlive {
		
		public UUID id;
		public CMWMobCustom mob;
		
		public MobAlive(UUID id, CMWMobCustom mob) {
			this.id = id;
			this.mob = mob;
		}

		public UUID getId() {
			return id;
		}

		public CMWMobCustom getMob() {
			return mob;
		}
		
	}

	public enum Power {
		STORM("cmwPowerStorm", Material.EGG),
		EXPLOSION("cmwPowerExplosion", Material.GUNPOWDER);

		private final String name;
		private final Material material;

		Power(String name, Material material) {
			this.name = name;
			this.material = material;
		}

		public String getName() {
			return name;
		}

		public Material getMaterial() {
			return material;
		}
	}

}
