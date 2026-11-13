package io.github.alexf6.puebloyparamo.menu;

import io.github.alexf6.puebloyparamo.block.entity.ClayPotBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Contenedor lógico de la Olla de barro: 4 ranuras de ingredientes sin orden fijo,
 * 1 ranura de salida y sincronización de progreso/calor mediante {@link ContainerData}.
 */
public class ClayPotMenu extends AbstractContainerMenu {

    private final Container potInventory;
    private final ContainerData data;

    public ClayPotMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(ClayPotBlockEntity.TOTAL_SLOTS), new SimpleContainerData(3));
    }

    public ClayPotMenu(int containerId, Inventory playerInventory, Container potInventory, ContainerData data) {
        super(ModMenus.OLLA_BARRO, containerId);
        checkContainerSize(potInventory, ClayPotBlockEntity.TOTAL_SLOTS);
        checkContainerDataCount(data, 3);
        this.potInventory = potInventory;
        this.data = data;
        potInventory.startOpen(playerInventory.player);

        // 4 ranuras de ingredientes en cuadrícula 2x2
        addSlot(new Slot(potInventory, 0, 42, 26));
        addSlot(new Slot(potInventory, 1, 60, 26));
        addSlot(new Slot(potInventory, 2, 42, 44));
        addSlot(new Slot(potInventory, 3, 60, 44));

        // Ranura de resultado (solo extracción)
        addSlot(new Slot(potInventory, ClayPotBlockEntity.OUTPUT_SLOT, 120, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Inventario del jugador
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Barra rápida
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    public int getCookProgress() {
        return data.get(0);
    }

    public int getCookTotal() {
        return Math.max(1, data.get(1));
    }

    public boolean isHeated() {
        return data.get(2) != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return potInventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        potInventory.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack current = slot.getItem();
            copy = current.copy();

            if (index == ClayPotBlockEntity.OUTPUT_SLOT) {
                if (!moveItemStackTo(current, ClayPotBlockEntity.TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(current, copy);
            } else if (index >= ClayPotBlockEntity.TOTAL_SLOTS) {
                if (!moveItemStackTo(current, 0, ClayPotBlockEntity.INPUT_SLOTS, false)) {
                    int invEnd = ClayPotBlockEntity.TOTAL_SLOTS + 27;
                    if (index < invEnd) {
                        if (!moveItemStackTo(current, invEnd, this.slots.size(), false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!moveItemStackTo(current, ClayPotBlockEntity.TOTAL_SLOTS, invEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(current, ClayPotBlockEntity.TOTAL_SLOTS, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (current.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (current.getCount() == copy.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, current);
        }
        return copy;
    }
}
