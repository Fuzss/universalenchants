package fuzs.universalenchants.core;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Optional;

/**
 * See Also: {@code net.neoforged.neoforge.registries.holdersets.CompositeHolderSet}
 */
public abstract class CompositeHolderSet<T> extends HolderSet.ListBacked<T> {
    protected final List<HolderSet<T>> holderSets;

    public CompositeHolderSet(List<HolderSet<T>> holderSets) {
        this.holderSets = holderSets;
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(this.contents());
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        for (HolderSet<T> holderSet : this.holderSets) {
            if (!holderSet.canSerializeIn(owner)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isBound() {
        for (HolderSet<T> holderSet : this.holderSets) {
            if (!holderSet.isBound()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public abstract String toString();
}
