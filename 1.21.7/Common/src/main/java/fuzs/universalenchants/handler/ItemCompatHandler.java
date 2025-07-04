package fuzs.universalenchants.handler;

import com.google.common.collect.ImmutableList;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.universalenchants.init.CompositeHolderSet;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

public class ItemCompatHandler {
    public static final Set<EquipmentSlotGroup> ARMOR_EQUIPMENT_SLOT_GROUPS = Set.of(EquipmentSlotGroup.FEET,
            EquipmentSlotGroup.LEGS,
            EquipmentSlotGroup.CHEST,
            EquipmentSlotGroup.HEAD,
            EquipmentSlotGroup.ARMOR);
    static final ThreadLocal<Unit> IS_BLOCKING_WITH_SHIELD = new ThreadLocal<>();

    public static void onFinalizeItemComponents(Item item, Consumer<Function<DataComponentMap, DataComponentPatch>> consumer) {
        if (item instanceof ShearsItem || item instanceof ShieldItem) {
            consumer.accept((DataComponentMap components) -> {
                return DataComponentPatch.builder().set(DataComponents.ENCHANTABLE, new Enchantable(1)).build();
            });
        } else {
            consumer.accept((DataComponentMap components) -> {
                if (!components.has(DataComponents.ENCHANTABLE)) {
                    Equippable equippable = components.get(DataComponents.EQUIPPABLE);
                    if (equippable != null && equippable.slot() == EquipmentSlot.BODY) {
                        ItemAttributeModifiers itemAttributeModifiers = components.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                                ItemAttributeModifiers.EMPTY);
                        double defenseValue = itemAttributeModifiers.modifiers()
                                .stream()
                                .filter((ItemAttributeModifiers.Entry entry) -> entry.attribute().is(Attributes.ARMOR))
                                .map(ItemAttributeModifiers.Entry::modifier)
                                .mapToDouble(AttributeModifier::amount)
                                .sum();
                        return DataComponentPatch.builder()
                                .set(DataComponents.ENCHANTABLE, new Enchantable(Math.max(1, Mth.ceil(defenseValue))))
                                .build();
                    }
                }

                return DataComponentPatch.EMPTY;
            });
        }
    }

    public static void onTagsUpdated(HolderLookup.Provider registries, boolean client) {
        // use this event to modify registered enchantments directly, relevant fields are made mutable via access widener
        HolderLookup.RegistryLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);
        HolderLookup.RegistryLookup<Enchantment> enchantmentLookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        enchantmentLookup.listElements().forEach((Holder.Reference<Enchantment> holder) -> {
            Enchantment enchantment = holder.value();
            Enchantment.EnchantmentDefinition enchantmentDefinition = enchantment.definition();
            // allow all armor enchantments to also work for the body equipment slot
            // they need to separately support such items though
            if (!enchantmentDefinition.slots().contains(EquipmentSlotGroup.BODY)) {
                for (EquipmentSlotGroup slot : enchantmentDefinition.slots()) {
                    if (ARMOR_EQUIPMENT_SLOT_GROUPS.contains(slot)) {
                        ImmutableList.Builder<EquipmentSlotGroup> builder = ImmutableList.builder();
                        builder.addAll(enchantmentDefinition.slots());
                        builder.add(EquipmentSlotGroup.BODY);
                        enchantmentDefinition.slots = builder.build();
                    }
                }
            }
            // replaces exclusive enchantment sets with a custom composite holder set that supports one additional inclusive set tag per enchantment
            setEnchantmentProperty(enchantmentLookup,
                    ModRegistry.getInclusiveSetEnchantmentTag(holder.key()),
                    enchantment.exclusiveSet(),
                    (HolderSet<Enchantment> holderSet) -> {
                        enchantment.exclusiveSet = holderSet;
                    },
                    CompositeHolderSet.Removal::new);
            // replaces the supported item holder sets with a custom composite holder set that includes one additional tag per enchantment
            setSupportedEnchantmentItems(itemLookup,
                    ModRegistry.getSecondaryEnchantableItemTag(holder.key()),
                    enchantmentDefinition.supportedItems(),
                    (HolderSet<Item> holderSet) -> {
                        enchantmentDefinition.supportedItems = holderSet;
                    });
            enchantmentDefinition.primaryItems().ifPresent((HolderSet<Item> holderSetX) -> {
                setSupportedEnchantmentItems(itemLookup,
                        ModRegistry.getPrimaryEnchantableItemTag(holder.key()),
                        holderSetX,
                        (HolderSet<Item> holderSet) -> {
                            enchantmentDefinition.primaryItems = Optional.of(holderSet);
                        });
            });
        });
    }

    private static <T> void setSupportedEnchantmentItems(HolderLookup.RegistryLookup<T> registryLookup, TagKey<T> tagKey, HolderSet<T> supportedItems, Consumer<HolderSet<T>> holderSetSetter) {
        setEnchantmentProperty(registryLookup,
                tagKey,
                supportedItems,
                holderSetSetter,
                (HolderSet<T> o1, HolderSet<T> o2) -> new CompositeHolderSet.Or<>(List.of(o1, o2)));
    }

    private static <T> void setEnchantmentProperty(HolderLookup.RegistryLookup<T> registryLookup, TagKey<T> tagKey, HolderSet<T> originalHolderSet, Consumer<HolderSet<T>> holderSetSetter, BinaryOperator<HolderSet<T>> holderSetCombiner) {
        Optional<HolderSet.Named<T>> optional = registryLookup.get(tagKey);
        HolderSet<T> newHolderSet = optional.map((HolderSet.Named<T> holderSetX) -> (HolderSet<T>) holderSetX)
                .orElse(HolderSet.empty());
        holderSetSetter.accept(holderSetCombiner.apply(originalHolderSet, newHolderSet));
    }

    public static EventResult onShieldBlock(LivingEntity blockingEntity, DamageSource damageSource, MutableFloat blockedDamage) {
        if (blockingEntity.level() instanceof ServerLevel serverLevel) {
            if (damageSource.isDirect() && damageSource.getEntity() instanceof LivingEntity attackingEntity) {
                // fix for mods hooking into post attack effects and triggering this event (namely Apotheosis)
                if (IS_BLOCKING_WITH_SHIELD.get() == null) {
                    IS_BLOCKING_WITH_SHIELD.set(Unit.INSTANCE);
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel,
                            blockingEntity,
                            damageSource,
                            blockingEntity.getUseItem());
                    float attackKnockback = (float) blockingEntity.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
                    attackKnockback = EnchantmentHelper.modifyKnockback(serverLevel,
                            blockingEntity.getUseItem(),
                            attackingEntity,
                            damageSource,
                            attackKnockback);
                    // also fixes a vanilla bug where shield do not deal knockback in LivingEntity::blockedByShield,
                    // since the knockback method is called on the blocking entity and not the attacking entity
                    // if that should not happen, so knockback only applies when the actual knockback enchantment is present
                    // include a check here if the knockback is different from the original attribute value
                    attackingEntity.knockback(0.5 * attackKnockback,
                            blockingEntity.getX() - attackingEntity.getX(),
                            blockingEntity.getZ() - attackingEntity.getZ());
                    IS_BLOCKING_WITH_SHIELD.remove();
                }
            }
        }
        return EventResult.PASS;
    }

    public static EventResult onUseItemTick(LivingEntity entity, ItemStack useItem, MutableInt useItemRemaining) {
        Item item = useItem.getItem();
        int itemUseDuration = useItem.getUseDuration(entity) - useItemRemaining.getAsInt();
        if (item instanceof BowItem && itemUseDuration < 20 || item instanceof TridentItem && itemUseDuration < 10) {
            // quick charge enchantment for bows and tridents
            // values same as crossbow, but speed improvements is not relative to actual item use duration now
            float chargingTime = EnchantmentHelper.modifyCrossbowChargingTime(useItem, entity, 1.25F);
            useItemRemaining.mapInt(duration -> duration - Mth.floor((1.25F - chargingTime) / 0.25F));
        }
        return EventResult.PASS;
    }
}
