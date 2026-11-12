package dev.sefiraat.cultivation.api.interfaces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;

/**
 * A bush is shown as a real {@link Material#WARPED_ROOTS} block instead of display entities.
 */
public interface CultivationBushHolder {

    String BUSH = "bush_display_present";
    Material BUSH_MATERIAL = Material.WARPED_ROOTS;

    default boolean hasDisplayBush(@Nonnull Block block) {
        return hasDisplayBush(block.getLocation());
    }

    default boolean hasDisplayBush(@Nonnull Location location) {
        String hasBush = StorageCacheUtils.getData(location, BUSH);
        return Boolean.parseBoolean(hasBush);
    }

    default boolean hasDisplayBush(@Nonnull SlimefunBlockData config) {
        String hasBush = config.getData(BUSH);
        return Boolean.parseBoolean(hasBush);
    }

    default void removeBush(@Nonnull Location location) {
        StorageCacheUtils.removeData(location, BUSH);
    }

    default void addDisplayBush(@Nonnull Location location) {
        Block block = location.getBlock();
        if (block.getType() != BUSH_MATERIAL) {
            block.setType(BUSH_MATERIAL, false);
        }
        StorageCacheUtils.setData(location, BUSH, "true");
    }
}
