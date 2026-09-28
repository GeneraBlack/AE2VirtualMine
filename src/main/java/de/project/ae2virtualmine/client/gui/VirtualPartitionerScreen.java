package de.project.ae2virtualmine.client.gui;

import de.project.ae2virtualmine.cell.VirtualMineCellItem;
import de.project.ae2virtualmine.registry.ModDataComponents;
import de.project.ae2virtualmine.cell.partition.MineCellPartition;
import de.project.ae2virtualmine.cell.partition.MineCellPartitionList;
import de.project.ae2virtualmine.menu.VirtualPartitionerMenu;
import de.project.ae2virtualmine.network.SetPartitionsPayload;
import de.project.ae2virtualmine.recipe.MineDropRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class VirtualPartitionerScreen extends AbstractContainerScreen<VirtualPartitionerMenu> {

    private static final int[] PALETTE = {
            0xFF2E86AB, // Blue
            0xFF2BA84A, // Green
            0xFFE08D3C, // Orange
            0xFF8338EC, // Purple
            0xFFE63946, // Red
            0xFF00B4D8  // Cyan
    };

    public static class PartitionDraft {
        public Item target;
        public int percent;
        public boolean voidSecondary;

        public PartitionDraft(Item target, int percent, boolean voidSecondary) {
            this.target = target;
            this.percent = percent;
            this.voidSecondary = voidSecondary;
        }
    }

    private final List<PartitionDraft> workingList = new ArrayList<>();
    private ItemStack lastCellStack = ItemStack.EMPTY;
    private boolean dirty = false;
    private int selectedRowForPicker = -1;
    private int scrollOffset = 0;

    public VirtualPartitionerScreen(VirtualPartitionerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 220;
        this.imageHeight = 262;
        this.inventoryLabelY = 169;
        this.inventoryLabelX = 30;
    }

    @Override
    protected void init() {
        super.init();
        syncFromCell(true);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack currentCell = menu.getSlot(0).getItem();
        if (!ItemStack.matches(currentCell, lastCellStack)) {
            syncFromCell(false);
        }
    }

    private void syncFromCell(boolean force) {
        ItemStack currentCell = menu.getSlot(0).getItem();
        if (force || !ItemStack.isSameItemSameComponents(currentCell, lastCellStack)) {
            lastCellStack = currentCell.copy();
            workingList.clear();
            selectedRowForPicker = -1;
            scrollOffset = 0;
            dirty = false;

            if (!currentCell.isEmpty() && currentCell.getItem() instanceof VirtualMineCellItem) {
                MineCellPartitionList list = currentCell.get(ModDataComponents.PARTITIONS.get());
                if (list != null && !list.isEmpty()) {
                    for (MineCellPartition p : list.partitions()) {
                        workingList.add(new PartitionDraft(p.target(), p.percent(), p.voidSecondary()));
                    }
                }
            }
        }
    }

    private int getTotalPercent() {
        int sum = 0;
        for (PartitionDraft d : workingList) {
            sum += d.percent;
        }
        return sum;
    }

    private int getUnallocatedPercent() {
        return Math.max(0, 100 - getTotalPercent());
    }

    private boolean hasVoidCard() {
        ItemStack cell = menu.getSlot(0).getItem();
        if (!cell.isEmpty()) {
            if (menu.getSlot(5).hasItem()) return true;
            var upgrades = appeng.api.upgrades.UpgradeInventories.forItem(cell, 5);
            return upgrades != null && (upgrades.isInstalled(de.project.ae2virtualmine.registry.ModItems.VOID_SECONDARY_CARD.get())
                || upgrades.isInstalled(appeng.core.definitions.AEItems.VOID_CARD.asItem()));
        }
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderCustomTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Window Background (GParted dark workstation theme)
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1E1E1E);
        // Bevel borders
        guiGraphics.fill(x, y, x + imageWidth, y + 1, 0xFF4A4A4A);
        guiGraphics.fill(x, y, x + 1, y + imageHeight, 0xFF4A4A4A);
        guiGraphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, 0xFF0D0D0D);
        guiGraphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, 0xFF0D0D0D);

        // Header separator
        guiGraphics.fill(x + 8, y + 16, x + imageWidth - 8, y + 17, 0xFF333333);

        // Cell slot background (x=16, y=20)
        drawSlotBox(guiGraphics, x + 15, y + 19);

        // Drive Info Header
        ItemStack cell = menu.getSlot(0).getItem();
        if (!cell.isEmpty() && cell.getItem() instanceof VirtualMineCellItem virtualCell) {
            String tierName = virtualCell.getTier().getTierName() + " Virtual Drive";
            guiGraphics.drawString(font, tierName, x + 38, y + 21, 0xFF55FF55, false);

            long totalBytes = virtualCell.getTier().getTotalBytes();
            String stats = String.format("Capacity: %,d B | Allocated: %d%%", totalBytes, getTotalPercent());
            guiGraphics.drawString(font, stats, x + 38, y + 30, 0xFFAAAAAA, false);
        } else {
            guiGraphics.drawString(font, "No Drive Connected", x + 38, y + 21, 0xFFFF5555, false);
            guiGraphics.drawString(font, "Insert a Virtual Cell below", x + 38, y + 30, 0xFF777777, false);
        }

        // GParted Disk Visual Bar (x=12, y=42, w=196, h=14)
        int barX = x + 12;
        int barY = y + 42;
        int barW = 196;
        int barH = 14;

        // Bar border
        guiGraphics.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF0A0A0A);

        if (cell.isEmpty() || !(cell.getItem() instanceof VirtualMineCellItem)) {
            guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF2A2A2A);
            guiGraphics.drawCenteredString(font, "NO DISK DETECTED", barX + barW / 2, barY + 3, 0xFF555555);
        } else {
            int currentX = barX;
            for (int i = 0; i < workingList.size(); i++) {
                PartitionDraft p = workingList.get(i);
                int sliceW = (int) Math.round((p.percent / 100.0) * barW);
                if (sliceW > 0) {
                    int color = PALETTE[i % PALETTE.length];
                    guiGraphics.fill(currentX, barY, Math.min(barX + barW, currentX + sliceW), barY + barH, color);
                    // 1px separator
                    guiGraphics.fill(currentX + sliceW - 1, barY, currentX + sliceW, barY + barH, 0xFF111111);
                    currentX += sliceW;
                }
            }
            // Unallocated slice
            if (currentX < barX + barW) {
                guiGraphics.fill(currentX, barY, barX + barW, barY + barH, 0xFF353535);
                if (barX + barW - currentX > 30) {
                    guiGraphics.drawCenteredString(font, getUnallocatedPercent() + "% Free", (currentX + barX + barW) / 2, barY + 3, 0xFF888888);
                }
            }
        }

        // Partition Table Area (y=58 to y=134)
        int tableX = x + 12;
        int tableY = y + 58;
        int tableW = 196;
        int tableH = 76;
        guiGraphics.fill(tableX, tableY, tableX + tableW, tableY + tableH, 0xFF141414);
        guiGraphics.fill(tableX, tableY, tableX + tableW, tableY + 1, 0xFF282828);

        // Table header labels
        guiGraphics.drawString(font, "Target", tableX + 22, tableY + 3, 0xFF888888, false);
        guiGraphics.drawString(font, "Alloc", tableX + 104, tableY + 3, 0xFF888888, false);
        guiGraphics.drawString(font, "Void", tableX + 144, tableY + 3, 0xFF888888, false);

        // Partition Rows (up to 3 visible rows, height = 20)
        int maxVisible = 3;
        if (workingList.isEmpty()) {
            if (!cell.isEmpty()) {
                guiGraphics.drawCenteredString(font, "No partitions. Click '+ Add' below.", tableX + tableW / 2, tableY + 32, 0xFF666666);
            }
        } else {
            for (int i = 0; i < maxVisible; i++) {
                int index = scrollOffset + i;
                if (index >= workingList.size()) break;

                PartitionDraft p = workingList.get(index);
                int rowY = tableY + 14 + i * 20;

                // Alternate row tint
                if (index % 2 == 1) {
                    guiGraphics.fill(tableX + 1, rowY - 1, tableX + tableW - 1, rowY + 19, 0xFF1A1A1A);
                }

                // Color swatch
                int color = PALETTE[index % PALETTE.length];
                guiGraphics.fill(tableX + 4, rowY + 1, tableX + 7, rowY + 17, color);

                // Target box (18x18)
                int boxX = tableX + 10;
                int boxY = rowY;
                int boxColor = (selectedRowForPicker == index) ? 0xFFFFFF00 : 0xFF3A3A3A;
                guiGraphics.fill(boxX - 1, boxY - 1, boxX + 17, boxY + 17, boxColor);
                guiGraphics.fill(boxX, boxY, boxX + 16, boxY + 16, 0xFF222222);

                if (p.target != null) {
                    guiGraphics.renderItem(new ItemStack(p.target), boxX, boxY);
                }

                // Target name (truncated)
                String name = (p.target != null) ? new ItemStack(p.target).getHoverName().getString() : "[Select]";
                if (font.width(name) > 52) {
                    name = font.plainSubstrByWidth(name, 48) + "..";
                }
                guiGraphics.drawString(font, name, tableX + 30, rowY + 4, 0xFFDDDDDD, false);

                // [-] button (x = tableX + 88)
                drawButton(guiGraphics, tableX + 88, rowY + 3, 11, 11, "-", 0xFFE0E0E0);

                // Percent text
                String pctStr = p.percent + "%";
                guiGraphics.drawCenteredString(font, pctStr, tableX + 111, rowY + 4, 0xFFFFFFFF);

                // [+] button (x = tableX + 122)
                drawButton(guiGraphics, tableX + 122, rowY + 3, 11, 11, "+", 0xFFE0E0E0);

                // [Void] toggle button (x = tableX + 138)
                int voidBg;
                int voidText;
                if (!hasVoidCard()) {
                    voidBg = 0xFF1A1A1A;
                    voidText = 0xFF444444;
                } else {
                    voidBg = p.voidSecondary ? 0xFF6A0DAD : 0xFF2C2C2C;
                    voidText = p.voidSecondary ? 0xFFFFFFFF : 0xFF777777;
                }
                drawButtonWithCustomBg(guiGraphics, tableX + 138, rowY + 3, 26, 11, "Void", voidText, voidBg);

                // [X] delete button (x = tableX + 172)
                drawButton(guiGraphics, tableX + 170, rowY + 3, 11, 11, "×", 0xFFFF5555);
            }
        }

        // Scroll buttons if needed
        if (workingList.size() > maxVisible) {
            drawButton(guiGraphics, tableX + tableW - 11, tableY + 14, 9, 9, "▲", scrollOffset > 0 ? 0xFFFFFFFF : 0xFF555555);
            drawButton(guiGraphics, tableX + tableW - 11, tableY + tableH - 12, 9, 9, "▼", (scrollOffset + maxVisible < workingList.size()) ? 0xFFFFFFFF : 0xFF555555);
        }

        // Action Buttons Row (y=138)
        int btnY = y + 137;
        boolean hasCell = !cell.isEmpty() && cell.getItem() instanceof VirtualMineCellItem;
        int addColor = (hasCell && workingList.size() < 6 && getUnallocatedPercent() > 0) ? 0xFFFFFFFF : 0xFF666666;
        drawButton(guiGraphics, x + 12, btnY, 36, 14, "+ Add", addColor);

        int eqColor = (hasCell && !workingList.isEmpty()) ? 0xFFFFFFFF : 0xFF666666;
        drawButton(guiGraphics, x + 51, btnY, 44, 14, "Equalize", eqColor);

        int clearColor = (hasCell && !workingList.isEmpty()) ? 0xFFFF7777 : 0xFF666666;
        drawButton(guiGraphics, x + 98, btnY, 36, 14, "Clear", clearColor);

        // Apply & Format button (green when dirty)
        int applyBg = dirty ? 0xFF1B4332 : 0xFF2C2C2C;
        int applyText = dirty ? 0xFF55FF55 : (hasCell ? 0xFFCCCCCC : 0xFF666666);
        drawButtonWithCustomBg(guiGraphics, x + 138, btnY, 70, 14, dirty ? "Apply *" : "Apply", applyText, applyBg);

        // Upgrade Slots Row (y=155)
        guiGraphics.drawString(font, "Upgrades:", x + 10, y + 158, 0xFF888888, false);
        // 4 Acceleration card slots (x = 52 + i * 18)
        for (int i = 0; i < 4; i++) {
            int slotX = x + 51 + i * 18;
            int slotY = y + 154;
            if (hasCell) {
                drawSlotBox(guiGraphics, slotX, slotY);
            } else {
                guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF1A1A1A);
                guiGraphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF111111);
            }
        }
        // 1 Void Secondary card slot (x = 138)
        int voidSlotX = x + 137;
        int voidSlotY = y + 154;
        if (hasCell) {
            drawVoidSlotBox(guiGraphics, voidSlotX, voidSlotY);
        } else {
            guiGraphics.fill(voidSlotX, voidSlotY, voidSlotX + 18, voidSlotY + 18, 0xFF1A1A1A);
            guiGraphics.fill(voidSlotX + 1, voidSlotY + 1, voidSlotX + 17, voidSlotY + 17, 0xFF111111);
        }

        // Player Inventory slots
        int invStartX = x + 30;
        int invStartY = y + 180;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                drawSlotBox(guiGraphics, invStartX + col * 18 - 1, invStartY + row * 18 - 1);
            }
        }

        // Hotbar slots
        int hotbarStartY = y + 238;
        for (int col = 0; col < 9; ++col) {
            drawSlotBox(guiGraphics, invStartX + col * 18 - 1, hotbarStartY - 1);
        }
    }

    private void drawSlotBox(GuiGraphics guiGraphics, int sx, int sy) {
        guiGraphics.fill(sx, sy, sx + 18, sy + 18, 0xFF373737);
        guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        guiGraphics.fill(sx + 1, sy + 1, sx + 16, sy + 16, 0xFF373737);
        guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);
        guiGraphics.fill(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);
        guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF1A1A1A);
    }

    private void drawVoidSlotBox(GuiGraphics guiGraphics, int sx, int sy) {
        guiGraphics.fill(sx, sy, sx + 18, sy + 18, 0xFF5A189A); // Subtle purple highlight
        guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF8B8B8B);
        guiGraphics.fill(sx + 1, sy + 1, sx + 16, sy + 16, 0xFF373737);
        guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF373737);
        guiGraphics.fill(sx + 1, sy + 1, sx + 2, sy + 17, 0xFF373737);
        guiGraphics.fill(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF1A1A1A);
    }

    private void drawButton(GuiGraphics guiGraphics, int bx, int by, int bw, int bh, String label, int textColor) {
        drawButtonWithCustomBg(guiGraphics, bx, by, bw, bh, label, textColor, 0xFF2C2C2C);
    }

    private void drawButtonWithCustomBg(GuiGraphics guiGraphics, int bx, int by, int bw, int bh, String label, int textColor, int bgColor) {
        guiGraphics.fill(bx, by, bx + bw, by + bh, 0xFF141414);
        guiGraphics.fill(bx + 1, by + 1, bx + bw - 1, by + bh - 1, bgColor);
        guiGraphics.drawCenteredString(font, label, bx + bw / 2, by + (bh - 8) / 2, textColor);
    }

    private void renderCustomTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Visual bar tooltips
        int barX = x + 12;
        int barY = y + 42;
        int barW = 196;
        int barH = 14;

        ItemStack cell = menu.getSlot(0).getItem();
        if (!cell.isEmpty() && cell.getItem() instanceof VirtualMineCellItem virtualCell) {
            long totalBytes = virtualCell.getTier().getTotalBytes();
            if (mouseX >= barX && mouseX <= barX + barW && mouseY >= barY && mouseY <= barY + barH) {
                int relX = mouseX - barX;
                int currentX = 0;
                boolean hovered = false;
                for (int i = 0; i < workingList.size(); i++) {
                    PartitionDraft p = workingList.get(i);
                    int sliceW = (int) Math.round((p.percent / 100.0) * barW);
                    if (relX >= currentX && relX <= currentX + sliceW) {
                        long allocBytes = (totalBytes * p.percent) / 100L;
                        String targetName = (p.target != null) ? new ItemStack(p.target).getHoverName().getString() : "Unknown";
                        List<Component> tooltip = List.of(
                                Component.literal(targetName).withStyle(ChatFormatting.GOLD),
                                Component.literal("Allocation: " + p.percent + "% (" + String.format("%,d", allocBytes) + " Bytes)").withStyle(ChatFormatting.AQUA),
                                Component.literal(p.voidSecondary ? "◆ Void Secondary Outputs: Active" : "◇ Normal Secondary Outputs").withStyle(ChatFormatting.GRAY)
                        );
                        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                        hovered = true;
                        break;
                    }
                    currentX += sliceW;
                }
                if (!hovered && relX >= currentX) {
                    long freeBytes = (totalBytes * getUnallocatedPercent()) / 100L;
                    List<Component> tooltip = List.of(
                            Component.literal("Unallocated Space").withStyle(ChatFormatting.YELLOW),
                            Component.literal("Free: " + getUnallocatedPercent() + "% (" + String.format("%,d", freeBytes) + " Bytes)").withStyle(ChatFormatting.GRAY),
                            Component.literal("Saves AE power when idle").withStyle(ChatFormatting.DARK_GREEN)
                    );
                    guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                }
            }
        }

        // Action button tooltips
        int btnY = y + 137;
        if (mouseY >= btnY && mouseY <= btnY + 14) {
            if (mouseX >= x + 12 && mouseX <= x + 48) {
                guiGraphics.renderTooltip(font, Component.literal("Add a new partition to this cell"), mouseX, mouseY);
            } else if (mouseX >= x + 51 && mouseX <= x + 95) {
                guiGraphics.renderTooltip(font, Component.literal("Evenly distribute space across all active partitions"), mouseX, mouseY);
            } else if (mouseX >= x + 98 && mouseX <= x + 134) {
                guiGraphics.renderTooltip(font, Component.literal("Clear all partitions"), mouseX, mouseY);
            } else if (mouseX >= x + 138 && mouseX <= x + 208) {
                guiGraphics.renderTooltip(font, Component.literal("Apply partition layout to the storage cell"), mouseX, mouseY);
            }
        }

        // Table row tooltips
        int tableX = x + 12;
        int tableY = y + 58;
        int maxVisible = 3;
        for (int i = 0; i < maxVisible; i++) {
            int index = scrollOffset + i;
            if (index >= workingList.size()) break;
            PartitionDraft p = workingList.get(index);
            int rowY = tableY + 14 + i * 20;

            // Target box hover
            if (mouseX >= tableX + 10 && mouseX <= tableX + 27 && mouseY >= rowY && mouseY <= rowY + 17) {
                if (p.target != null) {
                    List<Component> tooltip = new ArrayList<>(getTooltipFromContainerItem(new ItemStack(p.target)));
                    tooltip.add(Component.literal("Drop an ore here or click to pick").withStyle(ChatFormatting.YELLOW));
                    guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                } else {
                    guiGraphics.renderTooltip(font, Component.literal("Drop an ore here or click to pick from inventory"), mouseX, mouseY);
                }
            }
            // Void button hover
            if (mouseX >= tableX + 138 && mouseX <= tableX + 164 && mouseY >= rowY + 3 && mouseY <= rowY + 14) {
                if (!hasVoidCard()) {
                    guiGraphics.renderTooltip(font, Component.literal("Requires Void Secondary Card").withStyle(ChatFormatting.RED), mouseX, mouseY);
                } else {
                    guiGraphics.renderTooltip(font, Component.literal("Toggle voiding byproduct ores (Cobble, Gravel, etc.)"), mouseX, mouseY);
                }
            }
        }

        // Empty Upgrade slot tooltips
        for (int i = 0; i < 4; i++) {
            int slotX = x + 51 + i * 18;
            int slotY = y + 154;
            if (mouseX >= slotX && mouseX <= slotX + 18 && mouseY >= slotY && mouseY <= slotY + 18) {
                if (!menu.getSlot(1 + i).hasItem()) {
                    guiGraphics.renderTooltip(font, Component.literal("Acceleration Card (" + (i + 1) + "/4)").withStyle(ChatFormatting.GRAY), mouseX, mouseY);
                }
            }
        }
        int voidSlotX = x + 137;
        int voidSlotY = y + 154;
        if (mouseX >= voidSlotX && mouseX <= voidSlotX + 18 && mouseY >= voidSlotY && mouseY <= voidSlotY + 18) {
            if (!menu.getSlot(5).hasItem()) {
                guiGraphics.renderTooltip(font, Component.literal("Void Secondary Card").withStyle(ChatFormatting.LIGHT_PURPLE), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = this.leftPos;
        int y = this.topPos;

        ItemStack cell = menu.getSlot(0).getItem();
        boolean hasCell = !cell.isEmpty() && cell.getItem() instanceof VirtualMineCellItem;

        // Action Buttons Row
        int btnY = y + 137;
        if (mouseY >= btnY && mouseY <= btnY + 14) {
            // [+ Add]
            if (mouseX >= x + 12 && mouseX <= x + 48 && hasCell && workingList.size() < 6 && getUnallocatedPercent() > 0) {
                playClickSound();
                Item target = findFirstUnusedInventoryTarget();
                int pct = Math.min(20, Math.max(5, getUnallocatedPercent()));
                workingList.add(new PartitionDraft(target, pct, false));
                dirty = true;
                return true;
            }
            // [Equalize]
            if (mouseX >= x + 51 && mouseX <= x + 95 && hasCell && !workingList.isEmpty()) {
                playClickSound();
                int count = workingList.size();
                int share = 100 / count;
                int rem = 100 % count;
                for (int i = 0; i < count; i++) {
                    workingList.get(i).percent = share + (i < rem ? 1 : 0);
                }
                dirty = true;
                return true;
            }
            // [Clear]
            if (mouseX >= x + 98 && mouseX <= x + 134 && hasCell && !workingList.isEmpty()) {
                playClickSound();
                workingList.clear();
                selectedRowForPicker = -1;
                dirty = true;
                return true;
            }
            // [Apply & Format]
            if (mouseX >= x + 138 && mouseX <= x + 208 && hasCell) {
                playClickSound();
                applyPartitionsToServer();
                return true;
            }
        }

        // Table Rows interaction
        int tableX = x + 12;
        int tableY = y + 58;
        int maxVisible = 3;
        for (int i = 0; i < maxVisible; i++) {
            int index = scrollOffset + i;
            if (index >= workingList.size()) break;
            PartitionDraft p = workingList.get(index);
            int rowY = tableY + 14 + i * 20;

            // Target box click — supports drag-and-drop (cursor item) and picker mode
            if (mouseX >= tableX + 10 && mouseX <= tableX + 27 && mouseY >= rowY && mouseY <= rowY + 17) {
                // Check if player is carrying an item on cursor (drag-and-drop)
                ItemStack carried = menu.getCarried();
                if (!carried.isEmpty()) {
                    Item carriedItem = carried.getItem();
                    if (MineDropRegistry.isValidMiningTarget(carriedItem, minecraft != null ? minecraft.level : null)) {
                        playClickSound();
                        p.target = carriedItem;
                        selectedRowForPicker = -1;
                        dirty = true;
                        return true;
                    }
                }
                // Otherwise toggle picker mode (click inventory slot to select)
                playClickSound();
                if (selectedRowForPicker == index) {
                    selectedRowForPicker = -1;
                } else {
                    selectedRowForPicker = index;
                }
                return true;
            }

            // [-] button
            if (mouseX >= tableX + 88 && mouseX <= tableX + 99 && mouseY >= rowY + 3 && mouseY <= rowY + 14) {
                playClickSound();
                if (p.percent > 5) {
                    p.percent -= 5;
                    dirty = true;
                }
                return true;
            }

            // [+] button
            if (mouseX >= tableX + 122 && mouseX <= tableX + 133 && mouseY >= rowY + 3 && mouseY <= rowY + 14) {
                playClickSound();
                int unalloc = getUnallocatedPercent();
                if (unalloc > 0) {
                    int add = Math.min(5, unalloc);
                    p.percent += add;
                    dirty = true;
                }
                return true;
            }

            // [Void] toggle
            if (mouseX >= tableX + 138 && mouseX <= tableX + 164 && mouseY >= rowY + 3 && mouseY <= rowY + 14) {
                if (hasVoidCard()) {
                    playClickSound();
                    p.voidSecondary = !p.voidSecondary;
                    dirty = true;
                }
                return true;
            }

            // [X] delete
            if (mouseX >= tableX + 170 && mouseX <= tableX + 181 && mouseY >= rowY + 3 && mouseY <= rowY + 14) {
                playClickSound();
                workingList.remove(index);
                if (selectedRowForPicker == index) selectedRowForPicker = -1;
                dirty = true;
                return true;
            }
        }

        // Scroll buttons
        if (workingList.size() > maxVisible) {
            if (mouseX >= tableX + 185 && mouseX <= tableX + 194) {
                if (mouseY >= tableY + 14 && mouseY <= tableY + 23 && scrollOffset > 0) {
                    playClickSound();
                    scrollOffset--;
                    return true;
                }
                if (mouseY >= tableY + 64 && mouseY <= tableY + 73 && scrollOffset + maxVisible < workingList.size()) {
                    playClickSound();
                    scrollOffset++;
                    return true;
                }
            }
        }

        // If in picker mode and player clicks an inventory slot:
        Slot slot = getSlotUnderMouse();
        if (slot != null && slot.hasItem() && selectedRowForPicker >= 0 && selectedRowForPicker < workingList.size()) {
            Item item = slot.getItem().getItem();
            if (MineDropRegistry.isValidMiningTarget(item, minecraft != null ? minecraft.level : null)) {
                playClickSound();
                workingList.get(selectedRowForPicker).target = item;
                selectedRowForPicker = -1;
                dirty = true;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0 && scrollOffset > 0) {
            scrollOffset--;
            return true;
        } else if (scrollY < 0 && scrollOffset + 3 < workingList.size()) {
            scrollOffset++;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private Item findFirstUnusedInventoryTarget() {
        if (minecraft != null && minecraft.player != null) {
            Inventory inv = minecraft.player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty()) {
                    Item item = stack.getItem();
                    if (MineDropRegistry.isValidMiningTarget(item, minecraft.level)) {
                        boolean used = false;
                        for (PartitionDraft p : workingList) {
                            if (p.target == item) {
                                used = true;
                                break;
                            }
                        }
                        if (!used) return item;
                    }
                }
            }
        }
        return null; // No default — partition starts as "undefined", user must select a target
    }

    private void applyPartitionsToServer() {
        List<MineCellPartition> partitions = new ArrayList<>();
        for (PartitionDraft draft : workingList) {
            if (draft.target != null && draft.percent > 0) {
                partitions.add(new MineCellPartition(draft.target, draft.percent, draft.voidSecondary));
            }
        }
        PacketDistributor.sendToServer(new SetPartitionsPayload(new MineCellPartitionList(partitions)));
        dirty = false;
    }

    private void playClickSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }
}
