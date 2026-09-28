package de.project.ae2virtualmine.registry;

import de.project.ae2virtualmine.AE2VirtualMine;
import de.project.ae2virtualmine.block.VirtualPartitionerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AE2VirtualMine.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VirtualPartitionerBlockEntity>> VIRTUAL_PARTITIONER =
            BLOCK_ENTITIES.register("virtual_partitioner", () ->
                    new BlockEntityType<>(VirtualPartitionerBlockEntity::new, ModBlocks.VIRTUAL_PARTITIONER.get()));
}
