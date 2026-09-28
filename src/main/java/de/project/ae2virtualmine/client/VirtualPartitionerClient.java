package de.project.ae2virtualmine.client;

import de.project.ae2virtualmine.client.gui.VirtualPartitionerScreen;
import de.project.ae2virtualmine.registry.ModMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class VirtualPartitionerClient {
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.PARTITIONER_MENU.get(), VirtualPartitionerScreen::new);
    }
}
