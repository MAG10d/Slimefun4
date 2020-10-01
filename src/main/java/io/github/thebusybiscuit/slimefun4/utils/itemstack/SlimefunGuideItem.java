package io.github.thebusybiscuit.slimefun4.utils.itemstack;

import io.github.thebusybiscuit.cscorelib2.chat.ChatColors;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideImplementation;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import io.github.thebusybiscuit.slimefun4.implementation.guide.CheatSheetSlimefunGuide;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

/**
 * This is just a helper {@link ItemStack} class for the {@link SlimefunGuide} {@link ItemStack}.
 *
 * @author TheBusyBiscuit
 * @see SlimefunGuide
 * @see SlimefunGuideImplementation
 */
public class SlimefunGuideItem extends ItemStack {

    public SlimefunGuideItem(@Nonnull SlimefunGuideImplementation implementation, @Nonnull String name) {
        super(Material.ENCHANTED_BOOK);

        ItemMeta meta = getItemMeta();

        meta.setDisplayName(ChatColors.color(name));

        List<String> lore = new LinkedList<>();

        lore.add(implementation instanceof CheatSheetSlimefunGuide ? ChatColors.color("&4&l仅限管理员使用") : "");
        lore.add(ChatColors.color("&e右键 &8\u21E8 &7浏览物品"));
        lore.add(ChatColors.color("&eShift + 右键 &8\u21E8 &7打开 设置 / 关于"));

        meta.setLore(lore);
        SlimefunPlugin.getItemTextureService().setTexture(meta, "SLIMEFUN_GUIDE");

        setItemMeta(meta);
    }

}