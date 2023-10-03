package fuzs.universalenchants.world.item.enchantment.serialize;

import com.google.common.collect.Maps;
import fuzs.universalenchants.world.item.enchantment.data.BuiltInEnchantmentDataManager;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.DataEntry;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.EnchantmentDataKey;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.EntryCollection;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Map;
import java.util.Optional;

public class EnchantmentHolder {
    private final Enchantment enchantment;
    private final ResourceLocation id;
    private final EnchantmentCategory vanillaCategory;
    private final EnchantmentCategory category;
    private final Map<EnchantmentDataKey, EntryCollection<?>> entries = Maps.newEnumMap(EnchantmentDataKey.class);

    public EnchantmentHolder(Enchantment enchantment) {
        this.enchantment = enchantment;
        this.vanillaCategory = BuiltInEnchantmentDataManager.INSTANCE.getVanillaCategory(enchantment);
        this.category = BuiltInEnchantmentDataManager.INSTANCE.getCustomCategory(enchantment, this::canEnchant);
        this.id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
    }

    public ResourceLocation id() {
        return this.id;
    }

    public void requireEmpty() {
        if (!this.entries.isEmpty()) {
            throw new IllegalStateException("Holder for enchantment %s has not been cleared".formatted(this.id));
        }
    }

    public void clear() {
        this.entries.clear();
        // reset to vanilla enchantment category for now just in case something goes wrong during rebuilding
        BuiltInEnchantmentDataManager.INSTANCE.setEnchantmentCategory(this.enchantment, this.vanillaCategory);
    }

    public void submit(DataEntry<?> entry) {
        this.entries.computeIfAbsent(entry.getDataType(), $ -> new EntryCollection<>()).submit(entry);
    }

    public void applyEnchantmentCategory() {
        if (this.entries.containsKey(EnchantmentDataKey.ITEMS)) {
            BuiltInEnchantmentDataManager.INSTANCE.setEnchantmentCategory(this.enchantment, this.category);
        }
    }

    private boolean canEnchant(Item item) {
        return this.testForType(EnchantmentDataKey.ITEMS, item).orElseGet(() -> this.vanillaCategory.canEnchant(item));
    }

    public boolean canApplyAtAnvil(ItemStack itemStack) {
        return this.testForType(EnchantmentDataKey.ANVIL_ITEMS, itemStack.getItem()).orElseGet(() -> this.enchantment.canEnchant(itemStack));
    }

    public boolean isCompatibleWith(Enchantment other, boolean fallback) {
        return this.testForType(EnchantmentDataKey.INCOMPATIBLE, other).map(t -> !t && this.enchantment != other).orElse(fallback);
    }

    private Optional<Boolean> testForType(EnchantmentDataKey dataType, Object o) {
        return this.entries.containsKey(dataType) ? Optional.of(this.entries.get(dataType).getItems().contains(o)) : Optional.empty();
    }
}
