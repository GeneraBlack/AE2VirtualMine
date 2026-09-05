package de.project.ae2virtualmine.cell;

import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.AEConfig;
import appeng.core.localization.Tooltips;
import appeng.items.contents.CellConfig;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.util.ConfigInventory;
import de.project.ae2virtualmine.config.VirtualMineConfig;
import de.project.ae2virtualmine.recipe.MineDropRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.*;

public class VirtualMineCellItem extends Item implements ICellWorkbenchItem {

    private final MineCellTier tier;

    public VirtualMineCellItem(MineCellTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public MineCellTier getTier() {
        return tier;
    }

    public int getBytes(ItemStack stack) {
        return tier.getTotalBytes();
    }

    public int getBytesPerType(ItemStack stack) {
        return tier.getBytesPerType();
    }

    public int getTotalTypes(ItemStack stack) {
        return tier.getTotalTypes();
    }

    public double getIdleDrain() {
        return tier.getIdleDrain();
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack stack) {
        return UpgradeInventories.forItem(stack, 4);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack stack) {
        return CellConfig.create(Set.of(AEKeyType.items()), stack);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack stack) {
        return stack.getOrDefault(AEComponents.STORAGE_CELL_FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    @Override
    public void setFuzzyMode(ItemStack stack, FuzzyMode mode) {
        stack.set(AEComponents.STORAGE_CELL_FUZZY_MODE, mode);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);

        StorageCell cell = StorageCells.getCellInventory(stack, null);
        if (cell instanceof VirtualMineCellInventory mineInv) {
            lines.add(Tooltips.bytesUsed(mineInv.getUsedBytes(), mineInv.getTotalBytes()));
            lines.add(Tooltips.typesUsed(mineInv.getStoredItemTypes(), mineInv.getTotalItemTypes()));
        } else {
            lines.add(Tooltips.bytesUsed(0, tier.getTotalBytes()));
            lines.add(Tooltips.typesUsed(0, tier.getTotalTypes()));
        }

        int drops = tier.getDropCount();
        int intervalTicks = VirtualMineConfig.BASE_TICK_INTERVAL.get();
        double seconds = intervalTicks / 20.0;

        lines.add(Component.translatable("tooltip.ae2virtualmine.tier", tier.getTierName())
                .withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.ae2virtualmine.production", drops, String.format(Locale.ROOT, "%.1f", seconds))
                .withStyle(ChatFormatting.GRAY));

        List<GenericStack> config = stack.get(AEComponents.STORAGE_CELL_CONFIG_INV);
        Item configuredItem = null;
        if (config != null && !config.isEmpty()) {
            for (GenericStack entry : config) {
                if (entry != null && entry.what() instanceof AEItemKey itemKey) {
                    configuredItem = itemKey.getItem();
                    break;
                }
            }
        }

        if (configuredItem != null) {
            lines.add(Component.translatable("tooltip.ae2virtualmine.configured_target",
                            Component.translatable(configuredItem.getDescriptionId()))
                    .withStyle(ChatFormatting.YELLOW));
        } else {
            lines.add(Component.translatable("tooltip.ae2virtualmine.not_configured")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        StorageCell cell = StorageCells.getCellInventory(stack, null);
        if (!(cell instanceof VirtualMineCellInventory mineInv)) {
            return Optional.empty();
        }

        List<ItemStack> upgradeStacks = new ArrayList<>();
        try {
            if (AEConfig.instance().isTooltipShowCellUpgrades()) {
                for (ItemStack upgrade : getUpgrades(stack)) {
                    if (!upgrade.isEmpty()) {
                        upgradeStacks.add(upgrade);
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        List<GenericStack> content = new ArrayList<>();
        try {
            if (AEConfig.instance().isTooltipShowCellContent()) {
                int maxCountShown = AEConfig.instance().getTooltipMaxCellContentShown();
                KeyCounter availableStacks = new KeyCounter();
                mineInv.getAvailableStacks(availableStacks);
                for (var entry : availableStacks) {
                    content.add(new GenericStack(entry.getKey(), entry.getLongValue()));
                }

                content.sort(Comparator.comparingLong(GenericStack::amount).reversed());
                boolean hasMoreContent = content.size() > maxCountShown;
                if (content.size() > maxCountShown) {
                    content = new ArrayList<>(content.subList(0, maxCountShown));
                }
                return Optional.of(new StorageCellTooltipComponent(upgradeStacks, content, hasMoreContent, true));
            }
        } catch (Throwable ignored) {
        }

        return Optional.of(new StorageCellTooltipComponent(upgradeStacks, Collections.emptyList(), false, true));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack otherStack = player.getItemInHand(otherHand);

        if (player.isShiftKeyDown()) {
            if (!otherStack.isEmpty()) {
                // Quick-partition using item in off-hand
                if (MineDropRegistry.isValidMiningTarget(otherStack.getItem(), level)) {
                    if (!level.isClientSide()) {
                        AEItemKey key = AEItemKey.of(otherStack.getItem());
                        stack.set(AEComponents.STORAGE_CELL_CONFIG_INV, List.of(new GenericStack(key, 1)));
                        player.displayClientMessage(Component.translatable("message.ae2virtualmine.configured",
                                Component.translatable(otherStack.getItem().getDescriptionId())).withStyle(ChatFormatting.GOLD), true);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            } else {
                // Clear configuration
                if (!level.isClientSide()) {
                    stack.remove(AEComponents.STORAGE_CELL_CONFIG_INV);
                    player.displayClientMessage(Component.translatable("message.ae2virtualmine.cleared")
                            .withStyle(ChatFormatting.RED), true);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        return super.use(level, player, hand);
    }
}
