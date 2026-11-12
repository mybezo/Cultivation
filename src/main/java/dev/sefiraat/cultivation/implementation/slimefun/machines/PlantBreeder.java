package dev.sefiraat.cultivation.implementation.slimefun.machines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.cultivation.Registry;
import dev.sefiraat.cultivation.api.datatypes.instances.FloraLevelProfile;
import dev.sefiraat.cultivation.api.slimefun.items.plants.CultivationPlant;
import dev.sefiraat.cultivation.api.slimefun.plant.BreedingPair;
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
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Breeds two plants together using Genetic Nectar.
 * Two plants forming a breeding pair have a chance to produce the child; otherwise one of
 * the parents is copied. Two identical plants produce a copy of themselves.
 * The child's stats are the average of its parents. Breeding here does not unlock the Plant Codex.
 */
public class PlantBreeder extends SlimefunItem
    implements EnergyNetComponent, MachineProcessHolder<PlantBreeder.BreedingOperation> {

    private static final int INPUT_SLOT_1 = 10;
    private static final int INPUT_SLOT_2 = 16;
    private static final int NECTAR_SLOT = 13;
    private static final int STATUS_SLOT = 37;
    private static final int[] OUTPUT_SLOTS = new int[]{
        30, 31, 32, 33, 34, 35, 39, 40, 41, 42, 43, 44, 48, 49, 50, 51, 52, 53
    };
    private static final int[] PLANT_BORDER = new int[]{0, 1, 2, 9, 11, 18, 19, 20, 6, 7, 8, 15, 17, 24, 25, 26};
    private static final int[] NECTAR_BORDER = new int[]{3, 4, 5, 12, 14, 21, 22, 23};
    private static final int[] STATUS_BORDER = new int[]{27, 28, 29, 36, 38, 45, 46, 47};
    private static final int ENERGY_CONSUMPTION = 750;
    private static final double MAX_TIME_SECONDS = 180;
    private static final double MIN_TIME_SECONDS = 30;
    private static final double CHILD_CHANCE = 0.25;
    private static final int MAX_STAT = 10;

    private final MachineProcessor<BreedingOperation> processor = new MachineProcessor<>(this);

    @ParametersAreNonnullByDefault
    public PlantBreeder(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    public MachineProcessor<BreedingOperation> getMachineProcessor() {
        return processor;
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return 4000;
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
                        menu.dropItems(location, INPUT_SLOT_1, INPUT_SLOT_2, NECTAR_SLOT);
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
                    PlantBreeder.this.tick(block);
                }
            }
        );
    }

    @Override
    public void postRegister() {
        new BlockMenuPreset(this.getId(), this.getItemName()) {
            @Override
            public void init() {
                drawBackground(
                    new CustomItemStack(Material.BLUE_STAINED_GLASS_PANE, Theme.PASSIVE.apply("放入植物")),
                    PLANT_BORDER
                );
                drawBackground(
                    new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, Theme.PASSIVE.apply("放入基因花蜜")),
                    NECTAR_BORDER
                );
                drawBackground(STATUS_BORDER);
                addItem(STATUS_SLOT, idleStatus(), ChestMenuUtils.getEmptyClickHandler());
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return PlantBreeder.this.canUse(player, false)
                    && Slimefun.getProtectionManager()
                    .hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) {
                    return new int[]{NECTAR_SLOT, INPUT_SLOT_1, INPUT_SLOT_2};
                }
                return OUTPUT_SLOTS;
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
                if (flow == ItemTransportFlow.WITHDRAW) {
                    return OUTPUT_SLOTS;
                }
                if (isNectar(item)) {
                    return new int[]{NECTAR_SLOT};
                }
                if (item != null && SlimefunItem.getByItem(item) instanceof CultivationPlant) {
                    return new int[]{INPUT_SLOT_1, INPUT_SLOT_2};
                }
                return new int[0];
            }
        };
    }

    protected void tick(@Nonnull Block block) {
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if (menu == null) {
            return;
        }

        BreedingOperation operation = processor.getOperation(block);
        if (operation == null) {
            tryStartBreeding(block, menu);
            return;
        }

        if (!operation.isFinished()) {
            if (getCharge(block.getLocation()) < ENERGY_CONSUMPTION) {
                setStatus(menu, Material.RED_STAINED_GLASS_PANE, Theme.ERROR.apply("电力不足"));
                return;
            }
            removeCharge(block.getLocation(), ENERGY_CONSUMPTION);
            operation.addProgress(1);
            updateProgress(menu, operation);
            return;
        }

        ItemStack result = operation.getResult();
        if (menu.fits(result, OUTPUT_SLOTS)) {
            menu.pushItem(result, OUTPUT_SLOTS);
            processor.endOperation(block);
            menu.replaceExistingItem(STATUS_SLOT, idleStatus());
        } else {
            setStatus(menu, Material.CHEST, Theme.WARNING.apply("输出栏已满"));
        }
    }

    private void tryStartBreeding(@Nonnull Block block, @Nonnull BlockMenu menu) {
        ItemStack item1 = menu.getItemInSlot(INPUT_SLOT_1);
        ItemStack item2 = menu.getItemInSlot(INPUT_SLOT_2);
        ItemStack nectar = menu.getItemInSlot(NECTAR_SLOT);

        if (item1 == null || item2 == null) {
            menu.replaceExistingItem(STATUS_SLOT, idleStatus());
            return;
        }
        if (!(SlimefunItem.getByItem(item1) instanceof CultivationPlant plant1)
            || !(SlimefunItem.getByItem(item2) instanceof CultivationPlant plant2)
        ) {
            setStatus(menu, Material.RED_STAINED_GLASS_PANE, Theme.ERROR.apply("请放入农耕工艺的植物"));
            return;
        }
        if (!isNectar(nectar)) {
            setStatus(menu, Material.RED_STAINED_GLASS_PANE, Theme.ERROR.apply("缺少基因花蜜"));
            return;
        }
        if (!hasEmptyOutputSlot(menu)) {
            setStatus(menu, Material.CHEST, Theme.WARNING.apply("输出栏已满"));
            return;
        }

        CultivationPlant child;
        boolean crossBreed;
        BreedingPair pair = findBreedingPair(plant1, plant2);
        if (pair != null) {
            child = pair.getChild();
            crossBreed = true;
        } else if (plant1.getId().equals(plant2.getId())) {
            child = plant1;
            crossBreed = false;
        } else {
            setStatus(menu, Material.RED_STAINED_GLASS_PANE, Theme.ERROR.apply("这两株植物无法杂交"));
            return;
        }

        FloraLevelProfile profile1 = FloraLevelProfile.fromItemStack(item1);
        FloraLevelProfile profile2 = FloraLevelProfile.fromItemStack(item2);
        nectar.setAmount(nectar.getAmount() - 1);

        int averageStrength = average(profile1.getStrength(), profile2.getStrength());
        processor.startOperation(block, new BreedingOperation(
            getProcessTicks(averageStrength), plant1, plant2, child, profile1, profile2, crossBreed
        ));
    }

    /**
     * Strength 1 takes {@link #MAX_TIME_SECONDS}, strength 10 takes {@link #MIN_TIME_SECONDS}.
     * Slimefun ticks twice a second.
     */
    private static int getProcessTicks(int strength) {
        int clamped = Math.max(1, Math.min(MAX_STAT, strength));
        double step = (MAX_TIME_SECONDS - MIN_TIME_SECONDS) / (MAX_STAT - 1);
        double seconds = MAX_TIME_SECONDS - (clamped - 1) * step;
        return (int) (seconds * 2);
    }

    private static int average(int a, int b) {
        return Math.min(MAX_STAT, (int) Math.ceil((a + b) / 2.0));
    }

    @Nullable
    private static BreedingPair findBreedingPair(@Nonnull CultivationPlant plant1, @Nonnull CultivationPlant plant2) {
        for (BreedingPair pair : Registry.getInstance().getPlantBreedingPairs()) {
            if (pair.isBreedPossible(plant1.getId(), plant2.getId())) {
                return pair;
            }
        }
        return null;
    }

    private static boolean hasEmptyOutputSlot(@Nonnull BlockMenu menu) {
        for (int slot : OUTPUT_SLOTS) {
            ItemStack item = menu.getItemInSlot(slot);
            if (item == null || item.getType().isAir()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNectar(@Nullable ItemStack itemStack) {
        return itemStack != null && SlimefunUtils.isItemSimilar(itemStack, CultivationStacks.GENETIC_NECTAR, false);
    }

    private static void setStatus(@Nonnull BlockMenu menu, @Nonnull Material material, @Nonnull String name) {
        menu.replaceExistingItem(STATUS_SLOT, new CustomItemStack(material, name));
    }

    @Nonnull
    private static ItemStack idleStatus() {
        return new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, Theme.PASSIVE.apply("空闲"));
    }

    private static void updateProgress(@Nonnull BlockMenu menu, @Nonnull BreedingOperation operation) {
        int percent = (int) ((double) operation.getProgress() / operation.getTotalTicks() * 100);
        int secondsLeft = (int) Math.ceil(operation.getRemainingTicks() / 2.0);
        Material glass = percent < 33
            ? Material.RED_STAINED_GLASS_PANE
            : percent < 66 ? Material.YELLOW_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE;
        menu.replaceExistingItem(STATUS_SLOT, new CustomItemStack(
            glass,
            Theme.SUCCESS.apply("杂交中... " + secondsLeft + "秒"),
            Theme.PASSIVE.apply("进度: " + percent + "%")
        ));
    }

    public static class BreedingOperation implements MachineOperation {

        private final int totalTicks;
        private final CultivationPlant parent1;
        private final CultivationPlant parent2;
        private final CultivationPlant child;
        private final FloraLevelProfile profile1;
        private final FloraLevelProfile profile2;
        private final boolean crossBreed;
        private int progress = 0;
        @Nullable
        private ItemStack result;

        @ParametersAreNonnullByDefault
        public BreedingOperation(int totalTicks,
                                 CultivationPlant parent1,
                                 CultivationPlant parent2,
                                 CultivationPlant child,
                                 FloraLevelProfile profile1,
                                 FloraLevelProfile profile2,
                                 boolean crossBreed
        ) {
            this.totalTicks = totalTicks;
            this.parent1 = parent1;
            this.parent2 = parent2;
            this.child = child;
            this.profile1 = profile1;
            this.profile2 = profile2;
            this.crossBreed = crossBreed;
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

        /**
         * The result is rolled once and cached, so a full output does not re-roll it.
         */
        @Nonnull
        public ItemStack getResult() {
            if (result == null) {
                CultivationPlant plant = child;
                if (crossBreed && ThreadLocalRandom.current().nextDouble() >= CHILD_CHANCE) {
                    plant = ThreadLocalRandom.current().nextBoolean() ? parent1 : parent2;
                }
                FloraLevelProfile profile = new FloraLevelProfile(
                    average(profile1.getLevel(), profile2.getLevel()),
                    average(profile1.getSpeed(), profile2.getSpeed()),
                    average(profile1.getStrength(), profile2.getStrength()),
                    true
                );
                result = CultivationPlant.getStack(plant, profile);
            }
            return result.clone();
        }
    }
}
