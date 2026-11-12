package dev.sefiraat.cultivation.implementation.slimefun.machines;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import dev.sefiraat.cultivation.implementation.slimefun.CultivationStacks;
import dev.sefiraat.sefilib.string.Theme;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
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
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * Automatically crafts the recipes of a {@link CraftingKitchenMachine} (the Finishing Counter)
 * when an Android Chef is inserted.
 * Every ingredient stack (except filled buckets, which don't stack) must hold at least 2 items:
 * the last one stays behind as the recipe template, so the machine can keep crafting from cargo input.
 */
public class AndroidFinishing extends SlimefunItem implements EnergyNetComponent {

    private static final int[] INPUT_SLOTS = new int[]{14, 15, 16, 23, 24, 25, 32, 33, 34};
    private static final int CHEF_SLOT = 10;
    private static final int STATUS_SLOT = 37;
    private static final int[] OUTPUT_SLOTS = new int[]{50, 51, 52};
    private static final int[] INPUT_BORDER = new int[]{4, 5, 6, 7, 8, 13, 17, 22, 26, 31, 35, 40, 41, 42, 43, 44};
    private static final int[] CHEF_BORDER = new int[]{0, 1, 2, 9, 11, 18, 19, 20};
    private static final int[] OUTPUT_BORDER = new int[]{49, 53};
    private static final int[] BACKGROUND = new int[]{3, 12, 21, 27, 28, 29, 30, 36, 38, 39, 45, 46, 47, 48};
    private static final int ENERGY_CONSUMPTION = 200;

    @Nonnull
    private final CraftingKitchenMachine targetCounter;

    @ParametersAreNonnullByDefault
    public AndroidFinishing(ItemGroup itemGroup,
                            SlimefunItemStack item,
                            RecipeType recipeType,
                            ItemStack[] recipe,
                            CraftingKitchenMachine targetCounter
    ) {
        super(itemGroup, item, recipeType, recipe);
        this.targetCounter = targetCounter;
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
                        menu.dropItems(location, INPUT_SLOTS);
                        menu.dropItems(location, CHEF_SLOT);
                        menu.dropItems(location, OUTPUT_SLOTS);
                    }
                }
            },
            new BlockTicker() {
                @Override
                public boolean isSynchronized() {
                    return true;
                }

                @Override
                public void tick(Block block, SlimefunItem item, SlimefunBlockData data) {
                    AndroidFinishing.this.tick(block);
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
                    new CustomItemStack(Material.BLUE_STAINED_GLASS_PANE, Theme.PASSIVE.apply("Recipe Input")),
                    INPUT_BORDER
                );
                drawBackground(
                    new CustomItemStack(Material.ORANGE_STAINED_GLASS_PANE, Theme.PASSIVE.apply("Insert Android Chef")),
                    CHEF_BORDER
                );
                drawBackground(
                    new CustomItemStack(Material.CYAN_STAINED_GLASS_PANE, Theme.PASSIVE.apply("Output")),
                    OUTPUT_BORDER
                );
                addItem(STATUS_SLOT, idleStatus(), ChestMenuUtils.getEmptyClickHandler());
            }

            @Override
            public boolean canOpen(@Nonnull Block block, @Nonnull Player player) {
                return AndroidFinishing.this.canUse(player, false)
                    && Slimefun.getProtectionManager()
                    .hasPermission(player, block.getLocation(), Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                if (flow == ItemTransportFlow.INSERT) {
                    return INPUT_SLOTS;
                }
                return OUTPUT_SLOTS;
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
                if (flow == ItemTransportFlow.WITHDRAW) {
                    return OUTPUT_SLOTS;
                }
                if (isChef(item)) {
                    return new int[]{CHEF_SLOT};
                }
                return INPUT_SLOTS;
            }
        };
    }

    protected void tick(@Nonnull Block block) {
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());
        if (menu == null) {
            return;
        }
        if (!isChef(menu.getItemInSlot(CHEF_SLOT))) {
            setStatus(menu, Material.BARRIER, Theme.ERROR.apply("Missing Android Chef"));
            return;
        }
        if (getCharge(block.getLocation()) < ENERGY_CONSUMPTION) {
            setStatus(menu, Material.RED_STAINED_GLASS_PANE, Theme.ERROR.apply("No Power"));
            return;
        }

        ItemStack[] grid = new ItemStack[INPUT_SLOTS.length];
        boolean hasItems = false;
        for (int i = 0; i < INPUT_SLOTS.length; i++) {
            ItemStack itemStack = menu.getItemInSlot(INPUT_SLOTS[i]);
            grid[i] = itemStack;
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }
            hasItems = true;
            if (!KitchenObject.isBucket(itemStack) && itemStack.getAmount() < 2) {
                // Keep the last item of each stack as the recipe template
                setStatus(menu, Material.PAPER, Theme.WARNING.apply("Waiting for Ingredients"));
                return;
            }
        }
        if (!hasItems) {
            menu.replaceExistingItem(STATUS_SLOT, idleStatus());
            return;
        }

        ItemStack result = targetCounter.testRecipe(grid);
        SlimefunItem resultItem = result == null ? null : SlimefunItem.getByItem(result);
        if (result == null || (resultItem != null && resultItem.isDisabled())) {
            setStatus(menu, Material.RED_STAINED_GLASS_PANE, Theme.ERROR.apply("Invalid Recipe"));
            return;
        }
        if (!menu.fits(result, OUTPUT_SLOTS)) {
            setStatus(menu, Material.CHEST, Theme.WARNING.apply("Output Full"));
            return;
        }

        // Output the result first, so empty buckets can never take its space
        menu.pushItem(result.clone(), OUTPUT_SLOTS);
        for (int slot : INPUT_SLOTS) {
            ItemStack itemStack = menu.getItemInSlot(slot);
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }
            if (KitchenObject.isBucket(itemStack)) {
                // Filled buckets don't stack, so they are used up and the empty bucket goes to the output.
                // If the output has no room for it, it stays in the input slot instead of being lost.
                menu.replaceExistingItem(slot, menu.pushItem(new ItemStack(Material.BUCKET), OUTPUT_SLOTS));
            } else {
                menu.consumeItem(slot);
            }
        }
        removeCharge(block.getLocation(), ENERGY_CONSUMPTION);
        setStatus(menu, Material.LIME_STAINED_GLASS_PANE, Theme.SUCCESS.apply("Chef is Cooking..."));
    }

    private static boolean isChef(@Nullable ItemStack itemStack) {
        return itemStack != null && SlimefunUtils.isItemSimilar(itemStack, CultivationStacks.ANDROID_CHEF, false);
    }

    private static void setStatus(@Nonnull BlockMenu menu, @Nonnull Material material, @Nonnull String name) {
        menu.replaceExistingItem(STATUS_SLOT, new CustomItemStack(material, name));
    }

    @Nonnull
    private static ItemStack idleStatus() {
        return new CustomItemStack(Material.BLACK_STAINED_GLASS_PANE, Theme.PASSIVE.apply("Idle"));
    }
}
