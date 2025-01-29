package fuzs.universalenchants.data;

import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ModBlockTagProvider extends AbstractTagProvider<Block> {

    public ModBlockTagProvider(DataProviderContext context) {
        super(Registries.BLOCK, context);
    }

    @Override
    public void addTags(HolderLookup.Provider registries) {
        this.add(ModRegistry.FROSTED_ICE_REPLACEABLES_BLOCK_TAG)
                .add(Blocks.WATER,
                        Blocks.BUBBLE_COLUMN,
                        Blocks.KELP,
                        Blocks.KELP_PLANT,
                        Blocks.SEAGRASS,
                        Blocks.TALL_SEAGRASS);
    }
}
