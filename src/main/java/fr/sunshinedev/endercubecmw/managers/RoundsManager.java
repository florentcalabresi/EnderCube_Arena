package fr.sunshinedev.endercubecmw.managers;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.io.IOUtils;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom.TypeMob;
import fr.sunshinedev.endercubecmw.api.CMWRound;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class RoundsManager {
 
	public static void addRound(CMWRound cmwRound) {
        EnderCubeCMW.INSTANCE.getRounds().add(cmwRound);

        JSONObject round = new JSONObject();
        round.put("id", cmwRound.getId());
        round.put("name", cmwRound.getName());
        round.put("mobs", new JSONArray());

        JSONArray configRound = config();
		configRound.put(round);

		saveFileAndReloadRounds(configRound.toString());

    }
	
	public static JSONArray config() {
        try {
            InputStream targetStream = new FileInputStream(EnderCubeCMW.INSTANCE.roundsFile);
            return new JSONArray(IOUtils.toString(targetStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	public static void saveFileAndReloadRounds(String jArray) {
		try {
			FileWriter kitWriter = new FileWriter(EnderCubeCMW.INSTANCE.roundsFile);
			kitWriter.write(jArray);
			kitWriter.close();

			EnderCubeCMW.INSTANCE.reloadRounds();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Optional<CMWRound> getRoundFromName(String name) {
        return EnderCubeCMW.INSTANCE.getRounds().stream().filter(cmwRound -> cmwRound.getName().equalsIgnoreCase(name)).findFirst();
    }

	public static void addMobInRound(UUID roundId, UUID mobId, int number) {
		JSONObject roundMob = new JSONObject();
		roundMob.put("id", mobId);
		roundMob.put("number", number);

        JSONArray rounds = config();
        for(int i = 0; i<rounds.length(); i++) {
            JSONObject round = (JSONObject) rounds.get(i);
            if(UUID.fromString(round.getString("id")).equals(roundId)) {
                round.getJSONArray("mobs").put(roundMob);
                break;
            }
        }

		saveFileAndReloadRounds(rounds.toString());
	}

	public static void sendDataRoundChat(Player playerSender, CMWRound round) {
		TextComponent t = new TextComponent();
		round.getMobsRound().forEach(mobRound -> {
			Optional<CMWMobCustom> mobOpt = MobsManager.getMobFromId(mobRound.getId());
			if(mobOpt.isPresent()) {
				
				CMWMobCustom mobCustom = mobOpt.get();

				int mobAssemblyPoint = 0;
				if(mobCustom.getType() == TypeMob.ASSEMBLY) {
					Optional<CMWMobCustom> mobOptAbove = MobsManager.getMobFromId(mobCustom.getAboveMob());
					Optional<CMWMobCustom> mobOptBelow = MobsManager.getMobFromId(mobCustom.getBelowMob());
					if(mobOptAbove.isPresent() && mobOptBelow.isPresent()) {
						mobAssemblyPoint = mobOptAbove.get().getPoints() + mobOptBelow.get().getPoints();
					}
				}
				
				String entityName = mobCustom.getType() == TypeMob.BASIC ? mobCustom.getEntity().getName() : mobCustom.getDisplayName();
				TextComponent textComponent = new TextComponent(ChatColor.GRAY + "    - %s (%sx %s) - Rapporte %s points".formatted(
						mobCustom.getDisplayName(), 
						mobRound.getNumber(), 
						entityName,
						(mobCustom.getType() == TypeMob.BASIC ? mobCustom.getPoints() : mobAssemblyPoint)));

				TextComponent removeMob = new TextComponent(ChatColor.RED + " [Supprimer]");
				removeMob.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cmw round removeMobs %s %s".formatted(round.getName(), mobRound.getId())));
				textComponent.addExtra(removeMob);

				StringBuilder effectMobs = new StringBuilder();
				StringBuilder inventory = new StringBuilder();
				if(mobCustom.getType() == TypeMob.BASIC) {
					mobCustom.getEffects().forEach((effect, amplifier) -> {
						effectMobs.append(effect.getName() + " - "+ amplifier +", ");
					});
					
					
					if (mobCustom.getArmor().getHelmet() != null)
						inventory.append(mobCustom.getArmor().getHelmet().getType().name() + ", ");
					if (mobCustom.getArmor().getChestplate() != null)
						inventory.append(mobCustom.getArmor().getChestplate().getType().name() + ", ");
					if (mobCustom.getArmor().getLeggings() != null)
						inventory.append(mobCustom.getArmor().getLeggings().getType().name() + ", ");
					if (mobCustom.getArmor().getBoots() != null)
						inventory.append(mobCustom.getArmor().getBoots().getType().name() + ", ");
					if (mobCustom.getArmor().getArmorSword() != null)
						inventory.append(mobCustom.getArmor().getArmorSword().getType().name() + ",");
					
				}
				
				textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.AQUA + "DisplayName: " + ChatColor.GRAY + mobCustom.getDisplayName() + "\n"
						+ ChatColor.AQUA + "Entité: " + ChatColor.GRAY + entityName + "\n"
						+ ChatColor.AQUA + "Points: " + ChatColor.GRAY + (mobCustom.getType() == TypeMob.BASIC ? mobCustom.getPoints() : mobAssemblyPoint) + "\n"
						+ ChatColor.AQUA + (mobCustom.getType() == TypeMob.BASIC ? "Effets: " + ChatColor.GRAY + (mobCustom.getEffects().size() > 0 ? effectMobs.toString() : "Aucun") : "") + "\n"
						+ ChatColor.AQUA + (mobCustom.getType() == TypeMob.BASIC ? "Armure: " + ChatColor.GRAY + (mobCustom.getArmorInventory().length() > 0 ? inventory.toString() : "Aucun") : "") + "\n")
						.create()));
				
				playerSender.spigot().sendMessage(textComponent);
			}
		});
	}

    public static void removeRound(CMWRound round) {
		EnderCubeCMW.INSTANCE.getRounds().removeIf(cmwRound -> cmwRound.getId().equals(round.getId()));

		JSONArray rounds = config();
		for (int i = 0; i < rounds.length(); i++) {
			JSONObject mobJson = (JSONObject) rounds.get(i);
			if (UUID.fromString(mobJson.getString("id")).equals(round.getId())) {
				rounds.remove(i);
				break;
			}
		}

		saveFileAndReloadRounds(rounds.toString());
    }

	public static void removeMobInRound(CMWRound round, CMWMobCustom mobCustom) {
		round.getMobsRound().removeIf(mobRound -> mobRound.getId().equals(mobCustom.getId()));

		JSONArray rounds = config();
		for (int i = 0; i < rounds.length(); i++) {
			JSONObject mobJson = (JSONObject) rounds.get(i);
			if (UUID.fromString(mobJson.getString("id")).equals(round.getId())) {
				for (int j = 0; j < mobJson.getJSONArray("mobs").length(); j++) {
					JSONObject mobRoundJson = mobJson.getJSONArray("mobs").getJSONObject(j);
					if (UUID.fromString(mobRoundJson.getString("id")).equals(mobCustom.getId())) {
						mobJson.getJSONArray("mobs").remove(j);
						break;
					}
				}
				break;
			}
		}

		saveFileAndReloadRounds(rounds.toString());
	}

	public static void setAmountMob(CMWRound round, CMWRound.MobRound mobRound, int amount) {
		mobRound.setNumber(amount);

		JSONArray rounds = config();
		for (int i = 0; i < rounds.length(); i++) {
			JSONObject mobJson = (JSONObject) rounds.get(i);
			if (UUID.fromString(mobJson.getString("id")).equals(round.getId())) {
				for (int j = 0; j < mobJson.getJSONArray("mobs").length(); j++) {
					JSONObject mobRoundJson = mobJson.getJSONArray("mobs").getJSONObject(j);
					if (UUID.fromString(mobRoundJson.getString("id")).equals(mobRound.getId())) {
						mobRoundJson.put("number", amount);
						break;
					}
				}
				break;
			}
		}

		saveFileAndReloadRounds(rounds.toString());
	}
}
