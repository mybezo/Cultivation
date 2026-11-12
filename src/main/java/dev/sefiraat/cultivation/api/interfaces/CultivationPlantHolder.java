package dev.sefiraat.cultivation.api.interfaces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;

/**
 * A grown plant is shown as a real {@link Material#WARPED_ROOTS} block instead of display entities.
 */
public interface CultivationPlantHolder {

    String PLANT = "plant_display_present";
    Material PLANT_MATERIAL = Material.WARPED_ROOTS;

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
        Block block = location.getBlock();
        if (block.getType() != PLANT_MATERIAL) {
            block.setType(PLANT_MATERIAL, false);
        }
        StorageCacheUtils.setData(location, PLANT, "true");
    }
}
