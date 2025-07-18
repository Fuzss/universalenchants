package fuzs.universalenchants.init;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A holder set implementation that combines multiple holder sets.
 * <p>
 * Similar to {@code OrHolderSet} on NeoForge.
 */
public abstract class CompositeHolderSet<T> extends HolderSet.ListBacked<T> {
    protected final List<HolderSet<T>> components;

    public CompositeHolderSet(List<HolderSet<T>> components) {
        this.components = components;
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
        for (HolderSet<T> holderSet : this.components) {
            if (!holderSet.canSerializeIn(owner)) {
                return false;
            }
        }
        return super.canSerializeIn(owner);
    }

    @Override
    public boolean isBound() {
        for (HolderSet<T> holderSet : this.components) {
            if (!holderSet.isBound()) {
                return false;
            }
        }
        return true;
    }

    public static class Or<T> extends CompositeHolderSet<T> {

        public Or(List<HolderSet<T>> components) {
            super(components);
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.components.stream().flatMap(HolderSet::stream).distinct().toList();
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
        public String toString() {
            return "OrSet[" + this.contents() + "]";
        }
    }

    public static class And<T> extends CompositeHolderSet<T> {

        public And(List<HolderSet<T>> components) {
            super(components);
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.components.stream()
                    .map((HolderSet<T> holderSet) -> holderSet.stream().collect(Collectors.toSet()))
                    .reduce(Sets::intersection)
                    .map(List::copyOf)
                    .orElseGet(List::of);
        }

        @Override
        public boolean contains(Holder<T> holder) {
            for (HolderSet<T> holderSet : this.components) {
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

    public static class Removal<T> extends CompositeHolderSet<T> {
        private final HolderSet<T> holderSet;
        private final HolderSet<T> removals;

        public Removal(HolderSet<T> holderSet, HolderSet<T> removals) {
            super(List.of(holderSet, removals));
            this.holderSet = holderSet;
            this.removals = removals;
        }

        @Override
        protected List<Holder<T>> contents() {
            return this.holderSet.stream().filter(Predicate.not(this.removals::contains)).toList();
        }

        @Override
        public boolean contains(Holder<T> holder) {
            return this.holderSet.contains(holder) && !this.removals.contains(holder);
        }

        @Override
        public String toString() {
            return "RemovalSet[" + this.contents() + "]";
        }
    }
}
