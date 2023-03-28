package io.github.thebusybiscuit.slimefun4.core.config;

import io.github.bakedlibs.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSetting;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;

public class SlimefunConfigManager {
    /**
     * Slimefun plugin instance
     */
    private final Slimefun plugin;

    /**
     * Hold plugin config named "config.yml"
     */
    private final Config pluginConfig;

    /**
     * Hold item config named "Items.yml"
     */
    private final Config itemsConfig;

    /**
     * Hold research config named "Researches.yml"
     */
    private final Config researchesConfig;

    private boolean backwardsCompatibility;
    private boolean automaticallyLoadItems;
    private boolean enableResearches;
    private boolean freeCreativeResearches;
    private boolean researchFireworks;
    private boolean disableLearningAnimation;
    private boolean logDuplicateBlockEntries;
    private boolean talismanActionBarMessages;
    private boolean useMoneyUnlock;
    private boolean showVanillaRecipes;
    private boolean showHiddenItemGroupsInSearch;
    private boolean autoUpdate;
    private double researchConvertMultiplier;

    public SlimefunConfigManager(@Nonnull Slimefun plugin) {
        Validate.notNull(plugin, "The Plugin instance cannot be null");

        this.plugin = plugin;
        pluginConfig = getConfig(plugin, "config", () -> new Config(plugin));
        itemsConfig = getConfig(plugin, "Items", () -> new Config(plugin, "Items.yml"));
        researchesConfig = getConfig(plugin, "Researches", () -> new Config(plugin, "Researches.yml"));
    }

    @Nullable
    @ParametersAreNonnullByDefault
    private Config getConfig(Slimefun plugin, String name, Supplier<Config> supplier) {
        try {
            return supplier.get();
        } catch (Exception x) {
            plugin.getLogger().log(Level.SEVERE, x, () -> "An Exception was thrown while loading the config file \"" + name + ".yml\" for Slimefun v" + plugin.getDescription().getVersion());
            return null;
        }
    }

    public boolean reload() {
        boolean isSuccessful = true;

        try {
            pluginConfig.reload();
            itemsConfig.reload();
            researchesConfig.reload();

            researchesConfig.setDefaultValue("enable-researching", true);
            backwardsCompatibility = pluginConfig.getBoolean("options.backwards-compatibility");
            enableResearches = researchesConfig.getBoolean("enable-researching");
            freeCreativeResearches = pluginConfig.getBoolean("researches.free-in-creative-mode");
            researchFireworks = pluginConfig.getBoolean("researches.enable-fireworks");
            disableLearningAnimation = pluginConfig.getBoolean("researches.disable-learning-animation");
            logDuplicateBlockEntries = pluginConfig.getBoolean("options.log-duplicate-block-entries");
            talismanActionBarMessages = pluginConfig.getBoolean("talismans.use-actionbar");
            useMoneyUnlock = pluginConfig.getBoolean("researches.use-money-unlock");
            showVanillaRecipes = pluginConfig.getBoolean("guide.show-vanilla-recipes");
            showHiddenItemGroupsInSearch = pluginConfig.getBoolean("guide.show-hidden-item-groups-in-search");
            autoUpdate = pluginConfig.getBoolean("options.auto-update");

            researchConvertMultiplier = researchesConfig.getDouble("researches.money-multiply");
        } catch (Exception x) {
            plugin.getLogger().log(Level.SEVERE, x, () -> "An Exception was caught while (re)loading the config files for Slimefun v" + plugin.getDescription().getVersion());
            isSuccessful = false;
        }

        // Reload Research costs
        for (Research research : Slimefun.getRegistry().getResearches()) {
            try {
                NamespacedKey key = research.getKey();
                int cost = researchesConfig.getInt(key.getNamespace() + '.' + key.getKey() + ".cost");
                research.setLevelCost(cost);
                var status = researchesConfig.getBoolean(key.getNamespace() + '.' + key.getKey() + ".enabled");

                if (research.isEnabled() != status) {
                    if (status) {
                        research.register();
                    } else {
                        research.disable();
                    }
                }
            } catch (Exception x) {
                plugin.getLogger().log(Level.SEVERE, x, () -> "Something went wrong while trying to update the cost of a research: " + research);
                isSuccessful = false;
            }
        }

        for (SlimefunItem item : Slimefun.getRegistry().getAllSlimefunItems()) {
            var itemStatus = itemsConfig.getBoolean(item.getId() + ".enabled");
            if (item.isDisabled() != !itemStatus && item.getAddon() != null) {
                if (itemStatus && SlimefunItem.getById(item.getId()) == null) {
                    item.register(item.getAddon());
                } else {
                    Slimefun.logger().log(Level.WARNING, "物品禁用暂时不支持热重载, 请手动重启服务器.");
                    isSuccessful = false;
                }
            }

            // Reload Item Settings
            try {
                for (ItemSetting<?> setting : item.getItemSettings()) {
                    setting.reload();
                }
            } catch (Exception x) {
                item.error("Something went wrong while updating the settings for this item!", x);
                isSuccessful = false;
            }

            // Reload permissions
            try {
                Slimefun.getPermissionsService().update(item, false);
            } catch (Exception x) {
                item.error("Something went wrong while updating the permission node for this item!", x);
                isSuccessful = false;
            }
        }

        return isSuccessful;
    }

