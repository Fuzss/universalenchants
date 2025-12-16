package fuzs.universalenchants.init;

import fuzs.puzzleslib.api.data.v2.AbstractDatapackRegistriesProvider;
import fuzs.puzzleslib.api.init.v3.tags.TagFactory;
import fuzs.universalenchants.UniversalEnchants;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.*;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.item.enchantment.effects.DamageImmunity;
import net.minecraft.world.item.enchantment.effects.ReplaceDisk;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

import java.util.Optional;

public class ModRegistry {
    public static final RegistrySetBuilder REGISTRY_SET_BUILDER = new RegistrySetBuilder().add(Registries.ENCHANTMENT,
            ModRegistry::boostrapEnchantments);
    static final TagFactory TAGS = TagFactory.make(UniversalEnchants.MOD_ID);
    public static final TagKey<Block> FROSTED_ICE_REPLACEABLES_BLOCK_TAG = TAGS.registerBlockTag(
            "frosted_ice_replaceables");
    public static final TagKey<Item> ANIMAL_ARMOR_ITEM_TAG = TAGS.registerItemTag("animal_armor");

    public static void bootstrap() {
        // NO-OP
    }

    public static TagKey<Item> getPrimaryEnchantableItemTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ITEM, resourceKey.location().withPrefix("primary_enchantable/"));
    }

    public static TagKey<Item> getSecondaryEnchantableItemTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ITEM, resourceKey.location().withPrefix("secondary_enchantable/"));
    }

    public static TagKey<Enchantment> getInclusiveSetEnchantmentTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ENCHANTMENT, resourceKey.location().withPrefix("inclusive_set/"));
    }

    public static void boostrapEnchantments(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> itemLookup = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantmentLookup = context.lookup(Registries.ENCHANTMENT);
        // allow frost walker to replace sea vegetation and itself, also remove on ground check to enable jump-sprinting across water
        ReplaceDisk replaceDisk = new ReplaceDisk(new LevelBasedValue.Clamped(LevelBasedValue.perLevel(3.0F, 1.0F),
                0.0F,
                16.0F),
                LevelBasedValue.constant(1.0F),
                new Vec3i(0, -1, 0),
                Optional.of(BlockPredicate.anyOf(BlockPredicate.allOf(BlockPredicate.matchesTag(new Vec3i(0, 1, 0),
                                BlockTags.AIR),
                        BlockPredicate.matchesTag(FROSTED_ICE_REPLACEABLES_BLOCK_TAG),
                        BlockPredicate.matchesFluids(Fluids.WATER),
                        BlockPredicate.unobstructed()), BlockPredicate.matchesBlocks(Blocks.FROSTED_ICE))),
                BlockStateProvider.simple(Blocks.FROSTED_ICE),
                Optional.of(GameEvent.BLOCK_PLACE));
        AbstractDatapackRegistriesProvider.registerEnchantment(context,
                Enchantments.FROST_WALKER,
                Enchantment.enchantment(Enchantment.definition(itemLookup.getOrThrow(ItemTags.FOOT_ARMOR_ENCHANTABLE),
                                2,
                                2,
                                Enchantment.dynamicCost(10, 10),
                                Enchantment.dynamicCost(25, 10),
                                4,
                                EquipmentSlotGroup.FEET))
                        .exclusiveWith(enchantmentLookup.getOrThrow(EnchantmentTags.BOOTS_EXCLUSIVE))
                        .withEffect(EnchantmentEffectComponents.DAMAGE_IMMUNITY,
                                DamageImmunity.INSTANCE,
                                DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType()
                                        .tag(TagPredicate.is(DamageTypeTags.BURN_FROM_STEPPING))
                                        .tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY))))
                        .withEffect(EnchantmentEffectComponents.LOCATION_CHANGED, replaceDisk)
                        .withEffect(EnchantmentEffectComponents.TICK, replaceDisk,
                                // has a chance of about 90% to tick at least once every second, which should be enough
                                LootItemRandomChanceCondition.randomChance(0.1F)));
        // remove arrow entity type check, so this also works for tridents
        AbstractDatapackRegistriesProvider.registerEnchantment(context,
                Enchantments.POWER,
                Enchantment.enchantment(Enchantment.definition(itemLookup.getOrThrow(ItemTags.BOW_ENCHANTABLE),
                                10,
                                5,
                                Enchantment.dynamicCost(1, 10),
                                Enchantment.dynamicCost(16, 10),
                                1,
                                EquipmentSlotGroup.MAINHAND))
                        .withEffect(EnchantmentEffectComponents.DAMAGE, new AddValue(LevelBasedValue.perLevel(0.5F))));
    }
}
