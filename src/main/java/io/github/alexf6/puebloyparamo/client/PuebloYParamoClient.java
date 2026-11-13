package io.github.alexf6.puebloyparamo.client;

import io.github.alexf6.puebloyparamo.entity.ModEntities;
import io.github.alexf6.puebloyparamo.menu.ModMenus;
import io.github.alexf6.puebloyparamo.network.PicoSoundPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * Inicializador de cliente de {@code puebloyparamo}:
 * registra pantallas de menú, renderizador invisible de asientos y receptor de audio del Picó.
 */
public class PuebloYParamoClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenus.OLLA_BARRO, ClayPotScreen::new);
        EntityRendererRegistry.register(ModEntities.SEAT, SeatRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(PicoSoundPayload.TYPE, PicoSoundManager::handlePayload);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(PicoSoundManager::stopAll));
    }
}
