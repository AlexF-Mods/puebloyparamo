package io.github.alexf6.puebloyparamo.client;

import io.github.alexf6.puebloyparamo.PuebloYParamoMod;
import io.github.alexf6.puebloyparamo.menu.ClayPotMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Pantalla GUI de la Olla de barro: muestra las 4 ranuras de ingredientes,
 * el indicador de fuego inferior, el progreso de cocción y el resultado.
 */
public class ClayPotScreen extends AbstractContainerScreen<ClayPotMenu> {

    private static final Identifier TEXTURE = PuebloYParamoMod.id("textures/gui/olla_barro.png");

    public ClayPotScreen(ClayPotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        // Llama encendida si hay fogata, fuego, magma o estufa encendida debajo
        if (this.menu.isHeated()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 88, y + 51, 176.0F, 0.0F, 14, 14, 256, 256);
        }

        // Barra de progreso de cocción
        int progress = this.menu.getCookProgress();
        int total = this.menu.getCookTotal();
        if (progress > 0) {
            int arrowWidth = Math.max(1, (progress * 24) / total);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 84, y + 34, 176.0F, 14.0F, arrowWidth, 16, 256, 256);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        Component status = this.menu.isHeated()
                ? Component.translatable("container.puebloyparamo.olla_barro.heated")
                : Component.translatable("container.puebloyparamo.olla_barro.needs_heat");
        int color = this.menu.isHeated() ? 0xFF3E7A2B : 0xFF9C352B;
        graphics.text(this.font, status, x + 8, y + 70, color, false);
    }
}
