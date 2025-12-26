package fuzs.universalenchants.init;

import fuzs.puzzleslib.api.data.v2.AbstractDatapackRegistriesProvider;
import fuzs.puzzleslib.api.init.v3.tags.TagFactory;
import fuzs.universalenchants.UniversalEnchants;
import net.minecraft.advancements.criterion.*;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.*;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.item.enchantment.effects.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;

import java.util.List;
import java.util.Optional;

public class ModRegistry {
    public static final RegistrySetBuilder REGISTRY_SET_BUILDER = new RegistrySetBuilder().add(Registries.ENCHANTMENT,
            ModRegistry::boostrapEnchantments);
    static final TagFactory TAGS = TagFactory.make(UniversalEnchants.MOD_ID);
    public static final TagKey<Block> FROSTED_ICE_REPLACEABLES_BLOCK_TAG = TAGS.registerBlockTag(
            "frosted_ice_replaceables");

    public static void bootstrap() {
        // NO-OP
    }

    public static TagKey<Item> getPrimaryEnchantableItemTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ITEM, resourceKey.identifier().withPrefix("primary_enchantable/"));
    }

    public static TagKey<Item> getSecondaryEnchantableItemTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ITEM, resourceKey.identifier().withPrefix("secondary_enchantable/"));
    }

    public static TagKey<Enchantment> getExclusiveSetEnchantmentTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ENCHANTMENT, resourceKey.identifier().withPrefix("exclusive_set/"));
    }

    public static TagKey<Enchantment> getInclusiveSetEnchantmentTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ENCHANTMENT, resourceKey.identifier().withPrefix("inclusive_set/"));
    }

    public static void boostrapEnchantments(BootstrapContext<Enchantment> context) {
        HolderGetter<Block> blockLookup = context.lookup(Registries.BLOCK);
        HolderGetter<Item> itemLookup = context.lookup(Registries.ITEM);
        HolderGetter<EntityType<?>> entityLookup = context.lookup(Registries.ENTITY_TYPE);
        HolderGetter<Enchantment> enchantmentLookup = context.lookup(Registries.ENCHANTMENT);
        // Allow frost walker to replace sea vegetation and itself, also remove on ground check to enable jump-sprinting across water.
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
        // Remove the arrow entity type check, so this also works for tridents.
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
        // Allow entities attacking with a mace. Must be a smash attack; that is copied from the Wind Burst enchantment.
        AbstractDatapackRegistriesProvider.registerEnchantment(context,
                Enchantments.CHANNELING,
                Enchantment.enchantment(Enchantment.definition(itemLookup.getOrThrow(ItemTags.TRIDENT_ENCHANTABLE),
                                1,
                                1,
                                Enchantment.constantCost(25),
                                Enchantment.constantCost(50),
                                8,
                                EquipmentSlotGroup.MAINHAND))
                        .withEffect(EnchantmentEffectComponents.POST_ATTACK,
                                EnchantmentTarget.ATTACKER,
                                EnchantmentTarget.VICTIM,
                                AllOf.entityEffects(new SummonEntityEffect(HolderSet.direct(EntityType.LIGHTNING_BOLT.builtInRegistryHolder()),
                                                false),
                                        new PlaySoundEffect(List.of(SoundEvents.TRIDENT_THUNDER),
                                                ConstantFloat.of(5.0F),
                                                ConstantFloat.of(1.0F))),
                                AllOfCondition.allOf(WeatherCheck.weather().setThundering(true),
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                                EntityPredicate.Builder.entity()
                                                        .located(LocationPredicate.Builder.location()
                                                                .setCanSeeSky(true))),
                                        AnyOfCondition.anyOf(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER,
                                                        EntityPredicate.Builder.entity().of(entityLookup, EntityType.TRIDENT)),
                                                LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.DIRECT_ATTACKER,
                                                        EntityPredicate.Builder.entity()
                                                                .equipment(EntityEquipmentPredicate.Builder.equipment()
                                                                        .mainhand(ItemPredicate.Builder.item()
                                                                                .of(itemLookup, Items.MACE)))
                                                                .flags(EntityFlagsPredicate.Builder.flags()
                                                                        .setIsFlying(false))
                                                                .moving(MovementPredicate.fallDistance(MinMaxBounds.Doubles.atLeast(
                                                                        1.5)))))))
                        .withEffect(EnchantmentEffectComponents.HIT_BLOCK,
                                AllOf.entityEffects(new SummonEntityEffect(HolderSet.direct(EntityType.LIGHTNING_BOLT.builtInRegistryHolder()),
                                                false),
                                        new PlaySoundEffect(List.of(SoundEvents.TRIDENT_THUNDER),
                                                ConstantFloat.of(5.0F),
                                                ConstantFloat.of(1.0F))),
                                AllOfCondition.allOf(WeatherCheck.weather().setThundering(true),
                                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
                                                EntityPredicate.Builder.entity().of(entityLookup, EntityType.TRIDENT)),
                                        LocationCheck.checkLocation(LocationPredicate.Builder.location()
                                                .setCanSeeSky(true)
                                                .setBlock(net.minecraft.advancements.criterion.BlockPredicate.Builder.block()
                                                        .of(blockLookup, BlockTags.LIGHTNING_RODS))))));
    }
}
