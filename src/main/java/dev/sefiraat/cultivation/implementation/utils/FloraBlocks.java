package dev.sefiraat.cultivation.implementation.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import javax.annotation.Nonnull;

/**
 * Grown plants and bushes are shown as real azalea blocks: a plain {@link Material#AZALEA} while growing,
 * and a {@link Material#FLOWERING_AZALEA} once ready to harvest.
 * Bone meal is blocked on them (see the placement listener), so they never grow into trees.
 */
public final class FloraBlocks {

    public static final Material GROWING = Material.AZALEA;
    public static final Material MATURE = Material.FLOWERING_AZALEA;

    private FloraBlocks() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isFloraMaterial(@Nonnull Material material) {
        return material == GROWING || material == MATURE;
    }

    /**
     * Shows the flora at this location, without triggering physics on the new block.
     */
    public static void show(@Nonnull Location location, boolean mature) {
        Block block = location.getBlock();
        Material material = mature ? MATURE : GROWING;
        if (block.getType() != material) {
            block.setType(material, false);
        }
    }
}
