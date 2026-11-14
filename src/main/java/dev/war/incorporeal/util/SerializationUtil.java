package dev.war.incorporeal.util;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class SerializationUtil {
    public static byte[] serializeItems(Collection<ItemStack> items) {
        if (items == null || items.isEmpty()) {
            return new byte[0];
        }

        try {
            return ItemStack.serializeItemsAsBytes(items);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static ItemStack[] deserializeItems(byte[] data) {
        if (data == null || data.length == 0) {
            return new ItemStack[0];
        }

        try {
            return ItemStack.deserializeItemsFromBytes(data);
        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack[0];
        }
    }
}