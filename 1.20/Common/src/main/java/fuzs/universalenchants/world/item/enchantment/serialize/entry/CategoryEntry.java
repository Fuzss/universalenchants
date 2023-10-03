package fuzs.universalenchants.world.item.enchantment.serialize.entry;

import com.google.gson.JsonSyntaxException;
import fuzs.universalenchants.world.item.enchantment.data.BuiltInEnchantmentDataManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Objects;
import java.util.Set;

@Deprecated(forRemoval = true)
public class CategoryEntry extends DataEntry<Item> {
    private final EnchantmentCategory category;

    CategoryEntry(EnchantmentDataKey dataKey, EnchantmentCategory category) {
        super(dataKey);
        this.category = category;
    }

    CategoryEntry(EnchantmentDataKey dataKey, String s) throws JsonSyntaxException {
        super(dataKey);
        if (s.startsWith("$")) {
            s = s.substring(1);
        }
        ResourceLocation id = new ResourceLocation(s);
        EnchantmentCategory category = BuiltInEnchantmentDataManager.INSTANCE.getToCategoryMap().get(id);
        if (category == null) {
            throw new JsonSyntaxException("No category with name %s found".formatted(id));
        }
        this.category = category;
    }

    @Override
    public void dissolve(Set<Item> items) throws JsonSyntaxException {
        items.addAll(BuiltInEnchantmentDataManager.INSTANCE.getItems(this.category));
    }

    @Override
    protected String serialize() {
        Objects.requireNonNull(this.category, "category is null");
        ResourceLocation identifier = BuiltInEnchantmentDataManager.getResourceLocationFromCategory(this.category);
        Objects.requireNonNull(identifier, "identifier for category %s is null".formatted(this.category));
        return "$" + identifier;
    }
}
