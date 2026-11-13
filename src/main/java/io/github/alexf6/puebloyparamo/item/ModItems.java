package io.github.alexf6.puebloyparamo.item;

import io.github.alexf6.puebloyparamo.PuebloYParamoMod;
import io.github.alexf6.puebloyparamo.block.ModBlocks;
import io.github.alexf6.puebloyparamo.sound.ModSounds;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Registro de ítems de bloques, fibra e infusiones del frailejón,
 * platos especiales de la Olla de barro y discos de música personalizada.
 */
public final class ModItems {

    private static final List<Item> ALL_ITEMS = new ArrayList<>();
    private static final List<Item> BUILDING_ITEMS = new ArrayList<>();
    private static final List<Item> FUNCTIONAL_ITEMS = new ArrayList<>();
    private static final List<Item> FOOD_ITEMS = new ArrayList<>();
    private static final List<Item> DISC_ITEMS = new ArrayList<>();

    private static final ResourceKey<CreativeModeTab> PUEBLO_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            PuebloYParamoMod.id("puebloyparamo")
    );

    // BlockItems: Páramo y materiales
    public static final Item FRAILEJON = registerBlockItem(ModBlocks.FRAILEJON, BUILDING_ITEMS);
    public static final Item FRAILEJON_FIBER_BLOCK = registerBlockItem(ModBlocks.FRAILEJON_FIBER_BLOCK, BUILDING_ITEMS);

    // BlockItems: Arquitectura colonial y pueblo
    public static final Item TEJA_BARRO = registerBlockItem(ModBlocks.TEJA_BARRO, BUILDING_ITEMS);
    public static final Item TEJA_BARRO_STAIRS = registerBlockItem(ModBlocks.TEJA_BARRO_STAIRS, BUILDING_ITEMS);
    public static final Item TEJA_BARRO_SLAB = registerBlockItem(ModBlocks.TEJA_BARRO_SLAB, BUILDING_ITEMS);
    public static final Item BAHAREQUE = registerBlockItem(ModBlocks.BAHAREQUE, BUILDING_ITEMS);
    public static final Item BAHAREQUE_ENCALADO = registerBlockItem(ModBlocks.BAHAREQUE_ENCALADO, BUILDING_ITEMS);
    public static final Item ZOCALO_COLONIAL = registerBlockItem(ModBlocks.ZOCALO_COLONIAL, BUILDING_ITEMS);
    public static final Item ADOQUIN_COLONIAL = registerBlockItem(ModBlocks.ADOQUIN_COLONIAL, BUILDING_ITEMS);
    public static final Item ADOQUIN_COLONIAL_STAIRS = registerBlockItem(ModBlocks.ADOQUIN_COLONIAL_STAIRS, BUILDING_ITEMS);
    public static final Item ADOQUIN_COLONIAL_SLAB = registerBlockItem(ModBlocks.ADOQUIN_COLONIAL_SLAB, BUILDING_ITEMS);
    public static final Item ADOQUIN_COLONIAL_WALL = registerBlockItem(ModBlocks.ADOQUIN_COLONIAL_WALL, BUILDING_ITEMS);
    public static final Item BALCON_AZUL = registerBlockItem(ModBlocks.BALCON_AZUL, BUILDING_ITEMS);
    public static final Item BALCON_ROJO = registerBlockItem(ModBlocks.BALCON_ROJO, BUILDING_ITEMS);
    public static final Item BALCON_AMARILLO = registerBlockItem(ModBlocks.BALCON_AMARILLO, BUILDING_ITEMS);
    public static final Item PUERTA_TALLADA = registerBlockItem(ModBlocks.PUERTA_TALLADA, BUILDING_ITEMS);
    public static final Item FAROL_COLONIAL = registerBlockItem(ModBlocks.FAROL_COLONIAL, FUNCTIONAL_ITEMS);

    // BlockItems: Mobiliario típico
    public static final Item SILLA_RIMAX_BLANCA = registerBlockItem(ModBlocks.SILLA_RIMAX_BLANCA, FUNCTIONAL_ITEMS);
    public static final Item SILLA_RIMAX_ROJA = registerBlockItem(ModBlocks.SILLA_RIMAX_ROJA, FUNCTIONAL_ITEMS);
    public static final Item SILLA_RIMAX_AZUL = registerBlockItem(ModBlocks.SILLA_RIMAX_AZUL, FUNCTIONAL_ITEMS);
    public static final Item HAMACA = registerBlockItem(ModBlocks.HAMACA, FUNCTIONAL_ITEMS);
    public static final Item OLLA_BARRO = registerBlockItem(ModBlocks.OLLA_BARRO, FUNCTIONAL_ITEMS);
    public static final Item PICO = registerBlockItem(ModBlocks.PICO, FUNCTIONAL_ITEMS);

    // Recursos y consumibles
    public static final Item FRAILEJON_FIBER = registerSimple("frailejon_fiber");

    public static final Item AGUA_PURA = registerConsumable(
            "agua_pura", 2, 0.4F, true, Items.GLASS_BOTTLE,
            entity -> {
                entity.removeEffect(MobEffects.POISON);
                entity.removeEffect(MobEffects.WEAKNESS);
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0));
            }
    );

    public static final Item AGUAPANELA_CALIENTE = registerConsumable(
            "aguapanela_caliente", 5, 0.65F, true, Items.GLASS_BOTTLE,
            entity -> {
                entity.setTicksFrozen(0);
                entity.addEffect(new MobEffectInstance(MobEffects.SPEED, 1200, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.HASTE, 1800, 0));
            }
    );

    public static final Item INFUSION_FRAILEJON = registerConsumable(
            "infusion_frailejon", 4, 0.55F, true, Items.GLASS_BOTTLE,
            entity -> {
                entity.setTicksFrozen(0);
                entity.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1200, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 900, 0));
            }
    );

    public static final Item CALDO_COSTILLA = registerConsumable(
            "caldo_costilla", 8, 0.85F, false, Items.BOWL,
            entity -> {
                entity.removeEffect(MobEffects.NAUSEA);
                entity.removeEffect(MobEffects.MINING_FATIGUE);
                entity.heal(4.0F);
                entity.addEffect(new MobEffectInstance(MobEffects.SPEED, 900, 0));
            }
    );

    public static final Item AJIACO = registerConsumable(
            "ajiaco", 10, 0.95F, false, Items.BOWL,
            entity -> entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1))
    );

    public static final Item SANCOCHO = registerConsumable(
            "sancocho", 11, 1.0F, false, Items.BOWL,
            entity -> {
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 900, 0));
            }
    );

    // Discos musicales para el Picó y Tocadiscos vanilla
    public static final Item DISCO_CUMBIA = registerMusicDisc("disco_cumbia", ModSounds.SONG_CUMBIA);
    public static final Item DISCO_VALLENATO = registerMusicDisc("disco_vallenato", ModSounds.SONG_VALLENATO);
    public static final Item DISCO_SALSA = registerMusicDisc("disco_salsa", ModSounds.SONG_SALSA);
    public static final Item DISCO_RAP_LOCAL = registerMusicDisc("disco_rap_local", ModSounds.SONG_RAP_LOCAL);

    private ModItems() {
    }

    private static Item registerBlockItem(Block block, List<Item> categoryList) {
        ResourceKey<Block> blockKey = BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow();
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, blockKey.identifier());
        Item item = Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix())
        );
        ALL_ITEMS.add(item);
        categoryList.add(item);
        return item;
    }

    private static Item registerSimple(String name) {
        Item item = register(name, p -> new PuebloConsumableItem(p, "item.puebloyparamo." + name + ".desc", null));
        ALL_ITEMS.add(item);
        return item;
    }

    private static Item registerConsumable(
            String name,
            int nutrition,
            float saturation,
            boolean drink,
            Item remainder,
            Consumer<LivingEntity> effect
    ) {
        Item item = register(name, props -> {
            FoodProperties food = new FoodProperties.Builder()
                    .nutrition(nutrition)
                    .saturationModifier(saturation)
                    .alwaysEdible()
                    .build();
            Consumable consumable = Consumable.builder()
                    .consumeSeconds(drink ? 1.2F : 1.5F)
                    .animation(drink ? ItemUseAnimation.DRINK : ItemUseAnimation.EAT)
                    .sound(drink ? SoundEvents.GENERIC_DRINK : SoundEvents.GENERIC_EAT)
                    .hasConsumeParticles(!drink)
                    .build();
            Item.Properties configured = props.food(food, consumable).stacksTo(16);
            if (remainder != null) {
                configured = configured.usingConvertsTo(remainder);
            }
            return new PuebloConsumableItem(configured, "item.puebloyparamo." + name + ".desc", effect);
        });
        ALL_ITEMS.add(item);
        FOOD_ITEMS.add(item);
        return item;
    }

    private static Item registerMusicDisc(String name, ResourceKey<JukeboxSong> songKey) {
        Item item = register(
                name,
                props -> new Item(props.stacksTo(1).rarity(Rarity.RARE).jukeboxPlayable(songKey))
        );
        ALL_ITEMS.add(item);
        DISC_ITEMS.add(item);
        return item;
    }

    private static Item register(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, PuebloYParamoMod.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
    }

    public static void init() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                PUEBLO_TAB_KEY,
                FabricCreativeModeTab.builder()
                        .icon(() -> new ItemStack(OLLA_BARRO))
                        .title(Component.translatable("itemGroup.puebloyparamo.puebloyparamo"))
                        .displayItems((params, output) -> ALL_ITEMS.forEach(output::accept))
                        .build()
        );

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(tab -> BUILDING_ITEMS.forEach(tab::accept));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(tab -> FUNCTIONAL_ITEMS.forEach(tab::accept));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(tab -> FOOD_ITEMS.forEach(tab::accept));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(tab -> tab.accept(FRAILEJON_FIBER));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(tab -> DISC_ITEMS.forEach(tab::accept));
    }
}
