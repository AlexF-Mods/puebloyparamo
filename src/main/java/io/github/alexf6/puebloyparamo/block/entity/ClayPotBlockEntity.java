package io.github.alexf6.puebloyparamo.block.entity;

import io.github.alexf6.puebloyparamo.PuebloYParamoConfig;
import io.github.alexf6.puebloyparamo.block.ClayPotBlock;
import io.github.alexf6.puebloyparamo.logic.OllaLogic;
import io.github.alexf6.puebloyparamo.menu.ClayPotMenu;
import io.github.alexf6.puebloyparamo.recipe.OllaRecipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Entidad de bloque de la Olla de barro: gestiona 4 ranuras de ingredientes,
 * 1 ranura de salida, detección de calor debajo y cocción de recetas especiales.
 */
public class ClayPotBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_SLOT = 4;
    public static final int TOTAL_SLOTS = 5;

    private static final int[] INPUT_INDICES = {0, 1, 2, 3};
    private static final int[] OUTPUT_INDICES = {OUTPUT_SLOT};

    private final NonNullList<ItemStack> items = NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
    private int cookProgress;
    private int cookTotal = PuebloYParamoConfig.defaults().ollaCookTimeTicks();
    private int heatedFlag;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookProgress;
                case 1 -> cookTotal;
                case 2 -> heatedFlag;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cookProgress = value;
                case 1 -> cookTotal = value;
                case 2 -> heatedFlag = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public ClayPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.OLLA_BARRO, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ClayPotBlockEntity pot) {
        boolean heated = isHeatSourceBelow(level.getBlockState(pos.below()));
        pot.heatedFlag = heated ? 1 : 0;
        pot.cookTotal = PuebloYParamoConfig.get().ollaCookTimeTicks();

        Optional<OllaLogic.RecipeSpec> match = heated ? pot.findRecipe() : Optional.empty();
        boolean cooking = match.isPresent();

        if (state.getValue(ClayPotBlock.LIT) != (heated && cooking)) {
            level.setBlock(pos, state.setValue(ClayPotBlock.LIT, heated && cooking), Block.UPDATE_ALL);
        }

        if (match.isPresent()) {
            pot.cookProgress++;
            if (pot.cookProgress >= pot.cookTotal) {
                pot.cookProgress = 0;
                pot.finishCooking(level, pos, match.get());
            }
            pot.setChanged();
        } else if (pot.cookProgress > 0) {
            pot.cookProgress = Math.max(0, pot.cookProgress - 2);
            pot.setChanged();
        }
    }

    private Optional<OllaLogic.RecipeSpec> findRecipe() {
        List<String> inputIds = new ArrayList<>(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack stack = items.get(i);
            inputIds.add(stack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        }
        ItemStack outStack = items.get(OUTPUT_SLOT);
        OllaLogic.OutputSlotState outputState = outStack.isEmpty()
                ? OllaLogic.OutputSlotState.empty()
                : new OllaLogic.OutputSlotState(
                        BuiltInRegistries.ITEM.getKey(outStack.getItem()).toString(),
                        outStack.getCount(),
                        outStack.getMaxStackSize()
                );
        return OllaLogic.findMatchingRecipe(inputIds, OllaRecipeRegistry.getRecipes(), outputState);
    }

    private void finishCooking(Level level, BlockPos pos, OllaLogic.RecipeSpec recipe) {
        Item resultItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(recipe.resultItemId()));
        ItemStack currentOut = items.get(OUTPUT_SLOT);
        if (currentOut.isEmpty()) {
            items.set(OUTPUT_SLOT, new ItemStack(resultItem, recipe.resultCount()));
        } else {
            currentOut.grow(recipe.resultCount());
        }

        for (int i = 0; i < INPUT_SLOTS; i++) {
            ItemStack in = items.get(i);
            if (in.isEmpty()) {
                continue;
            }
            String consumedId = BuiltInRegistries.ITEM.getKey(in.getItem()).toString();
            in.shrink(1);
            Optional<String> containerId = OllaLogic.leftoverContainerFor(recipe, consumedId);
            if (containerId.isPresent()) {
                Item containerItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(containerId.get()));
                ItemStack leftover = new ItemStack(containerItem);
                if (in.isEmpty()) {
                    items.set(i, leftover);
                } else {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, leftover);
                }
            }
        }
        level.playSound(null, pos, SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 0.5F, 1.35F);
    }

    public static boolean isHeatSourceBelow(BlockState below) {
        if (below.is(BlockTags.CAMPFIRES) || below.is(BlockTags.FIRE)) {
            if (below.hasProperty(BlockStateProperties.LIT)) {
                return below.getValue(BlockStateProperties.LIT);
            }
            return true;
        }
        if (below.is(Blocks.MAGMA_BLOCK) || below.is(Blocks.LAVA)) {
            return true;
        }
        return below.hasProperty(BlockStateProperties.LIT)
                && (below.is(Blocks.FURNACE) || below.is(Blocks.SMOKER) || below.is(Blocks.BLAST_FURNACE))
                && below.getValue(BlockStateProperties.LIT);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.items);
        this.cookProgress = input.getIntOr("CookProgress", 0);
        this.cookTotal = input.getIntOr("CookTotal", PuebloYParamoConfig.get().ollaCookTimeTicks());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("CookProgress", this.cookProgress);
        output.putInt("CookTotal", this.cookTotal);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.puebloyparamo.olla_barro");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ClayPotMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return side == Direction.DOWN ? OUTPUT_INDICES : INPUT_INDICES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot < INPUT_SLOTS && dir != Direction.DOWN;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == OUTPUT_SLOT;
    }

    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}
