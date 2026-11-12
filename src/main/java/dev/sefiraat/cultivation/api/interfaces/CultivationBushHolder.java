package dev.sefiraat.cultivation.api.interfaces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.cultivation.implementation.utils.FloraBlocks;
import org.bukkit.Location;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;

/**
 * A bush is shown as a real azalea block instead of display entities, see {@link FloraBlocks}.
 */
public interface CultivationBushHolder {

    String BUSH = "bush_display_present";

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
        setDisplayBush(location, false);
    }

    /**
     * @param mature True shows the bush as ready to harvest (flowering)
     */
    default void setDisplayBush(@Nonnull Location location, boolean mature) {
        FloraBlocks.show(location, mature);
        StorageCacheUtils.setData(location, BUSH, "true");
    }
}
