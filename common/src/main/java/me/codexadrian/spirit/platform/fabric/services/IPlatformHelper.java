package me.codexadrian.spirit.platform.fabric.services;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    Path getConfigDir();

    /** Opens the Soul Cage upgrade menu for the given cage position, syncing the position to the client. */
    void openSoulCageMenu(ServerPlayer player, BlockPos pos);
}
