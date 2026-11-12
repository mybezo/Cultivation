package dev.sefiraat.cultivation.implementation.slimefun.machines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.cultivation.api.datatypes.instances.FloraLevelProfile;
import dev.sefiraat.cultivation.api.slimefun.items.plants.CultivationPlant;
import dev.sefiraat.cultivation.implementation.slimefun.CultivationStacks;
import dev.sefiraat.sefilib.string.Theme;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mutates a single plant seed, raising one or more of its stats.
 * With Genetic Nectar in the nectar slot, the seed keeps mutating until it is maxed out
 * or the nectar runs out.
 */
public class GeneticRecombinator extends SlimefunItem
    implements EnergyNetComponent, MachineProcessHolder<GeneticRecombinator.MutationOperation> {

    private static final int INPUT_SLOT = 10;
    private static final int NECTAR_SLOT = 16;
    private static final int STATUS_SLOT = 13;
    private static final int[] OUTPUT_SLOTS = new int[]{37, 38, 39, 40, 41, 42, 43};
    private static final int[] BACKGROUND = new int[]{
        3, 4, 5, 12, 14, 21, 22, 23, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
    };
    private static final int[] PLANT_SLOT_BACKGROUND = new int[]{0, 1, 2, 9, 11, 18, 19, 20};
    private static final int[] NECTAR_SLOT_BACKGROUND = new int[]{6, 7, 8, 15, 17, 24, 25, 26};
    private static final int ENERGY_CONSUMPTION = 500;
    private static final int PROCESS_TICKS = 30;
    private static final int MAX_STAT = 10;

    private final MachineProcessor<MutationOperation> processor = new MachineProcessor<>(this);

    @ParametersAreNonnullByDefault
    public GeneticRecombinator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    public MachineProcessor<MutationOperation> getMachineProcessor() {
        return processor;
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return 2000;
    }

    @Override
    public void preRegister() {
        addItemHandler(
            new BlockBreakHandler(false, false) {
                @Override
                @ParametersAreNonnullByDefault
                public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                    Location location = e.getBlock().getLocation();
                    BlockMenu menu = StorageCacheUtils.getMenu(location);
                    if (menu != null) {
                        menu.dropItems(location, INPUT_SLOT, NECTAR_SLOT);
                        menu.dropItems(location, OUTPUT_SLOTS);
                    }
                    processor.endOperation(e.getBlock());
                }
            },
            new BlockPlaceHandler(false) {
                @Override
                public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                    processor.endOperation(e.getBlock());
                }
            },
            new BlockTicker() {
                @Override
                public boolean isSynchronized() {
                    return true;
                }

                @Override
                public void tick(Block block, SlimefunItem item, SlimefunBlockData data) {
                    GeneticRecombinator.this.tick(block);
                }
            }
        );
    }

    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {
            @Override
            public void init() {
                drawBackground(BACKGROUND);
                drawBackground(
                    new CustomItemStack(Material.BLUE_STAINED_GLASS_PANE, Theme.PASSIVE.apply("Insert Plant")),
                    PLANT_SLOT_BACKGROUND
                );
                drawBackground(
                    new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, Theme.PASSIVE.apply("Insert Genetic Nectar")),
                    NECTAR_SLOT_BACKGROUND
                );
                addItem(STATUS_SLOT, idleStatus(), ChestMenuUtils.getEmptyClickHandler());
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return GeneticRecombinator.this.canUse(player, false)
                    && Slimefun.getProtectionManager()
                    .hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) {
                    return new int[]{INPUT_SLOT, NECTAR_SLOT};
                }
                return OUTPUT_SLOTS;
            }
        };
    }

    protected void tick(@Nonnull Block block) {
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if (menu == null) {
            return;
        }

        // Nectar put into the plant slot (e.g. by cargo) is moved to where it belongs
        ItemStack input = menu.getItemInSlot(INPUT_SLOT);
        if (isNectar(input)) {
            menu.replaceExistingItem(INPUT_SLOT, menu.pushItem(input, NECTAR_SLOT));
        }

        MutationOperation operation = processor.getOperation(block);
        if (operation == null) {
            startNewOperation(block, menu);
            return;
        }

        if (!operation.isFinished()) {
            if (getCharge(block.getLocation()) < ENERGY_CONSUMPTION) {
                menu.replaceExistingItem(STATUS_SLOT, new CustomItemStack(
                    Material.RED_STAINED_GLASS_PANE, Theme.ERROR.apply("No Power")
                ));
                return;
            }
            removeCharge(block.getLocation(), ENERGY_CONSUMPTION);
            operation.addProgress(1);
            updateProgress(menu, operation);
            return;
        }

        FloraLevelProfile mutated = operation.getResultProfile();
        ItemStack nectar = menu.getItemInSlot(NECTAR_SLOT);
        if (!isMaxed(mutated) && isNectar(nectar)) {
            // Keep mutating while nectar is available
            nectar.setAmount(nectar.getAmount() - 1);
            processor.endOperation(block);
            processor.startOperation(block, new MutationOperation(PROCESS_TICKS, operation.getPlant(), mutated));
            return;
        }

        ItemStack result = CultivationPlant.getStack(operation.getPlant(), mutated);
        if (menu.fits(result, OUTPUT_SLOTS)) {
            menu.pushItem(result, OUTPUT_SLOTS);
            processor.endOperation(block);
            menu.replaceExistingItem(STATUS_SLOT, idleStatus());
        } else {
            menu.replaceExistingItem(STATUS_SLOT, new CustomItemStack(
                Material.CHEST, Theme.WARNING.apply("Output Full")
            ));
        }
    }

    private void startNewOperation(@Nonnull Block block, @Nonnull BlockMenu menu) {
        ItemStack input = menu.getItemInSlot(INPUT_SLOT);
        if (input == null || input.getType().isAir()) {
            return;
        }
        if (!(SlimefunItem.getByItem(input) instanceof CultivationPlant plant)) {
            return;
        }
        FloraLevelProfile profile = FloraLevelProfile.fromItemStack(input);
        if (isMaxed(profile)) {
            return;
        }
        input.setAmount(input.getAmount() - 1);
        processor.startOperation(block, new MutationOperation(PROCESS_TICKS, plant, profile));
    }

    private static boolean isNectar(@Nullable ItemStack itemStack) {
        return itemStack != null && SlimefunUtils.isItemSimilar(itemStack, CultivationStacks.GENETIC_NECTAR, false);
    }

    private static boolean isMaxed(@Nonnull FloraLevelProfile profile) {
        return profile.getLevel() >= MAX_STAT && profile.getSpeed() >= MAX_STAT && profile.getStrength() >= MAX_STAT;
    }

    @Nonnull
    private static ItemStack idleStatus() {
        return new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, Theme.PASSIVE.apply("Idle"));
    }

    private static void updateProgress(@Nonnull BlockMenu menu, @Nonnull MutationOperation operation) {
        int percent = (int) ((double) operation.getProgress() / operation.getTotalTicks() * 100);
        int secondsLeft = (int) Math.ceil(operation.getRemainingTicks() / 2.0);
        menu.replaceExistingItem(STATUS_SLOT, new CustomItemStack(
            Material.LIME_STAINED_GLASS_PANE,
            Theme.SUCCESS.apply("Mutating... " + secondsLeft + "s"),
            Theme.PASSIVE.apply("Progress: " + percent + "%")
        ));
    }

    public static class MutationOperation implements MachineOperation {

        private final int totalTicks;
        private final CultivationPlant plant;
        private final FloraLevelProfile originalProfile;
        private int progress = 0;
        @Nullable
        private FloraLevelProfile resultProfile;

        public MutationOperation(int totalTicks, @Nonnull CultivationPlant plant, @Nonnull FloraLevelProfile profile) {
            this.totalTicks = totalTicks;
            this.plant = plant;
            this.originalProfile = profile;
        }

        @Override
        public void addProgress(int num) {
            this.progress += num;
        }

        @Override
        public int getProgress() {
            return progress;
        }

        @Override
        public int getTotalTicks() {
            return totalTicks;
        }

        @Nonnull
        public CultivationPlant getPlant() {
            return plant;
        }

        /**
         * Rolls 1-3 mutations (more being rarer), each raising a random non-maxed stat by 1-3.
         * The result is rolled once and cached.
         */
        @Nonnull
        public FloraLevelProfile getResultProfile() {
            if (resultProfile != null) {
                return resultProfile;
            }
            int level = originalProfile.getLevel();
            int speed = originalProfile.getSpeed();
            int strength = originalProfile.getStrength();
            ThreadLocalRandom random = ThreadLocalRandom.current();

            int roll = random.nextInt(100);
            int mutations = roll > 96 ? 3 : roll > 80 ? 2 : 1;

            for (int i = 0; i < mutations; i++) {
                List<Integer> stats = new ArrayList<>();
                if (level < MAX_STAT) {
                    stats.add(0);
                }
                if (speed < MAX_STAT) {
                    stats.add(1);
                }
                if (strength < MAX_STAT) {
                    stats.add(2);
                }
                if (stats.isEmpty()) {
                    break;
                }
                int gainRoll = random.nextInt(100);
                int gain = gainRoll > 95 ? 3 : gainRoll > 80 ? 2 : 1;
                switch (stats.get(random.nextInt(stats.size()))) {
                    case 0 -> level = Math.min(MAX_STAT, level + gain);
                    case 1 -> speed = Math.min(MAX_STAT, speed + gain);
                    default -> strength = Math.min(MAX_STAT, strength + gain);
                }
            }
            resultProfile = new FloraLevelProfile(level, speed, strength, true);
            return resultProfile;
        }
    }
}
