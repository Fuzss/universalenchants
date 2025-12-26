package fuzs.universalenchants.core;

import com.google.common.collect.Sets;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;

import java.util.List;
import java.util.stream.Collectors;

/**
 * See Also: {@code net.neoforged.neoforge.registries.holdersets.AndHolderSet}
 */
public final class AndHolderSet<T> extends CompositeHolderSet<T> {

    public AndHolderSet(List<HolderSet<T>> holderSets) {
        super(holderSets);
    }

    @Override
    protected List<Holder<T>> contents() {
        return this.holderSets.stream()
                .map((HolderSet<T> holderSet) -> holderSet.stream().collect(Collectors.toSet()))
                .reduce(Sets::intersection)
                .map(List::copyOf)
                .orElseGet(List::of);
    }

    @Override
    public boolean contains(Holder<T> holder) {
        for (HolderSet<T> holderSet : this.holderSets) {
            if (!holderSet.contains(holder)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "AndSet[" + this.contents() + "]";
    }
}