    public Config getPluginConfig() {
        return pluginConfig;
    }

    /**
     * This returns whether auto-loading is enabled.
     * Auto-Loading will automatically call {@link SlimefunItem#load()} when the item is registered.
     * Normally that method is called after the {@link Server} finished starting up.
     * But in the unusual scenario if a {@link SlimefunItem} is registered after that, this is gonna cover that.
     *
     * @return Whether auto-loading is enabled
     */
    public boolean isAutoLoadingEnabled() {
        return automaticallyLoadItems;
    }

    /**
     * This method returns whether backwards-compatibility is enabled.
     * Backwards compatibility allows Slimefun to recognize items from older versions but comes
     * at a huge performance cost.
     *
     * @return Whether backwards compatibility is enabled
     */
    public boolean isBackwardsCompatible() {
        return backwardsCompatibility;
    }

    /**
     * This method sets the status of backwards compatibility.
     * Backwards compatibility allows Slimefun to recognize items from older versions but comes
     * at a huge performance cost.
     *
     * @param compatible Whether backwards compatibility should be enabled
     */
    public void setBackwardsCompatible(boolean compatible) {
        backwardsCompatibility = compatible;
    }

    /**
     * This method will make any {@link SlimefunItem} which is registered automatically
     * call {@link SlimefunItem#load()}.
     * Normally this method call is delayed but when the {@link Server} is already running,
     * the method can be called instantaneously.
     *
     * @param mode Whether auto-loading should be enabled
     */
    public void setAutoLoadingMode(boolean mode) {
        automaticallyLoadItems = mode;
    }

    public void setResearchingEnabled(boolean enabled) {
        enableResearches = enabled;
    }

    public boolean isResearchingEnabled() {
        return enableResearches;
    }

    public void setFreeCreativeResearchingEnabled(boolean enabled) {
        freeCreativeResearches = enabled;
    }

    public boolean isFreeCreativeResearchingEnabled() {
        return freeCreativeResearches;
    }

    public boolean isResearchFireworkEnabled() {
        return researchFireworks;
    }

    /**
     * Returns whether the research learning animations is disabled
     *
     * @return Whether the research learning animations is disabled
     */
    public boolean isLearningAnimationDisabled() {
        return disableLearningAnimation;
    }

    public boolean logDuplicateBlockEntries() {
        return logDuplicateBlockEntries;
    }

    public boolean useActionbarForTalismans() {
        return talismanActionBarMessages;
    }

    public boolean isUseMoneyUnlock() {
        return useMoneyUnlock;
    }

    public void setShowVanillaRecipes(boolean enabled) {
        showVanillaRecipes = enabled;
    }

    public boolean isShowVanillaRecipes() {
        return showVanillaRecipes;
    }

    public void setShowHiddenItemGroupsInSearch(boolean enabled) {
        showHiddenItemGroupsInSearch = enabled;
    }

    public boolean isShowHiddenItemGroupsInSearch() {
        return showHiddenItemGroupsInSearch;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public double getResearchConvertMultiplier() {
        return researchConvertMultiplier;
    }
}
