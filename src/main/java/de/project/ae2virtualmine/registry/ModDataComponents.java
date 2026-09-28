package de.project.ae2virtualmine.registry;

import de.project.ae2virtualmine.AE2VirtualMine;
import de.project.ae2virtualmine.cell.partition.MineCellPartitionList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AE2VirtualMine.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MineCellPartitionList>> PARTITIONS =
            DATA_COMPONENTS.register("partitions", () -> DataComponentType.<MineCellPartitionList>builder()
                    .persistent(MineCellPartitionList.CODEC)
                    .networkSynchronized(MineCellPartitionList.STREAM_CODEC)
                    .build());
}
