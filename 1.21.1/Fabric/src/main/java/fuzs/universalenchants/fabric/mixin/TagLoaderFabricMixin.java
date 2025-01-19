package fuzs.universalenchants.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import fuzs.universalenchants.fabric.tags.RemovedTagEntry;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(value = TagLoader.class, priority = 800)
abstract class TagLoaderFabricMixin<T> {

    @ModifyReceiver(
            method = "load", at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/Dynamic;)Lcom/mojang/serialization/DataResult;"
    )
    )
    public Codec<TagFile> load(Codec<TagFile> codec, Dynamic<?> input) {
        // switch the vanilla tag entry codec with our own that supports reading the 'remove' field
        return RemovedTagEntry.CODEC;
    }

    @Inject(
            method = "build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void build(TagEntry.Lookup<T> lookup, List<TagLoader.EntryWithSource> entries, CallbackInfoReturnable<Either<Collection<TagLoader.EntryWithSource>, Collection<T>>> callback) {
        // vanilla uses an immutable set builder, but that does not allow for removals
        // just don't forget to make it immutable again at the end of the method
        Set<T> builder = new LinkedHashSet<>();
        List<TagLoader.EntryWithSource> list = new ArrayList<>();

        for (TagLoader.EntryWithSource entry : entries) {
            TagEntry tagEntry = entry.entry();
            // this is changed from vanilla, implementation simply copied from Forge
            if (!tagEntry.build(lookup, tagEntry instanceof RemovedTagEntry ? builder::remove : builder::add) &&
                    !(tagEntry instanceof RemovedTagEntry)) {
                list.add(entry);
            }
        }

        callback.setReturnValue(list.isEmpty() ? Either.right(List.copyOf(builder)) : Either.left(list));
    }
}
