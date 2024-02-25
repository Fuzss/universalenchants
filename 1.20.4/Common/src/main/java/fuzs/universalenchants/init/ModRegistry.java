package fuzs.universalenchants.init;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import fuzs.puzzleslib.api.capability.v2.CapabilityController;
import fuzs.puzzleslib.api.capability.v2.data.CapabilityKey;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.capability.ArrowLootingCapabilityImpl;
import fuzs.universalenchants.core.CommonAbstractions;
import fuzs.universalenchants.mixin.accessor.FeatureFlagAccessor;
import fuzs.universalenchants.world.item.enchantment.EnchantmentFeature;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlagUniverse;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Locale;

public class ModRegistry {
    public static final FeatureFlagSet DISABLED_FEATURE_FLAG_SET = FeatureFlagSet.of(FeatureFlagAccessor.universalenchants$callInit(new FeatureFlagUniverse(UniversalEnchants.MOD_ID), 0));
    public static final String ENCHANTMENT_CATEGORY_PREFIX = UniversalEnchants.MOD_NAME.toUpperCase(Locale.ROOT).replace(" ", "_") + "_";
    public static final EnchantmentCategory SHEARS_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "SHEARS", item -> item instanceof ShearsItem);
    public static final EnchantmentCategory AXE_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "AXE", item -> item instanceof AxeItem);
    public static final EnchantmentCategory HORSE_ARMOR_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "HORSE_ARMOR", item -> item instanceof HorseArmorItem);
    public static final EnchantmentCategory SHIELD_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "SHIELD", item -> item instanceof ShieldItem);
    static final CapabilityController CAPABILITIES = CapabilityController.from(UniversalEnchants.MOD_ID);
    public static final CapabilityKey<ArrowLootingCapability> ARROW_LOOTING_CAPABILITY = CAPABILITIES.registerEntityCapability("arrow_looting", ArrowLootingCapability.class, entity -> new ArrowLootingCapabilityImpl(), AbstractArrow.class);

    public static void touch() {
        if (true) return;
        Unsafe unsafe;
        try {
            Field theUnsafe = null;
            theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        Field filteredRegistriesField = Iterators.forArray(FeatureElement.class.getFields()).next();
        long filteredRegistriesOffset = unsafe.staticFieldOffset(filteredRegistriesField);
        Object filteredRegistriesBase = unsafe.staticFieldBase(filteredRegistriesField);
        unsafe.putObject(filteredRegistriesBase, filteredRegistriesOffset, Sets.newHashSet(FeatureElement.FILTERED_REGISTRIES));
        FeatureElement.FILTERED_REGISTRIES.add((ResourceKey<? extends Registry<? extends FeatureElement>>) (ResourceKey<?>) Registries.ENCHANTMENT);
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            if (enchantment.getMaxLevel() == 1) {
                ((EnchantmentFeature) enchantment).universalenchants$setRequiredFeatures(ModRegistry.DISABLED_FEATURE_FLAG_SET);
            }
        }
    }
}
