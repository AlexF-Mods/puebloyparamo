package io.github.alexf6.puebloyparamo.menu;

import io.github.alexf6.puebloyparamo.PuebloYParamoMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/**
 * Registro de {@link MenuType} de {@code puebloyparamo}.
 */
public final class ModMenus {

    public static final MenuType<ClayPotMenu> OLLA_BARRO = Registry.register(
            BuiltInRegistries.MENU,
            PuebloYParamoMod.id("olla_barro"),
            new MenuType<>(ClayPotMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static void init() {
    }

    private ModMenus() {
    }
}
