package fuzs.universalenchants.core;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Optional;

/**
 * A holder set implementation that combines multiple holder sets.
 * <p>
 * Similar to {@code OrHolderSet} on NeoForge.
 */
public final class CompositeHolderSet<T> extends HolderSet.ListBacked<T> {
    private final List<HolderSet<T>> components;

    public CompositeHolderSet(List<HolderSet<T>> components) {
        this.components = components;
    }

    @Override
    protected List<Holder<T>> contents() {
        return this.components.stream().flatMap(HolderSet::stream).toList();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(this.contents());
    }

    @Override
    public boolean contains(Holder<T> holder) {
        for (HolderSet<T> holderSet : this.components) {
            if (holderSet.contains(holder)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        for (HolderSet<T> holderSet : this.components) {
            if (!holderSet.canSerializeIn(owner)) {
                return false;
            }
        }
        return super.canSerializeIn(owner);
    }

    @Override
    public String toString() {
        return "CompositeSet[" + this.components + "]";
    }
}
