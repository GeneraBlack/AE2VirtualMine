package de.project.ae2virtualmine.registry;

import de.project.ae2virtualmine.AE2VirtualMine;
import de.project.ae2virtualmine.block.VirtualPartitionerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AE2VirtualMine.MODID);

    public static final DeferredBlock<VirtualPartitionerBlock> VIRTUAL_PARTITIONER =
            BLOCKS.registerBlock("virtual_partitioner", VirtualPartitionerBlock::new,
                    () -> BlockBehaviour.Properties.of()
                            .strength(2.5f, 6.0f)
                            .sound(SoundType.METAL)
                            .requiresCorrectToolForDrops());
}
