package fuzs.universalenchants.world.item.enchantment.serialize.entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class EntryCollection<T> {
    private final List<DataEntry<T>> entries = Lists.newArrayList();
    @Nullable
    private Set<T> items;

    public void submit(DataEntry<?> dataEntry) {
        Objects.requireNonNull(dataEntry, "data entry is null");
        this.entries.add((DataEntry<T>) dataEntry);
    }

    public Set<T> getItems() {
        return this.items == null ? this.items = dissolve(this.entries) : this.items;
    }

    private static <T> Set<T> dissolve(List<DataEntry<T>> entries) {
        Set<T> include = Sets.newIdentityHashSet();
        Set<T> exclude = Sets.newIdentityHashSet();
        for (DataEntry<T> entry : entries) {
            entry.dissolve(entry.exclude ? exclude : include);
        }
        include.removeAll(exclude);
        return Collections.unmodifiableSet(include);
    }
}
