package fuzs.universalenchants.util;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.handler.CompatibilityElement;
import fuzs.universalenchants.mixin.accessor.EnchantmentAccessor;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.item.*;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CompatibilityManager {

    private final CompatibilityElement parent;
    private final Map<Enchantment, ConfigEnchantmentType> types = Maps.newHashMap();

    public CompatibilityManager(CompatibilityElement parent) {
        
        this.parent = parent;
    }

    public void load() {

        // requires game restart when disabled on config reload
        if (this.parent.isEnabled()) {

            Map<Set<Enchantment>, TypePredicate> configMap = this.createConfigMap();
            this.addPredicates(configMap);
            this.setEnchantmentTypes();
        }
    }

    private Map<Set<Enchantment>, TypePredicate> createConfigMap() {

        final Map<Set<Enchantment>, TypePredicate> predicates = Maps.newHashMap();
        predicates.put(this.parent.swordEnchantments, TypePredicate.SWORD);
        predicates.put(this.parent.axeEnchantments, TypePredicate.AXE);
        predicates.put(this.parent.tridentEnchantments, TypePredicate.TRIDENT);
        predicates.put(this.parent.bowEnchantments, TypePredicate.BOW);
        predicates.put(this.parent.crossbowEnchantments, TypePredicate.CROSSBOW);

        return predicates;
    }

    private void addPredicates(Map<Set<Enchantment>, TypePredicate> configMap) {

        configMap.forEach((key, value) -> key.forEach(enchantment -> this.get(enchantment).addPredicate(value)));
    }

    private ConfigEnchantmentType get(Enchantment enchantment) {

        return this.types.computeIfAbsent(enchantment, ConfigEnchantmentType::new);
    }

    private void setEnchantmentTypes() {

        this.types.forEach((key, value) -> value.setEnchantmentType(key));
    }

    private static class ConfigEnchantmentType {

        private final EnchantmentType defaultType;
        private final Set<TypePredicate> predicates = Sets.newHashSet();
        private final Set<TypePredicate> previousPredicates = Sets.newHashSet();

        private static int typeCounter;

        private ConfigEnchantmentType(Enchantment parent) {

            this.defaultType = parent.type;
        }

        public void addPredicate(TypePredicate typePredicate) {

            this.predicates.add(typePredicate);
        }

        public void setEnchantmentType(Enchantment enchantment) {

            // a ConcurrentModificationException was reported here
            // probably caused by client and server thread both trying to reload this listener
            synchronized (this.predicates) {

                if (this.needsUpdate()) {

                    ((EnchantmentAccessor) enchantment).setCategory(this.createType());
                }

                this.finish();
            }
        }

        private boolean needsUpdate() {

            return !this.predicates.equals(this.previousPredicates);
        }

        private EnchantmentType createType() {

            Predicate<Item> typeDelegate = this.getPredicateStream().reduce(item -> false, Predicate::or);
            return EnchantmentType.create(UniversalEnchants.MOD_ID.toUpperCase(Locale.ROOT) + "_TYPE_" + typeCounter++, typeDelegate);
        }

        private Stream<Predicate<Item>> getPredicateStream() {

            return Stream.concat(Stream.of(this.defaultType::canEnchantItem), this.predicates.stream().map(TypePredicate::getDelegate));
        }

        private void finish() {

            this.previousPredicates.clear();
            this.previousPredicates.addAll(this.predicates);
            this.predicates.clear();
        }

    }

    private enum TypePredicate {

        SWORD(item -> item instanceof SwordItem && !((CompatibilityElement) UniversalEnchants.ENCHANTMENT_COMPATIBILITY).swordBlacklist.contains(item)),
        AXE(item -> item instanceof AxeItem && !((CompatibilityElement) UniversalEnchants.ENCHANTMENT_COMPATIBILITY).axeBlacklist.contains(item)),
        TRIDENT(item -> item instanceof TridentItem && !((CompatibilityElement) UniversalEnchants.ENCHANTMENT_COMPATIBILITY).tridentBlacklist.contains(item)),
        BOW(item -> item instanceof BowItem && !((CompatibilityElement) UniversalEnchants.ENCHANTMENT_COMPATIBILITY).bowBlacklist.contains(item)),
        CROSSBOW(item -> item instanceof CrossbowItem && !((CompatibilityElement) UniversalEnchants.ENCHANTMENT_COMPATIBILITY).crossbowBlacklist.contains(item));

        private final Predicate<Item> delegate;

        TypePredicate(Predicate<Item> delegate) {

            this.delegate = delegate;
        }

        public Predicate<Item> getDelegate() {

            return this.delegate;
        }

    }

}
