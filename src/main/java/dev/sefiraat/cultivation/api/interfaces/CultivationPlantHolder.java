package dev.sefiraat.cultivation.api.interfaces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.cultivation.implementation.utils.FloraBlocks;
import org.bukkit.Location;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;

/**
 * A grown plant is shown as a real azalea block instead of display entities, see {@link FloraBlocks}.
 */
public interface CultivationPlantHolder {

    String PLANT = "plant_display_present";

    default boolean hasDisplayPlant(@Nonnull Block block) {
        return hasDisplayPlant(block.getLocation());
    }

    default boolean hasDisplayPlant(@Nonnull Location location) {
        String hasPlant = StorageCacheUtils.getData(location, PLANT);
        return Boolean.parseBoolean(hasPlant);
    }

    default boolean hasDisplayPlant(@Nonnull SlimefunBlockData config) {
        String hasPlant = config.getData(PLANT);
        return Boolean.parseBoolean(hasPlant);
    }

    default void removePlant(@Nonnull Location location) {
        StorageCacheUtils.removeData(location, PLANT);
    }

    default void addDisplayPlant(@Nonnull Location location) {
        setDisplayPlant(location, false);
    }

    /**
     * @param mature True shows the plant as ready to harvest (flowering)
     */
    default void setDisplayPlant(@Nonnull Location location, boolean mature) {
        FloraBlocks.show(location, mature);
        StorageCacheUtils.setData(location, PLANT, "true");
    }
}
