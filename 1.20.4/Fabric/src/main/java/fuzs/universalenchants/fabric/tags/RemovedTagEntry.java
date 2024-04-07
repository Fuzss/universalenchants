package fuzs.universalenchants.fabric.tags;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A marker extension for {@link TagEntry} to support Forge's additional <code>remove</code> field on Fabric.
 * <p>
 * The super class is made extensible via access widener.
 * <p>
 * We do not need to account for the tag data provider used during data generation, since that only runs on Forge, where
 * the remove field is properly supported already.
 */
public final class RemovedTagEntry extends TagEntry {
    public static final Codec<TagFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(TagEntry.CODEC.listOf()
                    .fieldOf("values")
                    .forGetter(tagFile -> tagFile.entries()
                            .stream()
                            .filter(Predicate.not(RemovedTagEntry.class::isInstance))
                            .toList()),
            Codec.BOOL.optionalFieldOf("replace", Boolean.FALSE).forGetter(TagFile::replace),
            TagEntry.CODEC.listOf()
                    .optionalFieldOf("remove", List.of())
                    .forGetter(tagFile -> tagFile.entries().stream().filter(RemovedTagEntry.class::isInstance).toList())
    ).apply(instance, (List<TagEntry> values, Boolean replace, List<TagEntry> remove) -> {
        // order is important here, so that removals take place after additions
        values = Stream.concat(values.stream(), remove.stream().map(RemovedTagEntry::new)).toList();
        return new TagFile(values, replace);
    }));

    private RemovedTagEntry(TagEntry tagEntry) {
        // it's ok to just pass these on, there is no other state being stored here
        super(tagEntry.id, tagEntry.tag, tagEntry.required);
    }
}
