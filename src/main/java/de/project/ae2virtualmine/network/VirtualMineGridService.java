package de.project.ae2virtualmine.network;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.blockentities.IChestOrDrive;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.definitions.AEItems;
import de.project.ae2virtualmine.cell.IVirtualMineCell;
import de.project.ae2virtualmine.cell.partition.MineCellPartition;
import de.project.ae2virtualmine.cell.partition.MineCellPartitionList;
import de.project.ae2virtualmine.config.VirtualMineConfig;
import de.project.ae2virtualmine.recipe.MineDropEntry;
import de.project.ae2virtualmine.recipe.MineDropRegistry;
import de.project.ae2virtualmine.registry.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;

public class VirtualMineGridService implements IGridServiceProvider, IVirtualMineGridService {

    private final IGrid grid;
    private int tickCounter = 0;
    private final java.util.Map<Integer, Integer> cellProgress = new java.util.HashMap<>();

    public VirtualMineGridService(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public void onLevelEndTick(Level level) {
        if (level.isClientSide()) {
            return;
        }

        tickCounter++;
        if (tickCounter % 5 != 0) {
            return;
        }

        IEnergyService energyService = grid.getEnergyService();
        boolean requireEnergy = VirtualMineConfig.REQUIRE_AE_ENERGY.get();
        if (requireEnergy && !energyService.isNetworkPowered()) {
            return;
        }

        boolean altered = false;
        RandomSource random = level.getRandom();
        Set<IChestOrDrive> visitedDrives = new HashSet<>();

        for (IGridNode node : grid.getNodes()) {
            if (!node.isActive()) {
                continue;
            }
            if (node.getOwner() instanceof IChestOrDrive drive && visitedDrives.add(drive)) {
                if (!drive.isPowered()) {
                    continue;
                }

                for (int i = 0; i < drive.getCellCount(); i++) {
                    StorageCell cell = drive.getOriginalCellInventory(i);
                    if (cell instanceof IVirtualMineCell mineCell) {
                        altered |= tickCell(mineCell, level, energyService, requireEnergy, random);
                    }
                }
            }
        }

        if (altered) {
            grid.getStorageService().invalidateCache();
        }
    }

    private boolean tickCell(IVirtualMineCell mineCell, Level level, IEnergyService energyService, boolean requireEnergy, RandomSource random) {
        IUpgradeInventory upgrades = UpgradeInventories.forItem(mineCell.getItemStack(), 5);
        int speedCards = Math.min(4, upgrades.getInstalledUpgrades(AEItems.SPEED_CARD.asItem()));
        int baseInterval = VirtualMineConfig.BASE_TICK_INTERVAL.get();

        int targetInterval = switch (speedCards) {
            case 1 -> (int) (baseInterval * 0.70);
            case 2 -> (int) (baseInterval * 0.45);
            case 3 -> (int) (baseInterval * 0.30);
            case 4 -> Math.max(10, (int) (baseInterval * 0.20));
            default -> baseInterval;
        };

        int key = System.identityHashCode(mineCell.getItemStack());
        int progress = cellProgress.getOrDefault(key, 0) + 5;
        if (progress >= targetInterval) {
            cellProgress.put(key, 0);
            return processCell(mineCell, level, energyService, requireEnergy, random, speedCards, upgrades);
        } else {
            cellProgress.put(key, progress);
            return false;
        }
    }

    private boolean processCell(IVirtualMineCell mineCell, Level level, IEnergyService energyService, boolean requireEnergy, RandomSource random, int speedCards, IUpgradeInventory upgrades) {
        // 1. If whole cell is full, stop immediately
        if (mineCell.isFull() || mineCell.getStatus() == CellState.FULL) {
            return false;
        }

        MineCellPartitionList partitionList = mineCell.getPartitions();
        if (partitionList.isEmpty()) {
            return false;
        }

        int dropCycles = mineCell.getTier().getDropCount();
        if (dropCycles <= 0) {
            return false;
        }

        double baseEnergy = VirtualMineConfig.ENERGY_PER_DROP.get();
        double energyMultiplier = Math.pow(1.5, speedCards);
        double energyPerDrop = baseEnergy * energyMultiplier;
        boolean anyInserted = false;

        boolean globalVoidSecondary = de.project.ae2virtualmine.util.VirtualCellAdapter.hasVoidSecondaryCard(upgrades);

        for (int c = 0; c < dropCycles; c++) {
            if (mineCell.isFull() || mineCell.getStatus() == CellState.FULL) {
                break;
            }

            // Weighted selection across partitions (0 to 99)
            int roll = random.nextInt(100);
            int cumulative = 0;
            MineCellPartition selectedPartition = null;

            for (MineCellPartition p : partitionList.partitions()) {
                cumulative += p.percent();
                if (roll < cumulative) {
                    selectedPartition = p;
                    break;
                }
            }

            // If roll falls into unallocated space (or no partition selected), cycle is idle
            if (selectedPartition == null) {
                continue;
            }

            // If this specific partition has reached its capacity, skip it (other partitions can still produce!)
            if (mineCell.isPartitionFull(selectedPartition)) {
                continue;
            }

            Item target = selectedPartition.target();
            if (target == null || !MineDropRegistry.isValidMiningTarget(target, level)) {
                continue;
            }

            List<MineDropEntry> dropEntries = MineDropRegistry.getDropEntries(target, level, mineCell.getTier());
            if (dropEntries.isEmpty()) {
                continue;
            }

            MineDropRegistry.RolledDrop rolled = MineDropRegistry.rollDropWithIndex(dropEntries, random);
            ItemStack dropStack = rolled.stack();
            if (dropStack.isEmpty()) {
                continue;
            }

            // Check if this drop is a secondary byproduct
            boolean isSecondary = rolled.entryIndex() > 0;

            boolean voidThisSecondary = globalVoidSecondary && selectedPartition.voidSecondary();

            if (voidThisSecondary && isSecondary) {
                // Secondary output is voided!
                if (requireEnergy && energyPerDrop > 0) {
                    energyService.extractAEPower(energyPerDrop, Actionable.MODULATE, PowerMultiplier.CONFIG);
                }
                continue;
            }

            AEItemKey key = AEItemKey.of(dropStack);

            // Test if the cell has space to accept this item
            long canInsert = mineCell.injectGeneratedDrop(key, dropStack.getCount(), Actionable.SIMULATE);
            if (canInsert <= 0) {
                continue;
            }

            // Only consume AE power if the item actually fits into the cell
            if (requireEnergy && energyPerDrop > 0) {
                int dropCount = dropStack.getCount();
                double scaledEnergy = (canInsert < dropCount) ? energyPerDrop * ((double) canInsert / dropCount) : energyPerDrop;
                double extracted = energyService.extractAEPower(scaledEnergy, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                if (extracted < scaledEnergy) {
                    break; // Network ran out of power
                }
                energyService.extractAEPower(scaledEnergy, Actionable.MODULATE, PowerMultiplier.CONFIG);
            }

            long inserted = mineCell.injectGeneratedDrop(key, canInsert, Actionable.MODULATE);
            if (inserted > 0) {
                anyInserted = true;
            }
        }

        if (anyInserted) {
            mineCell.persist();
        }

        return anyInserted;
    }
}

