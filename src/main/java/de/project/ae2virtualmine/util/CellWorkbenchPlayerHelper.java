package de.project.ae2virtualmine.util;

import appeng.menu.implementations.CellWorkbenchMenu;
import appeng.util.Platform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public class CellWorkbenchPlayerHelper {

    public static @Nullable Player getCurrentPlayer(ItemStack cellStack) {
        if (Platform.isClient()) {
            return ClientPlayerGetter.getClientPlayer();
        } else {
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player.containerMenu instanceof CellWorkbenchMenu menu) {
                        ItemStack workbenchCell = menu.getWorkbenchItem();
                        if (!workbenchCell.isEmpty() && (workbenchCell == cellStack || ItemStack.isSameItem(workbenchCell, cellStack))) {
                            return player;
                        }
                    }
                }
            }
            return null;
        }
    }

    public static boolean hasItemInInventory(@Nullable Player player, Item item) {
        if (player == null) {
            return true;
        }
        if (player.isCreative()) {
            return true;
        }
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (!invStack.isEmpty() && invStack.is(item)) {
                return true;
            }
        }
        return false;
    }

    private static class ClientPlayerGetter {
        public static Player getClientPlayer() {
            return net.minecraft.client.Minecraft.getInstance().player;
        }
    }
}
