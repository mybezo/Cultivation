package dev.sefiraat.cultivation.implementation.slimefun.tools;

import dev.sefiraat.cultivation.Registry;
import dev.sefiraat.cultivation.api.slimefun.plant.BreedingPair;
import dev.sefiraat.cultivation.api.utils.StatisticUtils;
import dev.sefiraat.sefilib.string.Theme;
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

/**
 * A single-use book that unlocks every bred plant in the Plant Codex for the player.
 */
public class RecordOfBotanist extends SimpleSlimefunItem<ItemUseHandler> implements NotPlaceable {

    @ParametersAreNonnullByDefault
    public RecordOfBotanist(ItemGroup group, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(group, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return RecordOfBotanist::onRightClick;
    }

    private static void onRightClick(@Nonnull PlayerRightClickEvent event) {
        event.cancel();
        Player player = event.getPlayer();

        Set<String> plantIds = new HashSet<>();
        for (BreedingPair pair : Registry.getInstance().getPlantBreedingPairs()) {
            plantIds.add(pair.getChild().getId());
        }

        if (plantIds.isEmpty()) {
            player.sendMessage(Theme.WARNING.apply("No plants to discover!"));
            return;
        }

        int unlocked = 0;
        for (String id : plantIds) {
            if (!StatisticUtils.isDiscovered(player, id)) {
                StatisticUtils.unlockDiscovery(player, id);
                unlocked++;
            }
        }

        if (unlocked == 0) {
            player.sendMessage(Theme.WARNING.apply("You have already discovered all plants!"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return;
        }

        player.sendMessage(Theme.SUCCESS.apply("Plant Codex Unlocked!"));
        player.sendMessage(Theme.CLICK_INFO.asTitle("Discovered", unlocked + "/" + plantIds.size()));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack item = event.getItem();
            item.setAmount(item.getAmount() - 1);
        }
    }
}
