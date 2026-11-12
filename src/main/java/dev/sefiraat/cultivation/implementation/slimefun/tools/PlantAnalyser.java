package dev.sefiraat.cultivation.implementation.slimefun.tools;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.cultivation.api.datatypes.instances.FloraLevelProfile;
import dev.sefiraat.cultivation.api.interfaces.CultivationLevelProfileHolder;
import dev.sefiraat.sefilib.string.Theme;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class PlantAnalyser extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    public PlantAnalyser(ItemGroup group, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(group, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return PlantAnalyser::onRightClick;
    }

    private static void onRightClick(PlayerRightClickEvent playerRightClickEvent) {
        if (playerRightClickEvent.getClickedBlock().isEmpty()) {
            // No block preset
            return;
        }

        Block block = playerRightClickEvent.getClickedBlock().get();
        SlimefunItem item = StorageCacheUtils.getSfItem(block.getLocation());

        if (item == null) {
            return;
        }

        showStats(playerRightClickEvent, item, block);
    }

    @ParametersAreNonnullByDefault
    private static void showStats(PlayerRightClickEvent event, SlimefunItem item, Block block) {
        if (item instanceof CultivationLevelProfileHolder profileHolder) {
            FloraLevelProfile profile = profileHolder.getLevelProfile(block);
            profile.setAnalyzed(true);
            profileHolder.setLevelProfile(block.getLocation(), profile);
            Player player = event.getPlayer();
            player.sendMessage(Theme.CLICK_INFO.apply(item.getItemName()));
            player.sendMessage(Theme.CLICK_INFO.asTitle("Level", profile.getLevel()));
            player.sendMessage(Theme.CLICK_INFO.asTitle("Speed", profile.getSpeed()));
            player.sendMessage(Theme.CLICK_INFO.asTitle("Strength", profile.getStrength()));
        }
    }
}
