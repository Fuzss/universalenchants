package fuzs.universalenchants.handler;

import fuzs.puzzleslib.api.init.v3.RegistryHelper;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.world.item.enchantment.EnchantmentCategoryManager;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentDataTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Objects;

public class EnchantmentDataHandler {

    public static void onLoadComplete() {
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            EnchantmentCategory enchantmentCategory = EnchantmentCategoryManager.createEnchantmentCategory(enchantment, item -> canApplyAtEnchantingTable(enchantment, item));
            EnchantmentCategoryManager.setEnchantmentCategory(enchantment, enchantmentCategory);
        }
    }

    public static boolean canApplyAtEnchantingTable(Enchantment enchantment, Item item) {
        Objects.requireNonNull(enchantment, "enchantment is null");
        Objects.requireNonNull(item, "item is null");
        if (!UniversalEnchants.CONFIG.getHolder(ServerConfig.class).isAvailable() || !UniversalEnchants.CONFIG.get(ServerConfig.class).adjustEnchantingTableEnchantments) {
            return EnchantmentCategoryManager.getVanillaCategory(enchantment).canEnchant(item);
        }
        ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        ItemStack itemStack = new ItemStack(item);
        if (itemStack.is(EnchantmentDataTags.getDisallowAtEnchantingTable(resourceLocation))) return false;
        return itemStack.is(EnchantmentDataTags.getAllowAtEnchantingTable(resourceLocation));
    }

    public static boolean canApplyAtAnvil(Enchantment enchantment, ItemStack itemStack) {
        Objects.requireNonNull(enchantment, "enchantment is null");
        Objects.requireNonNull(itemStack, "item stack is null");
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).adjustAnvilEnchantments) return enchantment.canEnchant(itemStack);
        ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        if (itemStack.is(EnchantmentDataTags.getDisallowAtAnvil(resourceLocation))) return false;
        return itemStack.is(EnchantmentDataTags.getAllowAtAnvil(resourceLocation));
    }

    public static boolean isCompatibleWith(Enchantment enchantment, Enchantment other) {
        Objects.requireNonNull(enchantment, "enchantment is null");
        Objects.requireNonNull(other, "other enchantment is null");
        return enchantment != other && !isIncompatibleWith(enchantment, other) && !isIncompatibleWith(other, enchantment);
    }

    private static boolean isIncompatibleWith(Enchantment enchantment, Enchantment other) {
        ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        if (RegistryHelper.is(EnchantmentDataTags.getIsNotIncompatible(resourceLocation), other)) return false;
        return RegistryHelper.is(EnchantmentDataTags.getIsIncompatible(resourceLocation), other);
    }
}
