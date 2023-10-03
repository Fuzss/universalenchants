package fuzs.universalenchants.world.item.enchantment.serialize.entry;

import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class ItemEntry<T> extends DataEntry<T> {
    private final T item;

    ItemEntry(EnchantmentDataKey dataKey, T item) {
        super(dataKey);
        this.item = item;
    }

    ItemEntry(EnchantmentDataKey dataKey, String s) throws JsonSyntaxException {
        super(dataKey);
        ResourceLocation id = new ResourceLocation(s);
        if (!this.getRegistry().containsKey(id))
            throw new JsonSyntaxException("No item with name %s found".formatted(id));
        this.item = this.getRegistry().get(id);
    }

    @Override
    public void dissolve(Set<T> items) throws JsonSyntaxException {
        items.add(this.item);
    }

    @Override
    protected String serialize() {
        return this.getRegistry().getKey(this.item).toString();
    }
}
