package io.github.alexf6.puebloyparamo.block;

import io.github.alexf6.puebloyparamo.PuebloYParamoMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;

/**
 * Registro centralizado de bloques de páramo, arquitectura colonial/pueblo
 * y mobiliario típico de {@code puebloyparamo}.
 */
public final class ModBlocks {

    // Páramo y fibra de frailejón
    public static final Block FRAILEJON = register(
            "frailejon",
            FrailejonBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT)
                    .noCollision()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.MOSS)
                    .pushReaction(PushReaction.DESTROY)
                    .offsetType(BlockBehaviour.OffsetType.XZ)
    );

    public static final Block FRAILEJON_FIBER_BLOCK = register(
            "frailejon_fiber_block",
            FrailejonFiberBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .strength(0.5F)
                    .sound(SoundType.WOOL)
    );

    // Arquitectura colonial y de pueblo
    public static final Block TEJA_BARRO = register(
            "teja_barro",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_ORANGE)
                    .requiresCorrectToolForDrops()
                    .strength(1.8F, 5.0F)
                    .sound(SoundType.DEEPSLATE_TILES)
    );

    public static final Block TEJA_BARRO_STAIRS = register(
            "teja_barro_stairs",
            props -> new StairBlock(TEJA_BARRO.defaultBlockState(), props),
            BlockBehaviour.Properties.ofFullCopy(TEJA_BARRO)
    );

    public static final Block TEJA_BARRO_SLAB = register(
            "teja_barro_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(TEJA_BARRO)
    );

    public static final Block BAHAREQUE = register(
            "bahareque",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .strength(1.1F, 2.5F)
                    .sound(SoundType.PACKED_MUD)
    );

    public static final Block BAHAREQUE_ENCALADO = register(
            "bahareque_encalado",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.QUARTZ)
                    .strength(1.2F, 3.0F)
                    .sound(SoundType.CALCITE)
    );

    public static final Block ZOCALO_COLONIAL = register(
            "zocalo_colonial",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BLUE)
                    .strength(1.3F, 3.5F)
                    .sound(SoundType.STONE)
    );

    public static final Block ADOQUIN_COLONIAL = register(
            "adoquin_colonial",
            Block::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .requiresCorrectToolForDrops()
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.STONE)
    );

    public static final Block ADOQUIN_COLONIAL_STAIRS = register(
            "adoquin_colonial_stairs",
            props -> new StairBlock(ADOQUIN_COLONIAL.defaultBlockState(), props),
            BlockBehaviour.Properties.ofFullCopy(ADOQUIN_COLONIAL)
    );

    public static final Block ADOQUIN_COLONIAL_SLAB = register(
            "adoquin_colonial_slab",
            SlabBlock::new,
            BlockBehaviour.Properties.ofFullCopy(ADOQUIN_COLONIAL)
    );

    public static final Block ADOQUIN_COLONIAL_WALL = register(
            "adoquin_colonial_wall",
            WallBlock::new,
            BlockBehaviour.Properties.ofFullCopy(ADOQUIN_COLONIAL)
    );

    public static final Block BALCON_AZUL = register(
            "balcon_azul",
            FenceBlock::new,
            balconyProperties(MapColor.COLOR_BLUE)
    );

    public static final Block BALCON_ROJO = register(
            "balcon_rojo",
            FenceBlock::new,
            balconyProperties(MapColor.COLOR_RED)
    );

    public static final Block BALCON_AMARILLO = register(
            "balcon_amarillo",
            FenceBlock::new,
            balconyProperties(MapColor.COLOR_YELLOW)
    );

    public static final Block PUERTA_TALLADA = register(
            "puerta_tallada",
            props -> new DoorBlock(BlockSetType.DARK_OAK, props),
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(2.5F)
                    .noOcclusion()
                    .sound(SoundType.WOOD)
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final Block FAROL_COLONIAL = register(
            "farol_colonial",
            LanternBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F)
                    .lightLevel(state -> 15)
                    .noOcclusion()
                    .sound(SoundType.LANTERN)
                    .pushReaction(PushReaction.DESTROY)
    );

    // Mobiliario típico
    public static final Block SILLA_RIMAX_BLANCA = register(
            "silla_rimax_blanca",
            RimaxChairBlock::new,
            rimaxProperties(MapColor.SNOW)
    );

    public static final Block SILLA_RIMAX_ROJA = register(
            "silla_rimax_roja",
            RimaxChairBlock::new,
            rimaxProperties(MapColor.COLOR_RED)
    );

    public static final Block SILLA_RIMAX_AZUL = register(
            "silla_rimax_azul",
            RimaxChairBlock::new,
            rimaxProperties(MapColor.COLOR_BLUE)
    );

    public static final Block HAMACA = register(
            "hamaca",
            HammockBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .strength(0.6F)
                    .noOcclusion()
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.DESTROY)
    );

    public static final Block OLLA_BARRO = register(
            "olla_barro",
            ClayPotBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_RED)
                    .strength(1.5F, 4.0F)
                    .noOcclusion()
                    .sound(SoundType.DECORATED_POT)
    );

    public static final Block PICO = register(
            "pico",
            PicoBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.WOOD)
    );

    private static BlockBehaviour.Properties balconyProperties(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(1.6F, 2.5F)
                .sound(SoundType.WOOD)
                .ignitedByLava();
    }

    private static BlockBehaviour.Properties rimaxProperties(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(1.0F, 3_600_000.0F)
                .noOcclusion()
                .sound(SoundType.BAMBOO_WOOD);
    }

    private static Block register(
            String name,
            Function<BlockBehaviour.Properties, Block> factory,
            BlockBehaviour.Properties properties
    ) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, PuebloYParamoMod.id(name));
        Block block = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    public static void init() {
        // Dispara inicialización estática de bloques.
        Blocks.AIR.defaultBlockState();
    }

    private ModBlocks() {
    }
}
