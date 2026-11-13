package io.github.alexf6.puebloyparamo.block.entity;

import io.github.alexf6.puebloyparamo.PuebloYParamoMod;
import io.github.alexf6.puebloyparamo.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Registro de {@link BlockEntityType} de {@code puebloyparamo} (Olla de barro y Picó).
 */
public final class ModBlockEntities {

    public static final BlockEntityType<ClayPotBlockEntity> OLLA_BARRO = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            PuebloYParamoMod.id("olla_barro"),
            FabricBlockEntityTypeBuilder.create(ClayPotBlockEntity::new, ModBlocks.OLLA_BARRO).build()
    );

    public static final BlockEntityType<PicoBlockEntity> PICO = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            PuebloYParamoMod.id("pico"),
            FabricBlockEntityTypeBuilder.create(PicoBlockEntity::new, ModBlocks.PICO).build()
    );

    public static void init() {
    }

    private ModBlockEntities() {
    }
}
