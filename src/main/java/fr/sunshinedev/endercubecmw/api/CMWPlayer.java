package fr.sunshinedev.endercubecmw.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.managers.ArenaManager;
import fr.sunshinedev.endercubecmw.managers.TeamsManager;

public class CMWPlayer  implements Comparable<CMWPlayer> {

	public Player p;
	public int point;
	public ItemStack[] backupInventoryContent;
	public ItemStack[] backupInventoryArmor;
	public BukkitRunnable taskCooldown;
	public Scoreboard scoreBoard;
	private CMWScoreboardGame cmwScoreBoardGame;

	public CMWPlayer(Player p) {
		this.p = p;
		this.point = 0;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public Player getSpigotPlayer() {
		return p;
	}
	
	public String getName() {
		return p.getName();
	}
	
	public void incrementPoint(int point) {
		this.point = this.point + point;
	}

	public ItemStack[] getBackupInventoryContent() {
		return backupInventoryContent;
	}

	public void setBackupInventoryContent(ItemStack[] backupInventoryContent) {
		this.backupInventoryContent = backupInventoryContent;
	}

	public ItemStack[] getBackupInventoryArmor() {
		return backupInventoryArmor;
	}

	public void setBackupInventoryArmor(ItemStack[] backupInventoryArmor) {
		this.backupInventoryArmor = backupInventoryArmor;
	}

	public Scoreboard getScoreBoard() {
		return this.scoreBoard;
	}
	
	public void setScoreboard(Scoreboard scoreboard) {
		this.scoreBoard = scoreboard;
	}

	public CMWScoreboardGame getCmwScoreBoardGame() {
		return cmwScoreBoardGame;
	}

	public void setCmwScoreBoardGame(CMWScoreboardGame cmwScoreBoardGame) {
		this.cmwScoreBoardGame = cmwScoreBoardGame;
	}

	@Override
	public int compareTo(CMWPlayer teamCompare) {
		int compare = teamCompare.getPoint();
        return this.getPoint()-compare;
    }

	public void cooldownCrashLog() {
		taskCooldown = new BukkitRunnable() {
			@Override
			public void run() {
				if(EnderCubeCMW.INSTANCE.getGame() != null) {
					TeamsManager.removePlayer(p);
					System.out.println("L'inventaire de " + p.getName() + " a été supprimé.");
					this.cancel();
				}
			}
		};
		taskCooldown.runTaskLater(EnderCubeCMW.INSTANCE, 6000); //5 Minutes
	}

	public void setSpigotPlayer(Player player) {
		this.p = player;
	}

	public void initScoreboard(CMWTeam team) {
		scoreBoard = EnderCubeCMW.INSTANCE.getScoreBoardManager().getNewScoreboard();
		cmwScoreBoardGame = new CMWScoreboardGame(scoreBoard, team);
		getCmwScoreBoardGame().addOrUpdateEntry(this, true);
		this.p.setScoreboard(scoreBoard);
	}

	public void deathPlayerPoints() {
		this.point = this.point - 50;
		if(this.point < 0) this.point = 0;
	}
	
}
