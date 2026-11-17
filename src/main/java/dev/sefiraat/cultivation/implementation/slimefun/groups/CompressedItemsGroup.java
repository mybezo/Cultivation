package dev.sefiraat.cultivation.implementation.slimefun.groups;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * The "Compressed Items" menu, opened from the main Cultivation menu. Lists one group per kind of
 * compressed item (e.g. Compressed Machine).
 */
public class CompressedItemsGroup extends FlexItemGroup {

    private static final int GUIDE_BACK = 1;
    private static final int[] HEADER = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] FOOTER = new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final int FIRST_GROUP_SLOT = 9;

    @Nonnull
    private final String name;
    private final List<ItemGroup> itemGroups = new ArrayList<>();

    @ParametersAreNonnullByDefault
    public CompressedItemsGroup(String name, NamespacedKey key, ItemStack item) {
        super(key, item);
        this.name = name;
    }

    @Nonnull
    public CompressedItemsGroup addItemGroup(@Nonnull ItemGroup itemGroup) {
        this.itemGroups.add(itemGroup);
        return this;
    }

    /**
     * Only reachable from the main Cultivation menu, not listed in the guide's main menu.
     */
    @Override
    @ParametersAreNonnullByDefault
    public boolean isVisible(Player player, PlayerProfile profile, SlimefunGuideMode mode) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void open(Player player, PlayerProfile profile, SlimefunGuideMode mode) {
        ChestMenu menu = new ChestMenu(name);
        for (int slot : HEADER) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : FOOTER) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        menu.setEmptySlotsClickable(false);
        menu.addMenuOpeningHandler(p -> p.playSound(p.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1));

        menu.replaceExistingItem(GUIDE_BACK, ChestMenuUtils.getBackButton(
            player,
            "",
            ChatColor.GRAY + Slimefun.getLocalization().getMessage(player, "guide.back.guide")
        ));
        menu.addMenuClickHandler(GUIDE_BACK, (p, slot, item, action) -> {
            profile.getGuideHistory().goBack(Slimefun.getRegistry().getSlimefunGuide(mode));
            return false;
        });

        int slot = FIRST_GROUP_SLOT;
        for (ItemGroup itemGroup : itemGroups) {
            menu.addItem(slot, itemGroup.getItem(player), (p, s, item, action) -> {
                profile.getGuideHistory().add(this, 1);
                SlimefunGuide.openItemGroup(profile, itemGroup, mode, 1);
                return false;
            });
            slot++;
        }

        menu.open(player);
    }
}
