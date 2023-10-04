package fuzs.universalenchants.world.item.enchantment.serialize;

import com.google.common.collect.Maps;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentDataManager;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentDataTags;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.EnchantmentData2;
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
        this.vanillaCategory = EnchantmentDataManager.INSTANCE.getVanillaCategory(enchantment);
        this.category = EnchantmentDataManager.INSTANCE.getCustomCategory(enchantment, this::canEnchant);
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
        EnchantmentDataManager.INSTANCE.setEnchantmentCategory(this.enchantment, this.vanillaCategory);
    }

    public void submit(EnchantmentData2<?> entry) {
        this.entries.computeIfAbsent(entry.getDataType(), $ -> new EntryCollection<>()).submit(entry);
    }

    public void applyEnchantmentCategory() {
        if (this.entries.containsKey(EnchantmentDataKey.ITEMS)) {
            EnchantmentDataManager.INSTANCE.setEnchantmentCategory(this.enchantment, this.category);
        }
    }

    private boolean canEnchant(Item item) {
        ItemStack itemStack = new ItemStack(item);
        if (itemStack.is(EnchantmentDataTags.getTagKeyForDisabledItems(this.id))) return false;
        return itemStack.is(EnchantmentDataTags.getTagKeyForItems(this.id));
    }

    public boolean canApplyAtAnvil(ItemStack itemStack) {
        if (itemStack.is(EnchantmentDataTags.getTagKeyForDisabledAnvilItems(this.id))) return false;
        return itemStack.is(EnchantmentDataTags.getTagKeyForAnvilItems(this.id));
    }

    public boolean isCompatibleWith(Enchantment other, boolean fallback) {
        return this.testForType(EnchantmentDataKey.INCOMPATIBLE, other).map(t -> !t && this.enchantment != other).orElse(fallback);
    }

    private Optional<Boolean> testForType(EnchantmentDataKey dataType, Object o) {
        return this.entries.containsKey(dataType) ? Optional.of(this.entries.get(dataType).getItems().contains(o)) : Optional.empty();
    }
}
