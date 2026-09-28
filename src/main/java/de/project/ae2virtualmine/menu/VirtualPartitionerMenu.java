package de.project.ae2virtualmine.menu;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import de.project.ae2virtualmine.cell.IVirtualMineCell;
import de.project.ae2virtualmine.cell.partition.MineCellPartition;
import de.project.ae2virtualmine.cell.partition.MineCellPartitionList;
import de.project.ae2virtualmine.config.VirtualMineConfig;
import de.project.ae2virtualmine.recipe.MineDropRegistry;
import de.project.ae2virtualmine.registry.ModBlocks;
import de.project.ae2virtualmine.registry.ModDataComponents;
import de.project.ae2virtualmine.registry.ModMenus;
import de.project.ae2virtualmine.util.CellWorkbenchPlayerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VirtualPartitionerMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerLevelAccess access;

    public VirtualPartitionerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(1),
                playerInventory.player.level() != null ?
                        ContainerLevelAccess.create(playerInventory.player.level(), extraData.readBlockPos()) :
                        ContainerLevelAccess.NULL);
    }

    public VirtualPartitionerMenu(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access) {
        super(ModMenus.PARTITIONER_MENU.get(), containerId);
        checkContainerSize(container, 1);
        this.container = container;
        this.access = access;
        container.startOpen(playerInventory.player);

        // Slot 0: Cell slot
        this.addSlot(new Slot(container, 0, 16, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof IVirtualMineCell;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Player Inventory (3 rows x 9 columns)
        int invStartX = 30;
        int invStartY = 158;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, invStartX + col * 18, invStartY + row * 18));
            }
        }

        // Hotbar (1 row x 9 columns)
        int hotbarStartY = 216;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, invStartX + col * 18, hotbarStartY));
        }
    }

    public Container getPartitionerContainer() {
        return container;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.VIRTUAL_PARTITIONER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index == 0) {
                // Move cell to player inventory
                if (!this.moveItemStackTo(stackInSlot, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From inventory
                if (stackInSlot.getItem() instanceof IVirtualMineCell) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 1 && index < 28) {
                    if (!this.moveItemStackTo(stackInSlot, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 28 && index < 37) {
                    if (!this.moveItemStackTo(stackInSlot, 1, 28, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public void applyPartitions(Player player, MineCellPartitionList newPartitions) {
        Slot cellSlot = this.slots.get(0);
        if (cellSlot == null || !cellSlot.hasItem()) {
            return;
        }
        ItemStack cellStack = cellSlot.getItem();
        if (!(cellStack.getItem() instanceof IVirtualMineCell)) {
            return;
        }

        if (newPartitions == null || newPartitions.isEmpty()) {
            cellStack.remove(ModDataComponents.PARTITIONS.get());
            cellStack.remove(AEComponents.STORAGE_CELL_CONFIG_INV);
            cellSlot.setChanged();
            broadcastChanges();
            return;
        }

        // Validate
        List<MineCellPartition> validList = new ArrayList<>();
        Set<Item> seen = new HashSet<>();
        int totalPercent = 0;

        for (MineCellPartition p : newPartitions.partitions()) {
            if (p == null || p.target() == null || p.percent() <= 0) continue;
            if (!seen.add(p.target())) continue; // No duplicates
            if (!MineDropRegistry.isValidMiningTarget(p.target(), player.level())) continue;

            if (VirtualMineConfig.SPEC.isLoaded() && VirtualMineConfig.ENFORCE_INVENTORY_CHECK.get() && !player.isCreative()) {
                if (!CellWorkbenchPlayerHelper.hasItemInInventory(player, p.target())) {
                    continue;
                }
            }

            totalPercent += p.percent();
            validList.add(p);
        }

        if (totalPercent > 100) {
            // Scale or cap down so it doesn't exceed 100%
            int sum = 0;
            List<MineCellPartition> adjusted = new ArrayList<>();
            for (MineCellPartition p : validList) {
                int allowed = Math.min(p.percent(), 100 - sum);
                if (allowed > 0) {
                    adjusted.add(new MineCellPartition(p.target(), allowed, p.voidSecondary()));
                    sum += allowed;
                }
            }
            validList = adjusted;
        }

        if (validList.isEmpty()) {
            cellStack.remove(ModDataComponents.PARTITIONS.get());
            cellStack.remove(AEComponents.STORAGE_CELL_CONFIG_INV);
        } else {
            cellStack.set(ModDataComponents.PARTITIONS.get(), new MineCellPartitionList(validList));
            // Keep AE2 config synced with primary target for GUI compatibility
            cellStack.set(AEComponents.STORAGE_CELL_CONFIG_INV,
                    List.of(new GenericStack(AEItemKey.of(validList.get(0).target()), 1)));
        }

        cellSlot.setChanged();
        broadcastChanges();

        if (player.level() != null && !player.level().isClientSide()) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.6f, 1.2f);
        }
    }
}
