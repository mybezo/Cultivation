package dev.sefiraat.cultivation.implementation.slimefun.items;

import dev.sefiraat.cultivation.Cultivation;
import dev.sefiraat.cultivation.api.slimefun.groups.CultivationGroups;
import dev.sefiraat.cultivation.api.utils.CultivationThemes;
import dev.sefiraat.cultivation.implementation.slimefun.CultivationStacks;
import dev.sefiraat.cultivation.implementation.slimefun.machines.GardenCloche;
import dev.sefiraat.sefilib.string.Theme;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Registers the compressed machines (multiple machines combined into one), configured by {@code compressed.yml}.
 * <p>
 * Every machine gets one item per multiplier (x8, x16, x32, x64). The x8 recipe uses 8 of the original machine,
 * every higher multiplier uses 2 of the previous one, all in the Compressor.
 * Only the highest enabled multiplier is listed in the guide; the lower ones are in the hidden group.
 */
public final class CompressedMachines {

    public static final String FILE_NAME = "compressed.yml";
    public static final int[] MULTIPLIERS = new int[]{8, 16, 32, 64};

    private static final String MACHINES_SECTION = "machines";

    private static int registeredCount = 0;

    private CompressedMachines() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @return True when at least one compressed machine is registered (the guide button is shown)
     */
    public static boolean hasAny() {
        return registeredCount > 0;
    }

    public static void setup(@Nonnull Cultivation addon) {
        FileConfiguration config = loadConfig(addon);
        if (!config.getBoolean("enabled")) {
            return;
        }

        // A multiplier is only available when all the lower multipliers are enabled (its recipe needs the previous one)
        List<Integer> multipliers = new ArrayList<>();
        for (int multiplier : MULTIPLIERS) {
            if (!config.getBoolean("multipliers.x" + multiplier)) {
                break;
            }
            multipliers.add(multiplier);
        }
        if (multipliers.isEmpty()) {
            return;
        }

        if (isMachineEnabled(config, Machines.GARDEN_CLOCHE)) {
            registerGardenCloche(addon, multipliers);
        }

        if (registeredCount > 0) {
            Cultivation.logInfo("Registered " + registeredCount + " compressed machines.");
        }
    }

    private static boolean isMachineEnabled(@Nonnull FileConfiguration config, @Nonnull SlimefunItem machine) {
        return config.getBoolean(MACHINES_SECTION + "." + machine.getId()) && !machine.isDisabled();
    }

    private static void registerGardenCloche(@Nonnull Cultivation addon, @Nonnull List<Integer> multipliers) {
        SlimefunItemStack previous = CultivationStacks.GARDEN_CLOCHE;
        int previousMultiplier = 1;
        int highest = multipliers.get(multipliers.size() - 1);

        for (int multiplier : multipliers) {
            SlimefunItemStack stack = Theme.themedSlimefunItemStack(
                CultivationStacks.GARDEN_CLOCHE.getItemId() + "_X" + multiplier,
                Material.GREEN_TERRACOTTA,
                CultivationThemes.MACHINE,
                "Garden Cloche " + ChatColor.GREEN + "(x" + multiplier + ")",
                "Grows plants inside it automatically.",
                "Each plant in the slot counts as one cloche.",
                "",
                Theme.CLICK_INFO.asTitle("Power per Tick", GardenCloche.POWER_REQUIREMENT * multiplier),
                "",
                ChatColor.GREEN + "Combines " + multiplier + " machines into one."
            );

            new GardenCloche(
                groupFor(multiplier, highest),
                stack,
                RecipeType.COMPRESSOR,
                compressorRecipe(previous, multiplier / previousMultiplier),
                multiplier
            ).register(addon);
            registeredCount++;

            previous = stack;
            previousMultiplier = multiplier;
        }
    }

    /**
     * Only the highest multiplier is listed in the guide, the lower ones are still craftable.
     */
    @Nonnull
    private static ItemGroup groupFor(int multiplier, int highest) {
        return multiplier == highest ? CultivationGroups.COMPRESSED_MACHINES : CultivationGroups.HIDDEN_CONTENT;
    }

    @Nonnull
    private static ItemStack[] compressorRecipe(@Nonnull SlimefunItemStack input, int amount) {
        ItemStack ingredient = input.clone();
        ingredient.setAmount(amount);
        return new ItemStack[]{ingredient, null, null, null, null, null, null, null, null};
    }

    /**
     * Loads {@code compressed.yml}, creating it on the first start. Options missing from an existing file are
     * added with their default value, values that are already set are kept.
     */
    @Nonnull
    private static FileConfiguration loadConfig(@Nonnull Cultivation addon) {
        File file = new File(addon.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            addon.saveResource(FILE_NAME, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        setDefaultValue(config, "enabled", true);
        for (int multiplier : MULTIPLIERS) {
            setDefaultValue(config, "multipliers.x" + multiplier, true);
        }
        setDefaultValue(config, MACHINES_SECTION + "." + Machines.GARDEN_CLOCHE.getId(), true);

        try {
            config.save(file);
        } catch (IOException e) {
            Cultivation.logWarning("Could not save " + FILE_NAME + ": " + e.getMessage());
        }
        return config;
    }

    private static void setDefaultValue(@Nonnull FileConfiguration config, @Nonnull String path, @Nonnull Object value) {
        if (!config.isSet(path)) {
            config.set(path, value);
        }
    }
}
