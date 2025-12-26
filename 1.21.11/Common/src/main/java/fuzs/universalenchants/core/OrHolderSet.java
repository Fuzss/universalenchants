package fuzs.universalenchants.core;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;

import java.util.List;

/**
 * See Also: {@code net.neoforged.neoforge.registries.holdersets.OrHolderSet}
 */
public final class OrHolderSet<T> extends CompositeHolderSet<T> {

    public OrHolderSet(List<HolderSet<T>> holderSets) {
        super(holderSets);
    }

    @Override
    protected List<Holder<T>> contents() {
        return this.holderSets.stream().flatMap(HolderSet::stream).distinct().toList();
    }

    @Override
    public boolean contains(Holder<T> holder) {
        for (HolderSet<T> holderSet : this.holderSets) {
            if (holderSet.contains(holder)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "OrSet[" + this.contents() + "]";
    }
}
