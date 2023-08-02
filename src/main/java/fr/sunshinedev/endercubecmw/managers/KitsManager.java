package fr.sunshinedev.endercubecmw.managers;

import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.api.CMWKit;
import fr.sunshinedev.endercubecmw.api.NMSItem;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.io.IOUtils;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class KitsManager {
    public static final Material ITEM_DEFAULT = Material.GRAY_STAINED_GLASS_PANE;
 
    public static void addKit(CMWKit cmwKit) {
        EnderCubeCMW.INSTANCE.getKits().add(cmwKit);

        JSONObject kit = new JSONObject();
        kit.put("id", cmwKit.getId());
        kit.put("name", cmwKit.getName());
        kit.put("inventory", new JSONArray());

        JSONArray configKit = configKit();
        configKit.put(kit);


        saveFileAndReloadKits(configKit.toString());

    }

    public static Optional<CMWKit> getKitFromName(String name) {
        return EnderCubeCMW.INSTANCE.getKits().stream().filter(cmwKit -> cmwKit.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static Optional<CMWKit> getKitFromID(UUID id) {
        return EnderCubeCMW.INSTANCE.getKits().stream().filter(cmwKit -> cmwKit.getId().equals(id)).findFirst();
    }

    public static JSONArray configKit() {
        try {
            InputStream targetStream = new FileInputStream(EnderCubeCMW.INSTANCE.kitsFile);
            return new JSONArray(IOUtils.toString(targetStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveInventory(CMWKit kitTarget, Inventory inventory) {
        JSONArray items = new JSONArray();
        for (int i = 0; i<inventory.getContents().length; i++) {
            JSONObject itemJson = new JSONObject();
            ItemStack itemStack = inventory.getContents()[i];

            if(itemStack != null) {
                if(itemStack.getType() != Material.AIR && itemStack.getType() != ITEM_DEFAULT && itemStack.getType() != Material.LIME_STAINED_GLASS_PANE) {

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

        JSONArray kits = configKit();
        for(int i = 0; i<kits.length(); i++) {
            JSONObject kit = (JSONObject) kits.get(i);
            if(kit.getString("name").equalsIgnoreCase(kitTarget.getName())){
                kit.remove("inventory");
                kit.put("inventory", items);
                break;
            }
        }
        saveFileAndReloadKits(kits.toString());
    }
    
    public static ItemStack armorKit(CMWKit kit, String place) {
    	ItemStack armor = new ItemStack(Material.AIR);
        
        for(int i = 0; i<kit.getInventory().length(); i++) {
            JSONObject itemObj = kit.getInventory().getJSONObject(i);
            Material material = Material.getMaterial(itemObj.getString("material"));

            String tag = "{}";
            if(itemObj.has("nbtTag")) tag = itemObj.getString("nbtTag");
            if(itemObj.getInt("slot") == 1 && place.equalsIgnoreCase("helmet")) {
            	armor = NMSItem.applyNBTTagFromJson(new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")), tag);
            }else if(itemObj.getInt("slot") == 2 && place.equalsIgnoreCase("chestplate")) {
            	armor = NMSItem.applyNBTTagFromJson(new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")), tag);
            }else if(itemObj.getInt("slot") == 3 && place.equalsIgnoreCase("leggings")) {
            	armor = NMSItem.applyNBTTagFromJson(new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")), tag);
            }else if(itemObj.getInt("slot") == 4 && place.equalsIgnoreCase("boots")) {
            	armor = NMSItem.applyNBTTagFromJson(new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")), tag);
            }else if(itemObj.getInt("slot") == 6 && place.equalsIgnoreCase("offHands")) {
            	armor = NMSItem.applyNBTTagFromJson(new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")), tag);
            }
        }
        
		return armor;
    }

    public static void removeKit(CMWKit cmwKit) {
        EnderCubeCMW.INSTANCE.getMobsCustom().removeIf(cmwKitFilter -> cmwKitFilter.getId().equals(cmwKit.getId()));

        JSONArray kits = configKit();
        for (int i = 0; i < kits.length(); i++) {
            JSONObject mobJson = (JSONObject) kits.get(i);
            if (UUID.fromString(mobJson.getString("id")).equals(cmwKit.getId())) {
                kits.remove(i);
                break;
            }
        }

        saveFileAndReloadKits(kits.toString());
    }

    private static void saveFileAndReloadKits(String json) {
        try {
            FileWriter mobsWriter = new FileWriter(EnderCubeCMW.INSTANCE.kitsFile);
            mobsWriter.write(json);
            mobsWriter.close();

            EnderCubeCMW.INSTANCE.reloadKits();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
