package io.github.alexf6.puebloyparamo.entity;

import io.github.alexf6.puebloyparamo.PuebloYParamoMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Registro de entidades auxiliares de {@code puebloyparamo} (entidad asiento).
 */
public final class ModEntities {

    public static final EntityType<SeatEntity> SEAT = register(
            "seat",
            EntityType.Builder.of(SeatEntity::new, MobCategory.MISC)
                    .sized(0.001F, 0.001F)
                    .noSummon()
                    .clientTrackingRange(6)
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, PuebloYParamoMod.id(name));
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
    }

    private ModEntities() {
    }
}
