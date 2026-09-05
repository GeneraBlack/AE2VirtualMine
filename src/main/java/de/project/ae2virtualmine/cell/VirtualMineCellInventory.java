package de.project.ae2virtualmine.cell;

import appeng.api.config.Actionable;
import appeng.api.ids.AEComponents;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import de.project.ae2virtualmine.recipe.MineDropEntry;
import de.project.ae2virtualmine.recipe.MineDropRegistry;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VirtualMineCellInventory implements IVirtualMineCell {

    private final ItemStack stack;
    private final @Nullable ISaveProvider host;
    private final MineCellTier tier;
    private final Object2LongMap<AEKey> storedAmounts = new Object2LongOpenHashMap<>();
    private long storedItemCount = 0;
    private int storedItems = 0;
    private boolean isPersisted = true;

    public VirtualMineCellInventory(ItemStack stack, @Nullable ISaveProvider host, MineCellTier tier) {
        this.stack = stack;
        this.host = host;
        this.tier = tier;
        this.loadCellItems();
    }

    private void loadCellItems() {
        List<GenericStack> stacks = stack.get(AEComponents.STORAGE_CELL_INV);
        if (stacks != null) {
            for (GenericStack entry : stacks) {
                if (entry != null && entry.amount() > 0) {
                    this.storedAmounts.put(entry.what(), entry.amount());
                    this.storedItemCount += entry.amount();
                }
            }
        }
        this.storedItems = this.storedAmounts.size();
    }

    @Override
    public ItemStack getItemStack() {
        return stack;
    }

    @Nullable
    @Override
    public ISaveProvider getSaveProvider() {
        return host;
    }

    @Override
    public MineCellTier getTier() {
        return tier;
    }

    @Nullable
    @Override
    public Item getConfiguredTarget() {
        List<GenericStack> config = stack.get(AEComponents.STORAGE_CELL_CONFIG_INV);
        if (config != null && !config.isEmpty()) {
            for (GenericStack entry : config) {
                if (entry != null && entry.what() instanceof AEItemKey itemKey) {
                    return itemKey.getItem();
                }
            }
        }
        return null;
    }

    public long getTotalBytes() {
        return tier.getTotalBytes();
    }

    public int getBytesPerType() {
        return tier.getBytesPerType();
    }

    public int getTotalItemTypes() {
        return tier.getTotalTypes();
    }

    public long getStoredItemCount() {
        return storedItemCount;
    }

    public int getStoredItemTypes() {
        return storedItems;
    }

    public int getUnusedItemCount() {
        int rem = (int) (this.storedItemCount % 8L);
        return rem == 0 ? 0 : (8 - rem);
    }

    public long getUsedBytes() {
        long bytesForItemCount = (this.storedItemCount + 7L) / 8L;
        return (long) this.storedItems * this.tier.getBytesPerType() + bytesForItemCount;
    }

    public long getFreeBytes() {
        return Math.max(0L, this.tier.getTotalBytes() - this.getUsedBytes());
    }

    public long getRemainingItemTypes() {
        long basedOnStorage = this.getFreeBytes() / this.tier.getBytesPerType();
        long basedOnTotal = this.tier.getTotalTypes() - this.storedItems;
        return Math.max(0L, Math.min(basedOnStorage, basedOnTotal));
    }

    public boolean canHoldNewItem() {
        long freeBytes = this.getFreeBytes();
        return (freeBytes > this.tier.getBytesPerType()
                || (freeBytes == this.tier.getBytesPerType() && this.getUnusedItemCount() > 0))
                && this.getRemainingItemTypes() > 0;
    }

    public long getRemainingItemCount() {
        long remaining = this.getFreeBytes() * 8L + (long) this.getUnusedItemCount();
        return Math.max(0L, remaining);
    }

    @Override
    public boolean isFull() {
        return getStatus() == CellState.FULL || getRemainingItemCount() <= 0;
    }

    @Override
    public CellState getStatus() {
        if (this.storedItems == 0) {
            return CellState.EMPTY;
        }
        if (this.canHoldNewItem()) {
            return CellState.NOT_EMPTY;
        }
        if (this.getRemainingItemCount() > 0) {
            return CellState.TYPES_FULL;
        }
        return CellState.FULL;
    }

    @Override
    public long injectGeneratedDrop(AEKey key, long amount, Actionable mode) {
        if (amount <= 0 || isFull()) {
            return 0;
        }

        long currentAmount = this.storedAmounts.getLong(key);
        long remainingItemCount = this.getRemainingItemCount();

        // If this key is not yet present, check if we can allocate a new type
        if (currentAmount <= 0) {
            if (!canHoldNewItem()) {
                return 0;
            }
            remainingItemCount -= (long) this.tier.getBytesPerType() * 8L;
            if (remainingItemCount <= 0) {
                return 0;
            }
        }

        long toInsert = Math.min(amount, remainingItemCount);
        if (toInsert <= 0) {
            return 0;
        }

        if (mode == Actionable.MODULATE) {
            this.storedAmounts.put(key, currentAmount + toInsert);
            this.saveChanges();
        }

        return toInsert;
    }

    @Override
    public double getIdleDrain() {
        return tier.getIdleDrain();
    }

    @Override
    public void persist() {
        if (this.isPersisted) {
            return;
        }

        long itemCount = 0L;
        List<GenericStack> stacks = new ArrayList<>(storedAmounts.size());

        for (var entry : this.storedAmounts.object2LongEntrySet()) {
            long amount = entry.getLongValue();
            if (amount > 0) {
                itemCount += amount;
                stacks.add(new GenericStack(entry.getKey(), amount));
            }
        }

        if (stacks.isEmpty()) {
            stack.remove(AEComponents.STORAGE_CELL_INV);
        } else {
            stack.set(AEComponents.STORAGE_CELL_INV, stacks);
        }

        this.storedItems = this.storedAmounts.size();
        this.storedItemCount = itemCount;
        this.isPersisted = true;
    }

    protected void saveChanges() {
        this.storedItems = this.storedAmounts.size();
        this.storedItemCount = 0;
        for (long storedAmount : this.storedAmounts.values()) {
            this.storedItemCount += storedAmount;
        }

        this.isPersisted = false;
        if (this.host != null) {
            this.host.saveChanges();
        } else {
            this.persist();
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0) {
            return 0;
        }

        Item configuredTarget = getConfiguredTarget();
        if (configuredTarget == null) {
            return 0;
        }

        if (!(what instanceof AEItemKey itemKey)) {
            return 0;
        }

        Item item = itemKey.getItem();
        boolean allowed = false;
        if (item.equals(configuredTarget)) {
            allowed = true;
        } else {
            List<MineDropEntry> drops = MineDropRegistry.getDropEntries(configuredTarget, null);
            for (MineDropEntry entry : drops) {
                if (entry.item().is(item)) {
                    allowed = true;
                    break;
                }
            }
        }

        if (!allowed) {
            return 0;
        }

        return injectGeneratedDrop(what, amount, mode);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (amount <= 0) {
            return 0;
        }
        long currentAmount = this.storedAmounts.getLong(what);
        if (currentAmount <= 0) {
            return 0;
        }
        long toExtract = Math.min(amount, currentAmount);
        if (mode == Actionable.MODULATE) {
            if (currentAmount == toExtract) {
                this.storedAmounts.removeLong(what);
            } else {
                this.storedAmounts.put(what, currentAmount - toExtract);
            }
            this.saveChanges();
        }
        return toExtract;
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (var entry : Object2LongMaps.fastIterable(this.storedAmounts)) {
            out.add(entry.getKey(), entry.getLongValue());
        }
    }

    @Override
    public Component getDescription() {
        return stack.getHoverName();
    }
}
