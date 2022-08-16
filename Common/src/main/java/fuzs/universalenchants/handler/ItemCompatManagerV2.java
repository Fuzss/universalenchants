package fuzs.universalenchants.handler;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemCompatManagerV2 {
    private static final EnchantmentCategory[] VANILLA_ENCHANTMENT_CATEGORY_VALUES = {EnchantmentCategory.ARMOR, EnchantmentCategory.ARMOR_FEET, EnchantmentCategory.ARMOR_LEGS, EnchantmentCategory.ARMOR_CHEST, EnchantmentCategory.ARMOR_HEAD, EnchantmentCategory.WEAPON, EnchantmentCategory.DIGGER, EnchantmentCategory.FISHING_ROD, EnchantmentCategory.TRIDENT, EnchantmentCategory.BREAKABLE, EnchantmentCategory.BOW, EnchantmentCategory.WEARABLE, EnchantmentCategory.CROSSBOW, EnchantmentCategory.VANISHABLE};
    public static final BiMap<EnchantmentCategory, ResourceLocation> ENCHANTMENT_CATEGORIES_BY_ID = Stream.of(VANILLA_ENCHANTMENT_CATEGORY_VALUES).collect(Collectors.toMap(Function.identity(), category -> new ResourceLocation(category.name().toLowerCase(Locale.ROOT)), (o1, o2) -> o1, () -> EnumHashBiMap.create(EnchantmentCategory.class)));
}
