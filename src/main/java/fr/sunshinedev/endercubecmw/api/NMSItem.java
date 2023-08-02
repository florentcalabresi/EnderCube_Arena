package fr.sunshinedev.endercubecmw.api;

import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;

public class NMSItem {

    public static ItemStack applyNBTTagFromJson(ItemStack itemStack, String nbtTag) {
        try {
            net.minecraft.world.item.ItemStack NmsItem = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound nbttagcompound = MojangsonParser.a(nbtTag);
            NmsItem.c(nbttagcompound);
            return CraftItemStack.asBukkitCopy(NmsItem);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
