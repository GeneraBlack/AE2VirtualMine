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
import net.minecraft.world.InteractionResult;
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
        return UpgradeInventories.forItem(stack, 5);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack stack) {
        var holder = new ConfigHolder(stack);
        holder.inv = ConfigInventory.configTypes(63)
                .supportedTypes(Set.of(AEKeyType.items()))
                .slotFilter((slot, what) -> isKeyAllowedInConfig(what, stack))
                .changeListener(holder::save)
                .build();
        holder.load();
        return holder.inv;
    }

    private static boolean isKeyAllowedInConfig(appeng.api.stacks.AEKey what, ItemStack cellStack) {
        if (!(what instanceof AEItemKey itemKey)) {
            return false;
        }
        Item item = itemKey.getItem();
        // 1. Must be a valid mining target
        if (!MineDropRegistry.isValidMiningTarget(item, null)) {
            return false;
        }
        // 2. Enforce inventory check if configured
        if (VirtualMineConfig.SPEC.isLoaded() && VirtualMineConfig.ENFORCE_INVENTORY_CHECK.get()) {
            Player player = de.project.ae2virtualmine.util.CellWorkbenchPlayerHelper.getCurrentPlayer(cellStack);
            if (player != null && !de.project.ae2virtualmine.util.CellWorkbenchPlayerHelper.hasItemInInventory(player, item)) {
                return false;
            }
        }
        return true;
    }

    private static class ConfigHolder {
        private final ItemStack stack;
        private ConfigInventory inv;

        public ConfigHolder(ItemStack stack) {
            this.stack = stack;
        }

        public void load() {
            inv.readFromList(stack.getOrDefault(AEComponents.STORAGE_CELL_CONFIG_INV, List.of()));
        }

        public void save() {
            stack.set(AEComponents.STORAGE_CELL_CONFIG_INV, inv.toList());
        
            stack.remove(de.project.ae2virtualmine.registry.ModDataComponents.PARTITIONS.get());
        }
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

        var upgrades = UpgradeInventories.forItem(stack, 5);
        int speedCards = Math.min(4, upgrades.getInstalledUpgrades(appeng.core.definitions.AEItems.SPEED_CARD.asItem()));
        if (speedCards > 0) {
            double factor = switch (speedCards) {
                case 1 -> 0.70;
                case 2 -> 0.45;
                case 3 -> 0.30;
                case 4 -> 0.20;
                default -> 1.0;
            };
            seconds = (intervalTicks * factor) / 20.0;
        }

        lines.add(Component.translatable("tooltip.ae2virtualmine.tier", tier.getTierName())
                .withStyle(ChatFormatting.GOLD));

        if (speedCards > 0) {
            lines.add(Component.translatable("tooltip.ae2virtualmine.production_speed", drops, String.format(Locale.ROOT, "%.1f", seconds), speedCards)
                    .withStyle(ChatFormatting.AQUA));
        } else {
            lines.add(Component.translatable("tooltip.ae2virtualmine.production", drops, String.format(Locale.ROOT, "%.1f", seconds))
                    .withStyle(ChatFormatting.GRAY));
        }

        boolean hasVoidSecondary = de.project.ae2virtualmine.util.VirtualCellAdapter.hasVoidSecondaryCard(upgrades);
        if (hasVoidSecondary) {
            lines.add(Component.translatable("tooltip.ae2virtualmine.void_secondary_active")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }

        if (stack.has(de.project.ae2virtualmine.registry.ModDataComponents.PARTITIONS.get())) {
            var partitionList = stack.get(de.project.ae2virtualmine.registry.ModDataComponents.PARTITIONS.get());
            if (partitionList != null && !partitionList.isEmpty()) {
                lines.add(Component.translatable("tooltip.ae2virtualmine.partitions_header", partitionList.size())
                        .withStyle(ChatFormatting.AQUA));
                for (var p : partitionList.partitions()) {
                    var line = Component.literal(" ▪ ")
                            .append(Component.translatable(p.target().getDescriptionId()).withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(" (" + p.percent() + "%)").withStyle(ChatFormatting.GRAY));
                    if (p.voidSecondary()) {
                        line.append(Component.literal(" [Void]").withStyle(ChatFormatting.DARK_PURPLE));
                    }
                    lines.add(line);
                }
                if (partitionList.getUnallocatedPercent() > 0) {
                    lines.add(Component.literal(" ▪ Unallocated: " + partitionList.getUnallocatedPercent() + "%")
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            } else {
                lines.add(Component.translatable("tooltip.ae2virtualmine.not_configured")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
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
                    if (VirtualMineConfig.SPEC.isLoaded() && VirtualMineConfig.ENFORCE_INVENTORY_CHECK.get()) {
                        if (!de.project.ae2virtualmine.util.CellWorkbenchPlayerHelper.hasItemInInventory(player, otherStack.getItem())) {
                            return InteractionResultHolder.fail(stack);
                        }
                    }
                    if (!level.isClientSide()) {
                        AEItemKey key = AEItemKey.of(otherStack.getItem());
                        stack.set(AEComponents.STORAGE_CELL_CONFIG_INV, List.of(new GenericStack(key, 1)));
                        stack.remove(de.project.ae2virtualmine.registry.ModDataComponents.PARTITIONS.get());
                        player.displayClientMessage(Component.translatable("message.ae2virtualmine.configured",
                                Component.translatable(otherStack.getItem().getDescriptionId())).withStyle(ChatFormatting.GOLD), true);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            } else {
                // Clear configuration — only if cell is empty to prevent accidental wipe
                if (!level.isClientSide()) {
                    appeng.api.storage.cells.StorageCell cell = appeng.api.storage.StorageCells.getCellInventory(stack, null);
                    if (cell instanceof VirtualMineCellInventory mineInv && mineInv.getStoredItemCount() > 0) {
                        player.displayClientMessage(Component.translatable("message.ae2virtualmine.clear_blocked")
                                .withStyle(ChatFormatting.RED), true);
                    } else {
                        stack.remove(AEComponents.STORAGE_CELL_CONFIG_INV);
                        stack.remove(de.project.ae2virtualmine.registry.ModDataComponents.PARTITIONS.get());
                        player.displayClientMessage(Component.translatable("message.ae2virtualmine.cleared")
                                .withStyle(ChatFormatting.RED), true);
                    }
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        return super.use(level, player, hand);
    }
}