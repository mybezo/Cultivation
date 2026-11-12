package dev.sefiraat.cultivation.api.interfaces;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;

public interface CultivationCroppable {

    String CROPPED = "crops_present";
    String CROSS_CROPPED = "cross_crop_present";

    default boolean isCropped(@Nonnull Block block) {
        return isCropped(block.getLocation());
    }

    default boolean isCropped(@Nonnull Location location) {
        String cropped = StorageCacheUtils.getData(location, CROPPED);
        return Boolean.parseBoolean(cropped);
    }

    default boolean isCropped(@Nonnull SlimefunBlockData config) {
        String cropped = config.getData(CROPPED);
        return Boolean.parseBoolean(cropped);
    }

    default boolean isCrossCropped(@Nonnull Block block) {
        return isCrossCropped(block.getLocation());
    }

    default boolean isCrossCropped(@Nonnull Location location) {
        String cropped = StorageCacheUtils.getData(location, CROSS_CROPPED);
        return Boolean.parseBoolean(cropped);
    }

    default boolean isCrossCropped(@Nonnull SlimefunBlockData config) {
        String cropped = config.getData(CROSS_CROPPED);
        return Boolean.parseBoolean(cropped);
    }

    default boolean incrementCrop(@Nonnull Location location) {
        if (isCrossCropped(location)) {
            return false;
        } else if (isCropped(location)) {
            setCrossCropped(location);
            return true;
        } else {
            setCropped(location);
            return true;
        }
    }

    default void setCropped(@Nonnull Location location) {
        StorageCacheUtils.setData(location, CROPPED, "true");
    }

    default void setCrossCropped(@Nonnull Location location) {
        StorageCacheUtils.setData(location, CROPPED, "true");
        StorageCacheUtils.setData(location, CROSS_CROPPED, "true");
    }

    default void removeCropped(@Nonnull Location location) {
        StorageCacheUtils.removeData(location, CROPPED);
        StorageCacheUtils.removeData(location, CROSS_CROPPED);
    }
}
