package fr.sunshinedev.endercubecmw.managers;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;

import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeDefaults;
import net.minecraft.world.entity.ai.attributes.AttributeMapBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifiable;
import net.minecraft.world.entity.ai.attributes.AttributeProvider;
import net.minecraft.world.entity.animal.EntityWolf;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_20_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.api.CMWArena;
import fr.sunshinedev.endercubecmw.api.CMWGame;
import fr.sunshinedev.endercubecmw.api.CMWGame.MobAlive;
import fr.sunshinedev.endercubecmw.api.CMWKit;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.EntityLiving;

public class MobsManager {


	public static void saveMob(CMWMobCustom mob, Inventory inventory, boolean exist) {

		EnderCubeCMW.INSTANCE.getMobsCustom().add(mob);

		JSONObject mobJson = new JSONObject();
		mobJson.put("id", mob.getId());
		mobJson.put("type", mob.getType());
		mobJson.put("displayName", mob.getDisplayName());
		switch (mob.getType()) {
			case BASIC:
				mobJson.put("entity", mob.getEntity().name());
				mobJson.put("armorInventory", inventory != null ? armorInventoryToJSON(inventory) : new JSONArray());
				mobJson.put("points", 0);
				mobJson.put("health", -1);
				mobJson.put("effects", new JSONArray());
				mobJson.put("age", CMWMobCustom.Age.ADULT.getName());
				break;
			case ASSEMBLY:
				mobJson.put("belowEntityID", mob.getBelowMob());
				mobJson.put("aboveEntityID", mob.getAboveMob());
				break;
			default:
				break;
		}

		JSONArray configMobs = configMobs();

		if (exist) {
			for (int i = 0; i < configMobs.length(); i++) {
				JSONObject mobJsonExist = (JSONObject) configMobs.get(i);
				if (UUID.fromString(mobJsonExist.getString("id")).equals(mob.getId())) {
					mobJsonExist.put("armorInventory",
							inventory != null ? armorInventoryToJSON(inventory) : new JSONArray());
					break;
				}
			}
		} else
			configMobs.put(mobJson);

		saveFileAndReloadMobs(configMobs.toString());
	}

