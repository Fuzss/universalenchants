package fuzs.universalenchants.util;

import fuzs.universalenchants.UniversalEnchants;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class StoredEnchantmentHelper {
    public static final String TAG_STORED_ENCHANTMENTS = UniversalEnchants.id("stored_enchantments").toString();
    public static final Comparator<Enchantment> ENCHANTMENT_COMPARATOR = Comparator.comparing(enchantment -> getEnchantmentName(enchantment)
            .getString());

    public static boolean hasStoredEnchantments(ItemStack itemStack) {
        if (itemStack.getTag() != null && itemStack.getTag().contains(TAG_STORED_ENCHANTMENTS, Tag.TAG_LIST)) {
            return !itemStack.getTag().getList(TAG_STORED_ENCHANTMENTS, Tag.TAG_COMPOUND).isEmpty();
        } else {
            return false;
        }
    }

    public static ListTag getStoredEnchantments(ItemStack itemStack) {
        return itemStack.getTag() != null ?
                itemStack.getTag().getList(TAG_STORED_ENCHANTMENTS, Tag.TAG_COMPOUND) :
                new ListTag();
    }

    public static Map<Enchantment, Integer> getAllEnchantments(ItemStack itemStack) {
        Map<Enchantment, Integer> enchantments = new TreeMap<>(ENCHANTMENT_COMPARATOR);
        enchantments.putAll(EnchantmentHelper.deserializeEnchantments(itemStack.getEnchantmentTags()));
        enchantments.putAll(EnchantmentHelper.deserializeEnchantments(getStoredEnchantments(itemStack)));
        return enchantments;
    }

    /**
     * Similar to {@link EnchantmentHelper#setEnchantments(Map, ItemStack)}.
     */
    public static void setStoredEnchantments(Map<Enchantment, Integer> enchantments, ItemStack itemStack) {
        ListTag listTag = serializeEnchantments(enchantments);
        if (listTag.isEmpty()) {
            itemStack.removeTagKey(TAG_STORED_ENCHANTMENTS);
        } else {
            itemStack.addTagElement(TAG_STORED_ENCHANTMENTS, listTag);
        }
    }

    /**
     * Extracted from {@link EnchantmentHelper#setEnchantments(Map, ItemStack)}.
     */
    public static ListTag serializeEnchantments(Map<Enchantment, Integer> enchantments) {
        ListTag listTag = new ListTag();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment != null) {
                int level = entry.getValue();
                listTag.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), level));
            }
        }

        return listTag;
    }

    /**
     * Similar to {@link Enchantment#getFullname(int)}.
     */
    public static Component getEnchantmentName(Enchantment enchantment) {
        MutableComponent mutableComponent = Component.translatable(enchantment.getDescriptionId());
        return enchantment.isCurse() ? mutableComponent.withStyle(ChatFormatting.RED) : mutableComponent;
    }
}
