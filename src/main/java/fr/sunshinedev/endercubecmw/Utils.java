package fr.sunshinedev.endercubecmw;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.awt.*;

import fr.sunshinedev.endercubecmw.api.CMWPlayer;

public class Utils {

	public static boolean checkStringIfInt(String val) {
		try {
			Integer.parseInt(val);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public static boolean checkStringIfDouble(String val) {
		try {
			Double.parseDouble(val);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean checkStringIfColorHex(String val) {
		try {
			net.md_5.bungee.api.ChatColor.of(val);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean checkStringIfUUID(String s) {
		try{
			UUID.fromString(s);
			return true;
		} catch (IllegalArgumentException exception){
			return false;
		}
	}

	public static String compareTwoTimeStamps(Timestamp currentTime, Timestamp oldTime) {
		long milliseconds1 = oldTime.getTime();
		long milliseconds2 = currentTime.getTime();

		long diff = milliseconds2 - milliseconds1;
		long diffSeconds = diff / 1000;
		long diffMinutes = diff / (60 * 1000);
		long diffHours = diff / (60 * 60 * 1000);
		long diffDays = diff / (24 * 60 * 60 * 1000);

		return diffMinutes + " min, " + diffSeconds + " sec.";
	}
	
	public static ItemStack OutGuiItemStack() {
		ItemStack itemStack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(" ");
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
	
	public static ItemStack DoneItemStack() {
		ItemStack itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName(ChatColor.GREEN + "Confirmer");
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}
	
	public static void sendPlayerMessage(Player p, String msg) {
		p.sendMessage(EnderCubeCMW.INSTANCE.getPrefixChat() + ChatColor.GRAY + msg);
	}
	
	public static void sendPlayerMessageWithoutPrefix(Player p, String msg) {
		p.sendMessage(ChatColor.GRAY + org.bukkit.ChatColor.translateAlternateColorCodes('&', msg));
	}
	
	public static void sendPlayerMessageSuccess(Player p, String msg) {
		p.sendMessage(EnderCubeCMW.INSTANCE.getPrefixChat() + ChatColor.GREEN + msg);
	}
	
	public static void sendPlayerMessageError(Player p, String msg) {
		p.sendMessage(EnderCubeCMW.INSTANCE.getPrefixChat() + ChatColor.RED + msg);
	}
	
	public static void sendPlayerMessage(CMWPlayer p, String msg) {
		sendPlayerMessage(p.getSpigotPlayer(), msg);
	}

    public static net.md_5.bungee.api.ChatColor getPrimaryColor() {
		return net.md_5.bungee.api.ChatColor.of("#7895DB");
	}

	public static net.md_5.bungee.api.ChatColor getSecondaryColor() {
		return net.md_5.bungee.api.ChatColor.of("#869fd9");
	}

	public static String setPrimaryToSecondaryString(String s) {
		return getPrimaryColor() + s + getSecondaryColor();
	}

	public static String setPrimaryToSecondaryString(int s) {
		return getPrimaryColor() + String.valueOf(s) + getSecondaryColor();
	}
}
