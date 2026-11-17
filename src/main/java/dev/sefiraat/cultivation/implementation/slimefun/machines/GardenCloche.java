package dev.sefiraat.cultivation.implementation.slimefun.machines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.cultivation.api.datatypes.instances.FloraLevelProfile;
import dev.sefiraat.cultivation.api.slimefun.items.plants.HarvestablePlant;
import dev.sefiraat.sefilib.string.Theme;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Grows the plant in its plant slot automatically. The plant is not used up.
 * <p>
 * A compressed cloche ({@link #getMultiplier()} > 1) combines several cloches into one: every plant in the
 * plant slot's stack counts as one cloche, up to the multiplier. When the growth roll succeeds, the harvest
 * is multiplied by that count, but only as far as the output has room, so nothing is ever lost.
 * Energy is only used for what is produced.
 */
public class GardenCloche extends SlimefunItem implements EnergyNetComponent, RecipeDisplayItem {

    private static final int PLANT_SLOT = 20;
    private static final int[] OUTPUT_SLOTS = new int[]{
        14, 15, 16, 23, 24, 25, 32, 33, 34
    };
    private static final int[] PLANT_SLOT_BACKGROUND = new int[]{
        10, 11, 12, 19, 21, 28, 29, 30
    };
    private static final int[] BACKGROUND = new int[]{
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 13, 17, 18, 22, 26, 27, 31, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
    };
    public static final int POWER_REQUIREMENT = 100;
    public static final int CAPACITY = 2500;

    private final int multiplier;

    public GardenCloche(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        this(itemGroup, item, recipeType, recipe, 1);
    }

    @ParametersAreNonnullByDefault
    public GardenCloche(ItemGroup itemGroup,
                        SlimefunItemStack item,
                        RecipeType recipeType,
                        ItemStack[] recipe,
                        int multiplier
    ) {
        super(itemGroup, item, recipeType, recipe);
        this.multiplier = Math.max(1, multiplier);
    }

    /**
     * @return How many cloches are combined into this one, 1 for a normal cloche
     */
    public int getMultiplier() {
        return multiplier;
    }

    @Override
    public void preRegister() {
        addItemHandler(
            new BlockBreakHandler(false, false) {
                @Override
                @ParametersAreNonnullByDefault
                public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                    Location location = e.getBlock().getLocation();
                    BlockMenu blockMenu = StorageCacheUtils.getMenu(location);
                    if (blockMenu != null) {
                        blockMenu.dropItems(location, PLANT_SLOT);
                        blockMenu.dropItems(location, OUTPUT_SLOTS);
                    }
                }
            },
            new BlockTicker() {
                @Override
                public boolean isSynchronized() {
                    return false;
                }

                @Override
                public void tick(Block block, SlimefunItem item, SlimefunBlockData data) {
                    BlockMenu blockMenu = StorageCacheUtils.getMenu(block.getLocation());
                    ItemStack possiblePlant = blockMenu.getItemInSlot(PLANT_SLOT);
                    SlimefunItem slimefunItem = SlimefunItem.getByItem(possiblePlant);
                    Location location = block.getLocation();
                    if (slimefunItem instanceof HarvestablePlant plant) {
                        if (getCharge(location) < POWER_REQUIREMENT) {
                            return;
                        }
                        FloraLevelProfile profile = FloraLevelProfile.fromItemStack(possiblePlant);
                        double growthRate = plant.getGrowthRate(profile);
                        double rand = ThreadLocalRandom.current().nextDouble();
                        if (rand < growthRate) {
                            ItemStack itemStack = plant.getRandomItemWithDropModifier(profile);
                            if (itemStack != null) {
                                harvest(blockMenu, location, itemStack, possiblePlant.getAmount());
                            }
                        }
                    }
                }
            }
        );
    }

    /**
     * Outputs the harvest once per combined cloche, limited by the plants in the slot (up to the multiplier),
     * the room in the output and the stored energy. Big amounts are split into valid stacks.
     */
    private void harvest(BlockMenu blockMenu, Location location, ItemStack harvest, int plantAmount) {
        int perHarvest = harvest.getAmount();
        if (perHarvest <= 0) {
            return;
        }
        int times = Math.min(multiplier, plantAmount);
        times = Math.min(times, getCharge(location) / POWER_REQUIREMENT);
        times = Math.min(times, getRoomFor(blockMenu, harvest) / perHarvest);
        if (times <= 0) {
            return;
        }

        int maxStackSize = harvest.getMaxStackSize();
        int remaining = perHarvest * times;
        while (remaining > 0) {
            ItemStack stack = harvest.clone();
            stack.setAmount(Math.min(remaining, maxStackSize));
            blockMenu.pushItem(stack, OUTPUT_SLOTS);
            remaining -= stack.getAmount();
        }
        removeCharge(location, POWER_REQUIREMENT * times);
    }

    /**
     * @return How many of the item the output slots can still take
     */
    private static int getRoomFor(BlockMenu blockMenu, ItemStack itemStack) {
        int maxStackSize = itemStack.getMaxStackSize();
        int room = 0;
        for (int slot : OUTPUT_SLOTS) {
            ItemStack existing = blockMenu.getItemInSlot(slot);
            if (existing == null || existing.getType().isAir()) {
                room += maxStackSize;
            } else if (existing.isSimilar(itemStack)) {
                room += Math.max(0, maxStackSize - existing.getAmount());
            }
        }
        return room;
    }

    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {

            @Override
            public void init() {
                ItemStack backgroundInput = new CustomItemStack(
                    Material.GREEN_STAINED_GLASS_PANE,
                    Theme.PASSIVE.apply("Insert Plant")
                );
                drawBackground(BACKGROUND);
                drawBackground(backgroundInput, PLANT_SLOT_BACKGROUND);
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return GardenCloche.this.canUse(player, false)
                    && Slimefun.getProtectionManager()
                    .hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.WITHDRAW) {
                    return OUTPUT_SLOTS;
                }
                return new int[0];
            }
        };
    }

    @NotNull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return (int) Math.min((long) CAPACITY * multiplier, Integer.MAX_VALUE);
    }

    @Nonnull
    @Override
    public List<ItemStack> getDisplayRecipes() {
        if (multiplier <= 1) {
            return Collections.emptyList();
        }
        return List.of(new CustomItemStack(
            Material.COMPARATOR,
            Theme.CLICK_INFO.asTitle("Multiplier", "x" + multiplier),
            Theme.PASSIVE.apply("Combines " + multiplier + " Garden Cloches into one."),
            Theme.PASSIVE.apply("Each plant in the slot counts as one cloche."),
            Theme.PASSIVE.apply("Energy: " + POWER_REQUIREMENT + " J per harvest per cloche")
        ));
    }
}
