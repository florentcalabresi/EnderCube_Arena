package fr.sunshinedev.endercubecmw.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.json.JSONArray;
import org.json.JSONObject;

public class CMWMobCustom {

	public UUID id;
    public EntityType entity;
    public String displayName;
    public JSONArray armorInventory;
    public ArmorMob armorMob;
    public int points;
    public double health;
    public Map<PotionEffectType, Integer> effects = new HashMap<>();
	private TypeMob typeMob;
	public Age ageMob = Age.ADULT;
	
	//ONLY ASSEMBLY
    public UUID belowMob;
	public UUID aboveMob;

    public CMWMobCustom(String dName) {
    	displayName = dName;
    }

	public UUID getId() {
		return id;
	}

	public void setId(UUID uuid) {
		this.id = uuid;
	}

	public EntityType getEntity() {
		return entity;
	}

	public void setEntity(EntityType entity) {
		this.entity = entity;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public JSONArray getArmorInventory() {
		return armorInventory;
	}

	public void setArmorInventory(JSONArray armorInventory) {
		this.armorInventory = armorInventory;
		this.armorMob = new ArmorMob();
	}
	
	public ArmorMob getArmor() {
		return armorMob;
	}

	public void setArmor(ArmorMob armorMobCustom) {
		this.armorMob = armorMobCustom;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}
	
	public double getHealth() {
		return health;
	}

	public void setHealth(double healthInt) {
		this.health = healthInt;
	}

	public TypeMob getType() {
		return typeMob;
	}
	
	public void setType(TypeMob type) {
		this.typeMob = type;
	}

	public UUID getBelowMob() {
		return belowMob;
	}

	public void setBelowMob(UUID belowMob) {
		this.belowMob = belowMob;
	}

	public UUID getAboveMob() {
		return aboveMob;
	}

	public void setAboveMob(UUID aboveMob) {
		this.aboveMob = aboveMob;
	}

	public Map<PotionEffectType, Integer> getEffects() {
		return effects;
	}

	public Age getAgeMob() {
		return ageMob;
	}

	public void setAgeMob(Age ageMob) {
		this.ageMob = ageMob;
	}

	public static enum TypeMob {
		
		BASIC("basic"),
		ASSEMBLY("assembly");
		
		private String name;

		TypeMob(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static enum Age {

		ADULT("ADULT"),
		BABY("BABY");

		private String name;

		Age(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public class ArmorMob {
		
		ItemStack armorHelmet = null;
		ItemStack armorChestplate = null;
		ItemStack armorLeggings = null;
		ItemStack armorBoots = null;
		ItemStack armorSword = null;
		
		public ArmorMob() {
			for(int i=0;i<getArmorInventory().length();i++) {
				JSONObject itemJson = getArmorInventory().getJSONObject(i);
				String nbtTag = "{}";
				if(itemJson.has("nbtTag"))
					nbtTag = itemJson.getString("nbtTag");
				if(itemJson.getInt("slot") == 10)
					armorHelmet = NMSItem.applyNBTTagFromJson(new ItemStack(Material.getMaterial(itemJson.getString("material")), itemJson.getInt("amount"), (short) itemJson.getInt("itemData")), nbtTag);
				else if(itemJson.getString("material").endsWith("_CHESTPLATE"))
					armorChestplate = NMSItem.applyNBTTagFromJson(new ItemStack(Material.getMaterial(itemJson.getString("material")), itemJson.getInt("amount"), (short) itemJson.getInt("itemData")), nbtTag);
				else if(itemJson.getString("material").endsWith("_LEGGINGS"))
					armorLeggings = NMSItem.applyNBTTagFromJson(new ItemStack(Material.getMaterial(itemJson.getString("material")), itemJson.getInt("amount"), (short) itemJson.getInt("itemData")), nbtTag);
				else if(itemJson.getString("material").endsWith("_BOOTS"))
					armorBoots = NMSItem.applyNBTTagFromJson(new ItemStack(Material.getMaterial(itemJson.getString("material")), itemJson.getInt("amount"), (short) itemJson.getInt("itemData")), nbtTag);
				else if(itemJson.getInt("slot") == 14)
					armorSword = NMSItem.applyNBTTagFromJson(new ItemStack(Material.getMaterial(itemJson.getString("material")), itemJson.getInt("amount"), (short) itemJson.getInt("itemData")), nbtTag);
			}
		}

		public ItemStack getHelmet() {
			return armorHelmet;
		}

		public ItemStack getChestplate() {
			return armorChestplate;
		}

		public ItemStack getLeggings() {
			return armorLeggings;
		}

		public ItemStack getBoots() {
			return armorBoots;
		}

		public ItemStack getArmorSword() {
			return armorSword;
		}
	}
    
}
