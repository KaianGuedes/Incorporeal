package dev.war.incorporeal.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

public class SerializationUtil {
    public static final int FORMAT_BUKKIT = 2;

    public static byte[] serializeItems(Collection<ItemStack> items) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(bytes)) {
            out.writeInt(items.size());
            for (ItemStack item : items) {
                out.writeObject(item);
            }
        }
        return bytes.toByteArray();
    }

    public static ItemStack[] deserializeItems(byte[] data, Integer format) throws IOException {
        if (data == null || data.length == 0) {
            return new ItemStack[0];
        }
        if (format == null) {
            return deserializeLegacy(data);
        }
        if (format != FORMAT_BUKKIT) {
            throw new IOException("Unknown item format " + format);
        }
        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new ByteArrayInputStream(data))) {
            ItemStack[] items = new ItemStack[in.readInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) in.readObject();
            }
            return items;
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException(e);
        }
    }

    private static ItemStack[] deserializeLegacy(byte[] data) throws IOException {
        try {
            return (ItemStack[]) ItemStack.class.getMethod("deserializeItemsFromBytes", byte[].class).invoke(null, (Object) data);
        } catch (ReflectiveOperationException | LinkageError e) {
            throw new IOException("Chest from Incorporeal 0.1 needs a Paper server to be opened", e);
        }
    }
}