	public static JSONArray configMobs() {
		try {
			InputStream targetStream = new FileInputStream(EnderCubeCMW.INSTANCE.mobsFile);
			return new JSONArray(IOUtils.toString(targetStream, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveFileAndReloadMobs(String json) {
		try {
			FileWriter mobsWriter = new FileWriter(EnderCubeCMW.INSTANCE.mobsFile);
			mobsWriter.write(json);
			mobsWriter.close();

			EnderCubeCMW.INSTANCE.reloadMobs();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Optional<CMWMobCustom> getMobFromName(String name) {
		return EnderCubeCMW.INSTANCE.getMobsCustom().stream()
				.filter(cmwMob -> cmwMob.getDisplayName().equalsIgnoreCase(name)).findFirst();
	}

	public static Optional<CMWMobCustom> getMobFromId(UUID id) {
		return EnderCubeCMW.INSTANCE.getMobsCustom().stream().filter(cmwMob -> cmwMob.getId().equals(id)).findFirst();
	}

	public static void setPoints(CMWMobCustom cmwMob, int points) {
		cmwMob.setPoints(points);

		JSONArray mobs = configMobs();
		for (int i = 0; i < mobs.length(); i++) {
			JSONObject mob = (JSONObject) mobs.get(i);
			if (UUID.fromString(mob.getString("id")).equals(cmwMob.getId())) {
				mob.put("points", points);
				break;
			}
		}

		saveFileAndReloadMobs(mobs.toString());
	}

	public static JSONArray armorInventoryToJSON(Inventory inv) {
		JSONArray items = new JSONArray();
		for (int i = 0; i < inv.getContents().length; i++) {
			JSONObject itemJson = new JSONObject();
			ItemStack itemStack = inv.getContents()[i];

			if (itemStack != null) {
				if (itemStack.getType() != Material.AIR && itemStack.getType() != Material.LIME_STAINED_GLASS_PANE) {

					net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
					nmsStack.a(nmsStack.v());

					itemJson.put("slot", i);
					itemJson.put("material", itemStack.getType());
					itemJson.put("amount", itemStack.getAmount());
					itemJson.put("itemData", itemStack.getDurability());
					itemJson.put("nbtTag", nmsStack.v());

					items.put(itemJson);
				}
			}
		}
		return items;
	}

	public static Entity summonMob(Location loc, CMWMobCustom mobTarget) {
		Entity entity = loc.getWorld().spawnEntity(loc, mobTarget.getEntity());
		entity.setCustomName(mobTarget.getDisplayName());
		entity.getPersistentDataContainer().set(new NamespacedKey(EnderCubeCMW.INSTANCE, "cmwMob"), PersistentDataType.STRING, "cmwMob");
		entity.setPersistent(true);

		if (EnderCubeCMW.INSTANCE.getGame() != null) {
			CMWGame game = EnderCubeCMW.INSTANCE.getGame();
			game.getMobsAlive().add(new MobAlive(entity.getUniqueId(), mobTarget));
		}

		if (entity instanceof LivingEntity) {

			LivingEntity livEntity = (LivingEntity) entity;
			livEntity.setRemoveWhenFarAway(false);


			/*Player player = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);

			if(((LivingEntity) entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
				((Creature) entity).setTarget(player);
				((Creature) entity).attack(player);
			}*/

			if (entity instanceof Ageable) {
				Ageable castedEntity = (Ageable) entity;
				if(mobTarget.getAgeMob() == CMWMobCustom.Age.BABY)
					castedEntity.setBaby();
			}

			if (entity instanceof Piglin) {
				((Piglin) entity).setImmuneToZombification(true);
			}

			if (entity instanceof Hoglin) {
				((Hoglin) entity).setImmuneToZombification(true);
			}

			mobTarget.getEffects().forEach((potion, amplifier) -> {
				livEntity.addPotionEffect(new PotionEffect(potion, -1, amplifier));
			});

			if (mobTarget.getHealth() > 0) {
				System.out.println("Set health " + mobTarget.getHealth());
				((LivingEntity) entity).setMaxHealth(mobTarget.getHealth());
				((LivingEntity) entity).setHealth(mobTarget.getHealth());
			}

			if (mobTarget.getArmor().getHelmet() != null)
				livEntity.getEquipment().setHelmet(mobTarget.getArmor().getHelmet());
			if (mobTarget.getArmor().getChestplate() != null)
				livEntity.getEquipment().setChestplate(mobTarget.getArmor().getChestplate());
			if (mobTarget.getArmor().getLeggings() != null)
				livEntity.getEquipment().setLeggings(mobTarget.getArmor().getLeggings());
			if (mobTarget.getArmor().getBoots() != null)
				livEntity.getEquipment().setBoots(mobTarget.getArmor().getBoots());
			if (mobTarget.getArmor().getArmorSword() != null)
				livEntity.getEquipment().setItemInMainHand(mobTarget.getArmor().getArmorSword());
		}
		
		return entity;
	}
	

	public static void rideMobs(Location loc, CMWMobCustom mobTarget) {
		Optional<CMWMobCustom> mobBelowOpt = MobsManager.getMobFromId(mobTarget.getBelowMob());
        Optional<CMWMobCustom> mobAboveOpt = MobsManager.getMobFromId(mobTarget.getAboveMob());
        if(mobBelowOpt.isPresent() && mobAboveOpt.isPresent()) {
        	
        	Entity entityBelow = summonMob(loc, mobBelowOpt.get());
        	Entity entityAbove = summonMob(loc, mobAboveOpt.get());
        	
        	entityBelow.addPassenger(entityAbove);
        	
        	
        }
	}

	public static void addEffect(CMWMobCustom mob, PotionEffectType eff, int amplifNumber) {
		mob.getEffects().put(eff, amplifNumber);

		JSONArray mobs = configMobs();
		for (int i = 0; i < mobs.length(); i++) {
			JSONObject mobJson = (JSONObject) mobs.get(i);
			if (UUID.fromString(mobJson.getString("id")).equals(mob.getId())) {
				JSONObject effect = new JSONObject();
				effect.put("name", eff.getName());
				effect.put("amplifier", amplifNumber);
				mobJson.getJSONArray("effects").put(effect);
				break;
			}
		}

		saveFileAndReloadMobs(mobs.toString());
	}

	public static void removeMob(CMWMobCustom mob) {
		EnderCubeCMW.INSTANCE.getMobsCustom().removeIf(cmwMobCustom -> cmwMobCustom.getId().equals(mob.getId()));

		JSONArray mobs = configMobs();
		for (int i = 0; i < mobs.length(); i++) {
			JSONObject mobJson = (JSONObject) mobs.get(i);
			if (UUID.fromString(mobJson.getString("id")).equals(mob.getId())) {
				mobs.remove(i);
				break;
			}
		}

		saveFileAndReloadMobs(mobs.toString());
	}

	public static void removeEffect(CMWMobCustom mob, PotionEffectType eff) {
		mob.getEffects().remove(eff);

		JSONArray mobs = configMobs();
		for (int i = 0; i < mobs.length(); i++) {
			JSONObject mobJson = (JSONObject) mobs.get(i);
			if (UUID.fromString(mobJson.getString("id")).equals(mob.getId())) {
				for (int j = 0; j < mobJson.getJSONArray("effects").length(); j++) {
					JSONObject effect = mobJson.getJSONArray("effects").getJSONObject(j);
					if (effect.getString("name").equalsIgnoreCase(eff.getName()))
						mobJson.getJSONArray("effects").remove(j);
				}
				break;
			}
		}

		saveFileAndReloadMobs(mobs.toString());
	}

	public static void setHealth(CMWMobCustom cmwMob, double healthInt) {
		cmwMob.setHealth(healthInt);

		JSONArray mobs = configMobs();
		for (int i = 0; i < mobs.length(); i++) {
			JSONObject mob = (JSONObject) mobs.get(i);
			if (UUID.fromString(mob.getString("id")).equals(cmwMob.getId())) {
				mob.put("health", healthInt);
				break;
			}
		}

		saveFileAndReloadMobs(mobs.toString());
	}

	public static void setAge(CMWMobCustom cmwMob, CMWMobCustom.Age ageMob) {
		cmwMob.setAgeMob(ageMob);

		JSONArray mobs = configMobs();
		for (int i = 0; i < mobs.length(); i++) {
			JSONObject mob = (JSONObject) mobs.get(i);
			if (UUID.fromString(mob.getString("id")).equals(cmwMob.getId())) {
				mob.put("age", ageMob.getName());
				break;
			}
		}

		saveFileAndReloadMobs(mobs.toString());
	}
}
