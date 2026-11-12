package dev.sefiraat.cultivation.implementation.slimefun.groups;

import dev.sefiraat.cultivation.api.utils.Skins;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.chat.ChatInput;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * The main Cultivation menu in the Slimefun guide. Same layout as SefiLib's SimpleFlexGroup,
 * with an "Expansion by canyud" button in the header that sends the fork's GitHub link.
 */
public class CultivationMainGroup extends FlexItemGroup {

    public static final String GITHUB_URL = "https://github.com/mybezo/Cultivation";

    private static final int PAGE_SIZE = 36;
    private static final int GUIDE_BACK = 1;
    private static final int EXPANSION_INFO = 4;
    private static final int GUIDE_SEARCH = 7;
    private static final int PAGE_PREVIOUS = 46;
    private static final int PAGE_NEXT = 52;
    private static final int[] HEADER = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] FOOTER = new int[]{45, 46, 47, 48, 49, 50, 51, 52, 53};

    @Nonnull
    private final JavaPlugin plugin;
    @Nonnull
    private final String name;
    private final List<ItemGroup> itemGroups = new ArrayList<>();

    @ParametersAreNonnullByDefault
    public CultivationMainGroup(JavaPlugin plugin, String name, NamespacedKey key, ItemStack item) {
        super(key, item);
        this.plugin = plugin;
        this.name = name;
    }

    @Nonnull
    public CultivationMainGroup addItemGroup(@Nonnull ItemGroup itemGroup) {
        this.itemGroups.add(itemGroup);
        return this;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isVisible(Player player, PlayerProfile profile, SlimefunGuideMode mode) {
        return true;
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
        setupPage(player, profile, mode, menu, 1);
        menu.open(player);
    }

    @ParametersAreNonnullByDefault
    private void setupPage(Player player, PlayerProfile profile, SlimefunGuideMode mode, ChestMenu menu, int page) {
        int totalPages = Math.max(1, (int) Math.ceil(itemGroups.size() / (double) PAGE_SIZE));
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, itemGroups.size());

        applyHeader(player, profile, mode, menu);
        applyFooter(player, profile, mode, menu, page, totalPages);

        for (int i = 0; i < PAGE_SIZE; i++) {
            int slot = 9 + i;
            int index = start + i;
            if (index >= end) {
                menu.replaceExistingItem(slot, null);
                menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
                continue;
            }
            ItemGroup itemGroup = itemGroups.get(index);
            menu.replaceExistingItem(slot, itemGroup.getItem(player));
            menu.addMenuClickHandler(slot, (p, s, item, action) -> {
                profile.getGuideHistory().add(this, page);
                SlimefunGuide.openItemGroup(profile, itemGroup, mode, 1);
                return false;
            });
        }
    }

    @ParametersAreNonnullByDefault
    private void applyHeader(Player player, PlayerProfile profile, SlimefunGuideMode mode, ChestMenu menu) {
        menu.replaceExistingItem(GUIDE_BACK, ChestMenuUtils.getBackButton(
            player,
            "",
            ChatColor.GRAY + Slimefun.getLocalization().getMessage(player, "guide.back.guide")
        ));
        menu.addMenuClickHandler(GUIDE_BACK, (p, slot, item, action) -> {
            SlimefunGuide.openMainMenu(profile, mode, profile.getGuideHistory().getMainMenuPage());
            return false;
        });

        menu.replaceExistingItem(EXPANSION_INFO, getExpansionItem());
        menu.addMenuClickHandler(EXPANSION_INFO, (p, slot, item, action) -> {
            p.closeInventory();
            sendGithubLink(p);
            return false;
        });

        menu.replaceExistingItem(GUIDE_SEARCH, ChestMenuUtils.getSearchButton(player));
        menu.addMenuClickHandler(GUIDE_SEARCH, (p, slot, item, action) -> {
            p.closeInventory();
            Slimefun.getLocalization().sendMessage(p, "guide.search.message");
            ChatInput.waitForPlayer(plugin, p, msg -> SlimefunGuide.openSearch(profile, msg, mode, true));
            return false;
        });
    }

    @ParametersAreNonnullByDefault
    private void applyFooter(Player player,
                             PlayerProfile profile,
                             SlimefunGuideMode mode,
                             ChestMenu menu,
                             int page,
                             int totalPages
    ) {
        for (int slot : FOOTER) {
            menu.replaceExistingItem(slot, ChestMenuUtils.getBackground());
            menu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }
        menu.replaceExistingItem(PAGE_PREVIOUS, ChestMenuUtils.getPreviousButton(player, page, totalPages));
        menu.addMenuClickHandler(PAGE_PREVIOUS, (p, slot, item, action) -> {
            if (page > 1) {
                setupPage(p, profile, mode, menu, page - 1);
            }
            return false;
        });
        menu.replaceExistingItem(PAGE_NEXT, ChestMenuUtils.getNextButton(player, page, totalPages));
        menu.addMenuClickHandler(PAGE_NEXT, (p, slot, item, action) -> {
            if (page < totalPages) {
                setupPage(p, profile, mode, menu, page + 1);
            }
            return false;
        });
    }

    @Nonnull
    private static ItemStack getExpansionItem() {
        return new CustomItemStack(
            Skins.CULTIVATION_EXPANSION_MAIN.getPlayerHead(),
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "Expansion "
                + ChatColor.GRAY + "by " + ChatColor.AQUA + ChatColor.BOLD + "canyud",
            "",
            ChatColor.GRAY + "Machine and resource expansion",
            ChatColor.GRAY + "for Cultivation.",
            "",
            ChatColor.YELLOW + "Click to get the GitHub link"
        );
    }

    private static void sendGithubLink(@Nonnull Player player) {
        TextComponent link = new TextComponent(GITHUB_URL);
        link.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        link.setUnderlined(true);
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, GITHUB_URL));
        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Open in your browser")));

        player.spigot().sendMessage(new ComponentBuilder("Cultivation Expansion by canyud: ")
            .color(net.md_5.bungee.api.ChatColor.GRAY)
            .append(link)
            .create());
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }
}
