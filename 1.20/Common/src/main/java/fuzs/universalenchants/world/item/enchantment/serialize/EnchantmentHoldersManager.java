package fuzs.universalenchants.world.item.enchantment.serialize;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import fuzs.puzzleslib.api.config.v3.json.JsonConfigFileUtil;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentDataProvider;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentDataTags;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.EnchantmentData2;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.EnchantmentDataKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class EnchantmentHoldersManager {

    public static void onLoadComplete() {

    }

    public static boolean isCompatibleWith(Enchantment enchantment, Enchantment other, boolean fallback) {
        return getHolder(enchantment).isCompatibleWith(other, fallback) && getHolder(other).isCompatibleWith(enchantment, fallback);
    }

    public static boolean canApplyAtEnchantingTable(Enchantment enchantment, Item item) {
        Objects.requireNonNull(enchantment, "enchantment is null");
        Objects.requireNonNull(item, "item is null");
        ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        ItemStack itemStack = new ItemStack(item);
        if (itemStack.is(EnchantmentDataTags.getTagKeyForDisabledItems(resourceLocation))) return false;
        return itemStack.is(EnchantmentDataTags.getTagKeyForItems(resourceLocation));
    }

    public static boolean canApplyAtAnvil(Enchantment enchantment, ItemStack itemStack) {
        Objects.requireNonNull(enchantment, "enchantment is null");
        Objects.requireNonNull(itemStack, "item stack is null");
        ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        if (itemStack.is(EnchantmentDataTags.getTagKeyForDisabledAnvilItems(resourceLocation))) return false;
        return itemStack.is(EnchantmentDataTags.getTagKeyForAnvilItems(resourceLocation));
    }
}
