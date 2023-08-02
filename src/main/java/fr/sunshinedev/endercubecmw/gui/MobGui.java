package fr.sunshinedev.endercubecmw.gui;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.sunshinedev.endercubecmw.Utils;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom;
import fr.sunshinedev.endercubecmw.api.CMWMobCustom.TypeMob;
import fr.sunshinedev.endercubecmw.api.NMSItem;
import fr.sunshinedev.endercubecmw.managers.MobsManager;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

public class MobGui {
 
    Gui gui = null;
    Window window = null;
    CMWMobCustom mobCustom = null;

    public MobGui(String displayName, EntityType entityType, Player p) {
        mobCustom = new CMWMobCustom(displayName);
        mobCustom.setId(UUID.randomUUID());
        mobCustom.setEntity(entityType);
        mobCustom.setType(TypeMob.BASIC);
        gui = structureArmorMob();
        openGui(gui, "Mob - "+displayName, p);
    }
    
    public MobGui(CMWMobCustom mob, Player p) {
        mobCustom = mob;
        gui = structureArmorMobExist();
        openGui(gui, "Mob - "+mobCustom.getDisplayName(), p);
    }

    public Gui getGui() {
        return gui;
    }

    public Window getWindow() {
        return window;
    }

    public void openGui(Gui gui, String title, Player p) {
        window = Window.single()
                .setViewer(p)
                .setTitle(title)
                .setGui(gui)
                .build();

        window.open();
    }

    public Gui structureArmorMobExist() {
    	
    	ItemStack helmet = new ItemStack(Material.AIR);
    	ItemStack chestplate = new ItemStack(Material.AIR);
    	ItemStack leggings = new ItemStack(Material.AIR);
    	ItemStack boots = new ItemStack(Material.AIR);
    	ItemStack sword = new ItemStack(Material.AIR);
    	
    	if(mobCustom.getArmor() != null) {
    		
    		if (mobCustom.getArmor().getHelmet() != null)
    			helmet = mobCustom.getArmor().getHelmet();
    		if (mobCustom.getArmor().getChestplate() != null)
    			chestplate = mobCustom.getArmor().getChestplate();
    		if (mobCustom.getArmor().getLeggings() != null)
    			leggings = mobCustom.getArmor().getLeggings();
    		if (mobCustom.getArmor().getBoots() != null)
    			boots = mobCustom.getArmor().getBoots();
    		if (mobCustom.getArmor().getArmorSword() != null)
    			sword = mobCustom.getArmor().getArmorSword();
    	}
    	
    	
        return Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# H C L B S # D #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Utils.OutGuiItemStack())))
                .addIngredient('H', new PlaceItem(helmet))
                .addIngredient('C', new PlaceItem("_CHESTPLATE", chestplate))
                .addIngredient('L', new PlaceItem("_LEGGINGS", leggings))
                .addIngredient('B', new PlaceItem("_BOOTS", boots))
                .addIngredient('S', new PlaceItem(sword))
                .addIngredient('D', new DoneItem(true))
                .build();
    }
    
    public Gui structureArmorMob() {
        return Gui.normal()
                .setStructure(
                        "# # # # # # # # #",
                        "# H C L B S # D #",
                        "# # # # # # # # #")
                .addIngredient('#', new SimpleItem(new ItemBuilder(Utils.OutGuiItemStack())))
                .addIngredient('H', new PlaceItem(new ItemStack(Material.AIR)))
                .addIngredient('C', new PlaceItem("_CHESTPLATE", new ItemStack(Material.AIR)))
                .addIngredient('L', new PlaceItem("_LEGGINGS", new ItemStack(Material.AIR)))
                .addIngredient('B', new PlaceItem("_BOOTS", new ItemStack(Material.AIR)))
                .addIngredient('S', new PlaceItem(new ItemStack(Material.AIR)))
                .addIngredient('D', new DoneItem(false))
                .build();
    }

    public ItemBuilder getMobType(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        item.getItemMeta().setDisplayName(displayName);
        return new ItemBuilder(item);
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

    public class DoneItem extends SimpleItem {

    	boolean exist;
    	
        public DoneItem(boolean exist) {
            super(new ItemBuilder(Utils.DoneItemStack()));
            this.exist = exist;
        }

        @Override
        public ItemProvider getItemProvider() {
            return new ItemBuilder(Utils.DoneItemStack());
        }

        @Override
        public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
            if (clickType.isLeftClick()) {
                Inventory inventory = event.getInventory();
                for(ItemStack item : inventory.getContents())
                	if(item != null)
                		if(item.getType() == Material.GRAY_STAINED_GLASS_PANE || item.getType() == Material.GREEN_STAINED_GLASS_PANE)
                			inventory.remove(item);
                
                MobsManager.saveMob(mobCustom, event.getInventory(), this.exist);

                Utils.sendPlayerMessageSuccess(player, "Le mob a bien été mis à jour.");
                
                window.close();
            }
        }

        

    }

}
