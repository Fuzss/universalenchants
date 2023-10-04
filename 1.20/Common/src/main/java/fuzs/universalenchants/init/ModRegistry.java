package fuzs.universalenchants.init;

import fuzs.puzzleslib.api.capability.v2.CapabilityController;
import fuzs.puzzleslib.api.capability.v2.data.CapabilityKey;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.capability.ArrowLootingCapabilityImpl;
import fuzs.universalenchants.core.CommonAbstractions;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Locale;

public class ModRegistry {
    public static final String ENCHANTMENT_CATEGORY_PREFIX = UniversalEnchants.MOD_NAME.toUpperCase(Locale.ROOT).replace(" ", "_") + "_";
    public static final EnchantmentCategory SHEARS_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "SHEARS", item -> item instanceof ShearsItem);
    public static final EnchantmentCategory AXE_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "AXE", item -> item instanceof AxeItem);
    public static final EnchantmentCategory HORSE_ARMOR_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "HORSE_ARMOR", item -> item instanceof HorseArmorItem);
    public static final EnchantmentCategory SHIELD_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "SHIELD", item -> item instanceof ShieldItem);
    static final CapabilityController CAPABILITIES = CapabilityController.from(UniversalEnchants.MOD_ID);
    public static final CapabilityKey<ArrowLootingCapability> ARROW_LOOTING_CAPABILITY = CAPABILITIES.registerEntityCapability("arrow_looting", ArrowLootingCapability.class, entity -> new ArrowLootingCapabilityImpl(), AbstractArrow.class);

    public static void touch() {

    }
}
