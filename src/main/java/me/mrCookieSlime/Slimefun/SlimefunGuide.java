package me.mrCookieSlime.Slimefun;

import io.github.thebusybiscuit.cscorelib2.chat.ChatColors;
import io.github.thebusybiscuit.slimefun4.core.guide.ISlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideLayout;
import me.mrCookieSlime.Slimefun.Objects.Category;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.Slimefun.api.PlayerProfile;
import me.mrCookieSlime.Slimefun.api.Slimefun;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public final class SlimefunGuide {

	private SlimefunGuide() {}

	public static ItemStack getItem(SlimefunGuideLayout design) {
		ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
		ItemMeta meta = item.getItemMeta();
		List<String> lore = new LinkedList<>();
		lore.addAll(Arrays.asList("", ChatColors.color("&e右键 &8\u21E8 &7浏览物品"), ChatColors.color("&eShift + 右键 &8\u21E8 &7打开 设置")));

		switch (design) {
		case BOOK:
			meta.setDisplayName(ChatColors.color("&aSlimefun 指南 &7(书本界面)"));
			break;
		case CHEAT_SHEET:
			meta.setDisplayName(ChatColors.color("&cSlimefun 指南 &4(作弊界面)"));
			lore.add(0, ChatColors.color("&4&l仅限管理员使用"));
			lore.add(0, "");
			break;
		case CHEST:
			meta.setDisplayName(ChatColors.color("&aSlimefun 指南 &7(箱子界面)"));
			break;
		default:
			return null;
		}

		meta.setLore(lore);
		SlimefunPlugin.getItemTextureService().setTexture(meta, "SLIMEFUN_GUIDE");
		item.setItemMeta(meta);
		return item;
	}

	public static void openCheatMenu(Player p) {
		openMainMenuAsync(p, false, SlimefunGuideLayout.CHEAT_SHEET, 1);
	}

	public static void openGuide(Player p, ItemStack guide) {
		if (SlimefunManager.isItemSimilar(guide, getItem(SlimefunGuideLayout.CHEST), true)) {
			openGuide(p, SlimefunGuideLayout.CHEST);
		}
		else if (SlimefunManager.isItemSimilar(guide, getItem(SlimefunGuideLayout.BOOK), true)) {
			openGuide(p, SlimefunGuideLayout.BOOK);
		}
		else if (SlimefunManager.isItemSimilar(guide, getItem(SlimefunGuideLayout.CHEAT_SHEET), true)) {
			openGuide(p, SlimefunGuideLayout.CHEAT_SHEET);
		} else {
		    openGuide(p, SlimefunGuideLayout.CHEST);
        }
	}

	public static void openGuide(Player p, SlimefunGuideLayout layout) {
		if (!SlimefunPlugin.getWhitelist().getBoolean(p.getWorld().getName() + ".enabled")) return;
		if (!SlimefunPlugin.getWhitelist().getBoolean(p.getWorld().getName() + ".enabled-items.SLIMEFUN_GUIDE")) return;

		ISlimefunGuide guide = SlimefunPlugin.getRegistry().getGuideLayout(layout);
		Object last;

		Optional<PlayerProfile> profile = PlayerProfile.find(p);
		if (profile.isPresent()) {
			last = guide.getLastEntry(profile.get(), false);
			guide.handleHistory(profile.get(), last, true);
		}
		else {
			openMainMenuAsync(p, true, layout, 1);
		}
	}

	private static void openMainMenuAsync(Player player, boolean survival, SlimefunGuideLayout layout, int selectedPage) {
		if (!PlayerProfile.get(player, profile -> Slimefun.runSync(() -> openMainMenu(profile, layout, survival, selectedPage))))
			SlimefunPlugin.getLocal().sendMessage(player, "messages.opening-guide");
	}

	public static void openMainMenu(PlayerProfile profile, SlimefunGuideLayout layout, boolean survival, int selectedPage) {
		SlimefunPlugin.getRegistry().getGuideLayout(layout).openMainMenu(profile, survival, selectedPage);
	}

	public static void openCategory(PlayerProfile profile, Category category, SlimefunGuideLayout layout, boolean survival, int selectedPage) {
		if (category == null) return;
		SlimefunPlugin.getRegistry().getGuideLayout(layout).openCategory(profile, category, survival, selectedPage);
	}

	public static void openSearch(PlayerProfile profile, String input, boolean survival, boolean addToHistory) {
		SlimefunPlugin.getRegistry().getGuideLayout(SlimefunGuideLayout.CHEST).openSearch(profile, input, survival, addToHistory);
	}

	public static void displayItem(PlayerProfile profile, ItemStack item, boolean addToHistory) {
		SlimefunPlugin.getRegistry().getGuideLayout(SlimefunGuideLayout.CHEST).displayItem(profile, item, addToHistory);
	}

	public static void displayItem(PlayerProfile profile, SlimefunItem item, boolean addToHistory) {
		SlimefunPlugin.getRegistry().getGuideLayout(SlimefunGuideLayout.CHEST).displayItem(profile, item, addToHistory);
	}
}
