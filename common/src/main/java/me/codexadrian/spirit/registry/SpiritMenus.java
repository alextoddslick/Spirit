package me.codexadrian.spirit.registry;

import me.codexadrian.spirit.menu.SoulCageMenu;
import me.codexadrian.spirit.platform.fabric.Services;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class SpiritMenus {

    public static Supplier<MenuType<SoulCageMenu>> SOUL_CAGE_MENU;

    public static void registerAll() {
        SOUL_CAGE_MENU = Services.REGISTRY.registerMenu("soul_cage", SoulCageMenu::new);
    }
}
