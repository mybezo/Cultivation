package dev.sefiraat.cultivation.implementation.listeners;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.cultivation.api.interfaces.CustomPlacementBlock;
import dev.sefiraat.cultivation.api.slimefun.items.bushes.CultivationBush;
import dev.sefiraat.cultivation.api.slimefun.items.plants.CultivationPlant;
import dev.sefiraat.cultivation.implementation.utils.FloraBlocks;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The purpose of this listener is to allow us to cancel the block placement if not on the
 * correct material. If done within the onBlockPlace handler, the BlockStorage is retained
 * leading to dupes.
 * Also allows other objects implementing {@link CustomPlacementBlock}
 * to fire their own methods
 * TODO PR to slimefun to either do blockstorage after checking the event is cancelled or to remove
 */
public class CustomPlacementListener implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(@Nonnull BlockPlaceEvent event) {
        SlimefunItem slimefunItem = SlimefunItem.getByItem(event.getItemInHand());
        if (slimefunItem instanceof CustomPlacementBlock block) {
            block.whenPlaced(event);
        }
    }

    @EventHandler
    public void onWaterHitsPlant(@Nonnull BlockFromToEvent event) {
        Location location = event.getBlock().getLocation();
        unsafelyKillItem(location, StorageCacheUtils.getSfItem(location));
    }

    @EventHandler
    public void onPistonExtends(@Nonnull BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            Block issueBlock = block.getRelative(BlockFace.UP);
            Location location = issueBlock.getLocation();
            unsafelyKillItem(location, StorageCacheUtils.getSfItem(location));
        }
    }
    @EventHandler
    public void onPistonRetracts(@Nonnull BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            Block issueBlock = block.getRelative(BlockFace.UP);
            Location location = issueBlock.getLocation();
            unsafelyKillItem(location, StorageCacheUtils.getSfItem(location));
        }
    }

    @EventHandler
    public void onBlockSpread(@Nonnull BlockSpreadEvent event) {
        Location location = event.getBlock().getLocation();
        unsafelyKillItem(location, StorageCacheUtils.getSfItem(location));
    }
    
    /**
     * Grown plants and bushes are azaleas, which bone meal would grow into a tree. Block that,
     * from players and dispensers alike.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFloraFertilize(@Nonnull BlockFertilizeEvent event) {
        Block block = event.getBlock();
        if (isFloraBlock(block)) {
            event.setCancelled(true);
        }
    }

    /**
     * Stops bone meal from being used up on a plant or bush. Only the item use is denied, so
     * right-clicking still harvests through Slimefun.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBoneMealFlora(@Nonnull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
            || event.getItem() == null
            || event.getItem().getType() != Material.BONE_MEAL
        ) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block != null && isFloraBlock(block)) {
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onBoneMeal(@Nonnull BlockFertilizeEvent event) {
        for (BlockState blockState : event.getBlocks()) {
            Block issueBlock = blockState.getBlock();
            Location location = issueBlock.getLocation();
            unsafelyKillItem(location, StorageCacheUtils.getSfItem(location));
        }
    }

    @EventHandler
    public void onSnowmanBlockForm(@Nonnull EntityBlockFormEvent event) {
        Location location = event.getBlock().getLocation();
        unsafelyKillItem(location, StorageCacheUtils.getSfItem(location));
    }

    @EventHandler
    public void onLiquidDispense(@Nonnull BlockDispenseEvent event) {
        Block block = event.getBlock();
        if (isLiquid(event.getItem())
            && block.getBlockData() instanceof Directional directional
        ) {
            BlockFace face = directional.getFacing();
            Location location = block.getRelative(face).getLocation();
            unsafelyKillItem(location, StorageCacheUtils.getSfItem(location));
        }
    }

    /**
     * Grown plants and bushes are azalea blocks ({@link FloraBlocks}), which would normally pop off
     * when a neighbouring block updates. Keep them in place.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFloraPhysics(@Nonnull BlockPhysicsEvent event) {
        if (isFloraBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    /**
     * When the block under a grown plant or bush is broken, the plant can no longer stand.
     * Remove it properly (dropping its item) instead of leaving a floating block.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSupportBroken(@Nonnull BlockBreakEvent event) {
        Block above = event.getBlock().getRelative(BlockFace.UP);
        if (!FloraBlocks.isFloraMaterial(above.getType())) {
            return;
        }
        Location location = above.getLocation();
        SlimefunItem slimefunItem = StorageCacheUtils.getSfItem(location);
        if (isFlora(slimefunItem)) {
            unsafelyKillItem(location, slimefunItem);
        }
    }

    private boolean isFloraBlock(@Nonnull Block block) {
        return FloraBlocks.isFloraMaterial(block.getType()) && isFlora(StorageCacheUtils.getSfItem(block.getLocation()));
    }

    private boolean isFlora(@Nullable SlimefunItem slimefunItem) {
        return slimefunItem instanceof CultivationPlant || slimefunItem instanceof CultivationBush;
    }

    private void unsafelyKillItem(@Nonnull Location location, @Nullable SlimefunItem slimefunItem) {
        if (slimefunItem instanceof CultivationPlant plant) {
            location.getWorld().dropItem(location, plant.getDroppedItemStack(location));
            plant.removeCropped(location);
            plant.removePlant(location);
            plant.clearCaches(location);
            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
            location.getBlock().setType(Material.AIR);
        } else if (slimefunItem instanceof CultivationBush bush) {
            location.getWorld().dropItem(location, bush.getItem().clone());
            bush.removeBush(location);
            bush.clearCaches(location);
            Slimefun.getDatabaseManager().getBlockDataController().removeBlock(location);
            location.getBlock().setType(Material.AIR);
        }
    }

    private boolean isLiquid(@Nonnull ItemStack itemStack) {
        Material material = itemStack.getType();
        return material == Material.WATER_BUCKET
            || material == Material.LAVA_BUCKET
            || material == Material.POWDER_SNOW_BUCKET;
    }
}
