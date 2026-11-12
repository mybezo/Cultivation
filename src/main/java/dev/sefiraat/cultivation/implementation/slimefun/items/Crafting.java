package dev.sefiraat.cultivation.implementation.slimefun.items;

import dev.sefiraat.cultivation.Cultivation;
import dev.sefiraat.cultivation.api.slimefun.groups.CultivationGroups;
import dev.sefiraat.cultivation.implementation.slimefun.CultivationStacks;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class Crafting {

    private Crafting() {
        throw new IllegalStateException("Utility class");
    }

    public static void setup(Cultivation addon) {

        new SlimefunItem(
            CultivationGroups.CRAFTING,
            CultivationStacks.GENETIC_NECTAR,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            new ItemStack[]{
                null, null, null,
                null, new ItemStack(Material.SUGAR), null,
                null, null, null
            }
        ).register(addon);

        new SlimefunItem(
            CultivationGroups.CRAFTING,
            CultivationStacks.SILICONE,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            new ItemStack[]{
                SlimefunItems.CARBON, null, null,
                SlimefunItems.CARBON, null, null,
                null, null, null
            }
        ).register(addon);

        new SlimefunItem(
            CultivationGroups.CRAFTING,
            CultivationStacks.SPATULA,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            new ItemStack[]{
                null, CultivationStacks.SILICONE, null,
                null, CultivationStacks.SILICONE, null,
                null, new ItemStack(Material.STICK), null
            }
        ).register(addon);

        new SlimefunItem(
            CultivationGroups.CRAFTING,
            CultivationStacks.CHEF_HAT,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            new ItemStack[]{
                new ItemStack(Material.LEATHER), new ItemStack(Material.LEATHER), new ItemStack(Material.LEATHER),
                new ItemStack(Material.RABBIT_HIDE), null, new ItemStack(Material.RABBIT_HIDE),
                null, null, null
            }
        ).register(addon);

        // A player head, so it must not be placed as a block
        new UnplaceableBlock(
            CultivationGroups.CRAFTING,
            CultivationStacks.ANDROID_CHEF,
            RecipeType.ENHANCED_CRAFTING_TABLE,
            new ItemStack[]{
                null, CultivationStacks.CHEF_HAT, null,
                CultivationStacks.SPATULA, SlimefunItems.PROGRAMMABLE_ANDROID, CultivationStacks.SPATULA,
                null, SlimefunItems.ELECTRIC_MOTOR, null
            }
        ).register(addon);
    }
}
