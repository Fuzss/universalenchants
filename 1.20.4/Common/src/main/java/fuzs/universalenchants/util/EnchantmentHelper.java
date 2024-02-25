package fuzs.universalenchants.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public class EnchantmentHelper {

    public static Map<Enchantment, Integer> getEnchantments(ItemStack itemStack) {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(itemStack);
    }

    public static ItemStack addEnchantments(Map<Enchantment, Integer> enchantments, ItemStack itemStack, boolean override) {
        Map<Enchantment, Integer> oldEnchantments = getEnchantments(itemStack);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            if (override) {
                oldEnchantments.put(entry.getKey(), entry.getValue());
            } else {
                oldEnchantments.merge(entry.getKey(), entry.getValue(), Math::max);
            }
        }
        return setEnchantments(enchantments, itemStack);
    }

    public static ItemStack setEnchantments(Map<Enchantment, Integer> enchantments, ItemStack itemStack) {

        ListTag listTag = new ListTag();

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            if (entry.getKey() != null && entry.getValue() > 0) {
                ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(entry.getKey());
                listTag.add(net.minecraft.world.item.enchantment.EnchantmentHelper.storeEnchantment(resourceLocation, entry.getValue()));
            }
        }

        itemStack.removeTagKey(ItemStack.TAG_ENCH);
        itemStack.removeTagKey(EnchantedBookItem.TAG_STORED_ENCHANTMENTS);
        itemStack = getEnchantedStack(itemStack, !listTag.isEmpty());

        if (!listTag.isEmpty()) {
            String tagKey = itemStack.getItem() instanceof EnchantedBookItem ? EnchantedBookItem.TAG_STORED_ENCHANTMENTS : ItemStack.TAG_ENCH;
            itemStack.addTagElement(tagKey, listTag);
        }

        return itemStack;
    }

    public static ItemStack getEnchantedStack(ItemStack itemStack, boolean isEnchanted) {
        ItemStack newStack;
        if (itemStack.getItem() instanceof EnchantedBookItem && !isEnchanted) {
            newStack = new ItemStack(Items.BOOK, itemStack.getCount());
        } else if (itemStack.getItem() instanceof BookItem && isEnchanted) {
            newStack = new ItemStack(Items.ENCHANTED_BOOK, itemStack.getCount());
        } else {
            return itemStack;
        }
        CompoundTag tag = itemStack.getTag();
        if (tag != null) {
            newStack.setTag(tag.copy());
        }
        return newStack;
    }
}
