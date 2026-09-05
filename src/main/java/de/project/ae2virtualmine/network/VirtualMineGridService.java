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
import de.project.ae2virtualmine.cell.IVirtualMineCell;
import de.project.ae2virtualmine.config.VirtualMineConfig;
import de.project.ae2virtualmine.recipe.MineDropEntry;
import de.project.ae2virtualmine.recipe.MineDropRegistry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VirtualMineGridService implements IGridServiceProvider, IVirtualMineGridService {

    private final IGrid grid;
    private int tickCounter = 0;

    public VirtualMineGridService(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public void onLevelEndTick(Level level) {
        if (level.isClientSide()) {
            return;
        }

        tickCounter++;
        int interval = VirtualMineConfig.BASE_TICK_INTERVAL.get();
        if (tickCounter < interval) {
            return;
        }
        tickCounter = 0;

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
                        altered |= processCell(mineCell, level, energyService, requireEnergy, random);
                    }
                }
            }
        }

        if (altered) {
            grid.getStorageService().invalidateCache();
        }
    }

    private boolean processCell(IVirtualMineCell mineCell, Level level, IEnergyService energyService, boolean requireEnergy, RandomSource random) {
        // 1. If cell is full, stop immediately and do not generate or consume power
        if (mineCell.isFull() || mineCell.getStatus() == CellState.FULL) {
            return false;
        }

        Item target = mineCell.getConfiguredTarget();
        if (target == null) {
            return false;
        }

        List<MineDropEntry> dropEntries = MineDropRegistry.getDropEntries(target, level);
        if (dropEntries.isEmpty()) {
            return false;
        }

        int dropCycles = mineCell.getTier().getDropCount();
        if (dropCycles <= 0) {
            return false;
        }

        double energyPerDrop = VirtualMineConfig.ENERGY_PER_DROP.get();
        boolean anyInserted = false;

        for (int c = 0; c < dropCycles; c++) {
            // Check if cell has become full during the cycle
            if (mineCell.isFull() || mineCell.getStatus() == CellState.FULL) {
                break; // Stop generating, cell is full!
            }

            ItemStack dropStack = MineDropRegistry.rollDrop(dropEntries, random);
            if (dropStack.isEmpty()) {
                continue;
            }

            AEItemKey key = AEItemKey.of(dropStack);

            // Test if the cell has space to accept this item
            long canInsert = mineCell.injectGeneratedDrop(key, dropStack.getCount(), Actionable.SIMULATE);
            if (canInsert <= 0) {
                // Cell is full or cannot accept this drop, stop immediately
                break;
            }

            // Only consume AE power if the item actually fits into the cell
            if (requireEnergy && energyPerDrop > 0) {
                double extracted = energyService.extractAEPower(energyPerDrop, Actionable.SIMULATE, PowerMultiplier.CONFIG);
                if (extracted < energyPerDrop) {
                    break; // Network ran out of power
                }
                energyService.extractAEPower(energyPerDrop, Actionable.MODULATE, PowerMultiplier.CONFIG);
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
