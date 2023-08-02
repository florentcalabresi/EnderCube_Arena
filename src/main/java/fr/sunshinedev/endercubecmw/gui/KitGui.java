package fr.sunshinedev.endercubecmw.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.sunshinedev.endercubecmw.EnderCubeCMW;
import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.api.CMWKit;
import fr.sunshinedev.endercubecmw.api.NMSItem;
import fr.sunshinedev.endercubecmw.managers.KitsManager;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.SlotElement;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

public class KitGui {

    public static final Material ITEM_DEFAULT = Material.GRAY_STAINED_GLASS_PANE;
    private final CMWKit kitTarget;
 
    Gui gui = null;
    Window window = null;

    public KitGui(Player p, CMWKit kit) {
        kitTarget = kit;

        gui = getStructure(new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR));

        window = Window.single()
                .setViewer(p)
                .setTitle("Kit - "+kitTarget.getName())
                .setGui(gui)
                .addCloseHandler(new Runnable() {
                    @Override
                    public void run() {
                        Inventory inventory = p.getOpenInventory().getTopInventory();
                        KitsManager.saveInventory(kit, inventory);
                    }
                })
                .build();
    }

    public KitGui(CMWKit kit) {
        kitTarget = kit;

        JSONArray inventory = kit.getInventory();

        ItemStack armorHelmet = new ItemStack(Material.AIR);
        ItemStack armorChestplate = new ItemStack(Material.AIR);
        ItemStack armorLeggings = new ItemStack(Material.AIR);
        ItemStack armorBoots = new ItemStack(Material.AIR);
        for(int i = 0; i<inventory.length(); i++) {
            JSONObject itemObj = inventory.getJSONObject(i);
            Material material = Material.getMaterial(itemObj.getString("material"));

            String tag = "{}";
            if(itemObj.has("nbtTag")) tag = itemObj.getString("nbtTag");
            if(itemObj.getInt("slot") == 1) {
                armorHelmet = NMSItem.applyNBTTagFromJson(new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")), tag);
            }else if(itemObj.getInt("slot") == 2) {
                armorChestplate = NMSItem.applyNBTTagFromJson(new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")), tag);
            }else if(itemObj.getInt("slot") == 3) {
                armorLeggings = NMSItem.applyNBTTagFromJson(new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")), tag);
            }else if(itemObj.getInt("slot") == 4) {
                armorBoots = NMSItem.applyNBTTagFromJson(new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")), tag);
            }


        }

        gui = getStructure(armorHelmet, armorChestplate, armorLeggings, armorBoots);

        for(int i = 0; i<inventory.length(); i++) {
            JSONObject itemObj = inventory.getJSONObject(i);
            Material material = Material.getMaterial(itemObj.getString("material"));
            int slot = itemObj.getInt("slot");
            String tag = "{}";
            if(itemObj.has("nbtTag")) tag = itemObj.getString("nbtTag");
            if(slot != 1 && slot != 2 && slot != 3 && slot != 4) {
                ItemStack item = NMSItem.applyNBTTagFromJson(new ItemStack(material, itemObj.getInt("amount"), (short) itemObj.getInt("itemData")), tag);
                gui.setItem(slot, new PlaceItem(item));
            }
        }
    }

    public Gui getGui() {
        return gui;
    }

    public Window getWindow() {
        return window;
    }

    public Gui getStructure(ItemStack armorHelmet, ItemStack armorChestplate, ItemStack armorLeggings, ItemStack armorBoots) {
        return Gui.normal()
                .setStructure(
                        "# 1 2 3 4 # . # #",
                        "# # # # # # @ # #",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .",
                        ". . . . . . . . .")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Utils.OutGuiItemStack())))
                .addIngredient('@', new DoneKit())
                .addIngredient('1', new PlaceItem("_HELMET", armorHelmet))
                .addIngredient('2', new PlaceItem("_CHESTPLATE", armorChestplate))
                .addIngredient('3', new PlaceItem("_LEGGINGS", armorLeggings))
                .addIngredient('4', new PlaceItem("_BOOTS", armorBoots))
                .addIngredient('.', new PlaceItem(new ItemStack(Material.AIR)))
                .build();
    }

    public void show(Player p) {
        window = Window.single()
                .setViewer(p)
                .setTitle("Kit - "+kitTarget.getName())
                .setGui(gui)
                .addCloseHandler(new Runnable() {
                    @Override
                    public void run() {
                        Inventory inventory = p.getOpenInventory().getTopInventory();
                        KitsManager.saveInventory(kitTarget, inventory);
                    }
                })
                .build();

        window.open();
    }

    public class PlaceItem extends SimpleItem {

        String allowed = null;

        public PlaceItem(ItemStack itemStack) {
            super(new ItemBuilder(itemStack));
        }

        public PlaceItem(String allow, ItemStack itemStack) {
            super(new ItemBuilder(itemStack));
            allowed = allow;
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

            if (!clickType.isRightClick()) {
                ItemStack itemCursor = event.getCursor();
                ItemStack itemSlot = event.getCurrentItem();

                if (itemCursor == null && itemSlot == null) return;

                if (allowed != null && !itemCursor.getType().name().endsWith(allowed) && itemCursor.getType() != Material.AIR)
                    return;

                gui.setItem(event.getSlot(), new PlaceItem(allowed, itemCursor));
                event.setCursor(itemSlot != null ? itemSlot : new ItemStack(Material.AIR));
            }

            notifyWindows();
        }
    }

    public class DoneKit extends SimpleItem {

        public DoneKit() {
            super(new ItemBuilder(Utils.DoneItemStack()));;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Utils.DoneItemStack());
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {

            if (clickType.isLeftClick()) {
                KitsManager.saveInventory(kitTarget, event.getInventory());
                window.close();
                Utils.sendPlayerMessageSuccess(player, "Le kit a bien été mis à jour.");
            }

            notifyWindows();
        }

    }

}
