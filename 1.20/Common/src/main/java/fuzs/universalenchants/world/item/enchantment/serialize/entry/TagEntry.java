package fuzs.universalenchants.world.item.enchantment.serialize.entry;

import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Set;

public class TagEntry<T> extends DataEntry<T> {
    private final TagKey<T> tag;

    TagEntry(EnchantmentDataKey dataKey, TagKey<T> tag) {
        super(dataKey);
        this.tag = tag;
    }

    TagEntry(EnchantmentDataKey dataKey, String s) throws JsonSyntaxException {
        super(dataKey);
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        ResourceLocation id = new ResourceLocation(s);
        TagKey<T> tag = TagKey.create((ResourceKey<? extends Registry<T>>) (ResourceKey<?>) dataKey.registryKey, id);
        if (this.getRegistry().getTag(tag).isEmpty()) {
            throw new JsonSyntaxException("No tag with name %s found".formatted(id));
        }
        this.tag = tag;
    }

    @Override
    public void dissolve(Set<T> items) throws JsonSyntaxException {
        for (Holder<T> holder : this.getRegistry().getTagOrEmpty(this.tag)) {
            items.add(holder.value());
        }
    }

    @Override
    protected String serialize() {
        return "#" + this.tag.location();
    }
}
