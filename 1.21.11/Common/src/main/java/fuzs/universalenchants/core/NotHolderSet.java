package fuzs.universalenchants.core;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * See Also: {@code net.neoforged.neoforge.registries.holdersets.NotHolderSet}
 */
public final class NotHolderSet<T> extends HolderSet.ListBacked<T> {
    private final HolderLookup<T> holderLookup;
    private final HolderSet<T> holderSet;

    public NotHolderSet(HolderLookup<T> holderLookup, HolderSet<T> holderSet) {
        this.holderLookup = holderLookup;
        this.holderSet = holderSet;
    }

    @Override
    protected List<Holder<T>> contents() {
        return this.holderLookup.listElements()
                .filter(Predicate.not(this.holderSet::contains))
                .<Holder<T>>map((Holder.Reference<T> holder) -> {
                    return holder;
                })
                .toList();
    }

    @Override
    public boolean isBound() {
        return this.holderSet.isBound();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(this.contents());
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return !this.holderSet.contains(holder);
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return this.holderSet.canSerializeIn(owner);
    }

    @Override
    public String toString() {
        return "NotSet[" + this.contents() + "]";
    }
}
