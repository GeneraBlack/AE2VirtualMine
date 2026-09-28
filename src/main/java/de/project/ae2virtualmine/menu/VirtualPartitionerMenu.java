package de.project.ae2virtualmine.menu;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import de.project.ae2virtualmine.cell.VirtualMineCellItem;
import de.project.ae2virtualmine.cell.partition.MineCellPartition;
import de.project.ae2virtualmine.cell.partition.MineCellPartitionList;
import de.project.ae2virtualmine.config.VirtualMineConfig;
import de.project.ae2virtualmine.recipe.MineDropRegistry;
import de.project.ae2virtualmine.registry.ModBlocks;
import de.project.ae2virtualmine.registry.ModDataComponents;
import de.project.ae2virtualmine.registry.ModItems;
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

    private boolean loadingUpgrades = false;
    private ItemStack lastCellInSlot0 = ItemStack.EMPTY;

    private final SimpleContainer upgradeContainer = new SimpleContainer(5) {
        @Override
        public void setChanged() {
            super.setChanged();
            if (!loadingUpgrades) {
                saveUpgradesToCell();
            }
        }
    };

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
                return stack.getItem() instanceof VirtualMineCellItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void set(ItemStack stack) {
                super.set(stack);
                loadUpgradesFromCell();
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                loadUpgradesFromCell();
            }
        });

        // Slots 1..4: 4 Acceleration Card Slots (indices 0..3 in upgradeContainer)
        // Positioned: x = 52 + i * 18, y = 155
        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(this.upgradeContainer, i, 52 + i * 18, 155) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !container.getItem(0).isEmpty() && stack.is(appeng.core.definitions.AEItems.SPEED_CARD.asItem());
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean isActive() {
                    return !container.getItem(0).isEmpty();
                }
            });
        }

        // Slot 5: 1 Void Secondary Output Card Slot (index 4 in upgradeContainer)
        // Positioned with a gap: x = 138, y = 155
        this.addSlot(new Slot(this.upgradeContainer, 4, 138, 155) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !container.getItem(0).isEmpty() && (stack.is(ModItems.VOID_SECONDARY_CARD.get())
                        || stack.is(appeng.core.definitions.AEItems.VOID_CARD.asItem()));
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean isActive() {
                return !container.getItem(0).isEmpty();
            }
        });

        // Slots 6..32: Player Inventory (3 rows x 9 columns)
        int invStartX = 30;
        int invStartY = 180;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, invStartX + col * 18, invStartY + row * 18));
            }
        }

        // Slots 33..41: Hotbar (1 row x 9 columns)
        int hotbarStartY = 238;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, invStartX + col * 18, hotbarStartY));
        }

        loadUpgradesFromCell();
    }

    private void loadUpgradesFromCell() {
        loadingUpgrades = true;
        try {
            upgradeContainer.clearContent();
            ItemStack cell = container.getItem(0);
            if (!cell.isEmpty() && cell.getItem() instanceof VirtualMineCellItem) {
                var inv = appeng.api.upgrades.UpgradeInventories.forItem(cell, 5);
                int speedIdx = 0;
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack upgrade = inv.getStackInSlot(i);
                    if (upgrade.isEmpty()) continue;
                    if (upgrade.is(appeng.core.definitions.AEItems.SPEED_CARD.asItem())) {
                        if (speedIdx < 4) {
                            upgradeContainer.setItem(speedIdx++, upgrade.copyWithCount(1));
                        }
                    } else if (upgrade.is(ModItems.VOID_SECONDARY_CARD.get())
                            || upgrade.is(appeng.core.definitions.AEItems.VOID_CARD.asItem())) {
                        upgradeContainer.setItem(4, upgrade.copyWithCount(1));
                    }
                }
            }
        } finally {
            loadingUpgrades = false;
        }
    }

    private void saveUpgradesToCell() {
        ItemStack cell = container.getItem(0);
        if (!cell.isEmpty() && cell.getItem() instanceof VirtualMineCellItem) {
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                ItemStack stack = upgradeContainer.getItem(i);
                if (!stack.isEmpty()) {
                    list.add(stack.copy());
                }
            }
            if (list.isEmpty()) {
                cell.remove(AEComponents.UPGRADES);
            } else {
                cell.set(AEComponents.UPGRADES, net.minecraft.world.item.component.ItemContainerContents.fromItems(list));
            }
            container.setChanged();
            Slot cellSlot = this.slots.get(0);
            if (cellSlot != null) {
                cellSlot.setChanged();
            }
            broadcastChanges();
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
    public void broadcastChanges() {
        ItemStack currentCell = container.getItem(0);
        if (!ItemStack.matches(currentCell, lastCellInSlot0)) {
            lastCellInSlot0 = currentCell.copy();
            loadUpgradesFromCell();
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();

            if (index == 0) {
                // Move cell to player inventory / hotbar (slots 6..42)
                if (!this.moveItemStackTo(stackInSlot, 6, 42, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= 1 && index <= 5) {
                // Move upgrade to player inventory / hotbar (slots 6..42)
                if (!this.moveItemStackTo(stackInSlot, 6, 42, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From inventory / hotbar (index >= 6)
                if (stackInSlot.getItem() instanceof VirtualMineCellItem) {
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stackInSlot.is(appeng.core.definitions.AEItems.SPEED_CARD.asItem())) {
                    // Try moving to acceleration slots (1..5)
                    if (!this.moveItemStackTo(stackInSlot, 1, 5, false)) {
                        if (index >= 6 && index < 33) {
                            if (!this.moveItemStackTo(stackInSlot, 33, 42, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else if (index >= 33 && index < 42) {
                            if (!this.moveItemStackTo(stackInSlot, 6, 33, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                } else if (stackInSlot.is(ModItems.VOID_SECONDARY_CARD.get())
                        || stackInSlot.is(appeng.core.definitions.AEItems.VOID_CARD.asItem())) {
                    // Try moving to void slot (5..6)
                    if (!this.moveItemStackTo(stackInSlot, 5, 6, false)) {
                        if (index >= 6 && index < 33) {
                            if (!this.moveItemStackTo(stackInSlot, 33, 42, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else if (index >= 33 && index < 42) {
                            if (!this.moveItemStackTo(stackInSlot, 6, 33, false)) {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                } else if (index >= 6 && index < 33) {
                    if (!this.moveItemStackTo(stackInSlot, 33, 42, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= 33 && index < 42) {
                    if (!this.moveItemStackTo(stackInSlot, 6, 33, false)) {
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
        if (!(cellStack.getItem() instanceof VirtualMineCellItem)) {
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
