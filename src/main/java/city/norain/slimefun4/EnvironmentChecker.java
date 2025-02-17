package city.norain.slimefun4;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;

class EnvironmentChecker {
    static {
        if (Bukkit.getPluginManager().getPlugin("MiraiMC") != null) {
            System.setProperty("MiraiMC.StandWithNpp", "true");
            var logger = Logger.getLogger("Slimefun");
            logger.log(Level.WARNING, "检测到 MiraiMC, 已自动添加参数防止服务器被恶意阻塞");
            logger.log(Level.WARNING, "我用 Notepad++ 怎么你了?");
        }
    }

    private static final List<String> UNSUPPORTED_PLUGINS = List.of("BedrockTechnology", "SlimefunFix", "SlimefunBugFixer", "Slimefunbookfix", "MiraiMC");

    static boolean checkIncompatiblePlugins(@Nonnull Slimefun sf, @Nonnull Logger logger) {
        for (String name : UNSUPPORTED_PLUGINS) {
            if (sf.getServer().getPluginManager().getPlugin(name) != null) {
                logger.log(Level.WARNING, "检测到安装了 {0}, 该插件已不再兼容新版 Slimefun, 可能会带来不良效果!", name);
                Bukkit.getPluginManager().disablePlugin(sf);
                return true;
            }
        }

        return false;
    }

    static boolean checkHybridServer(@Nonnull Slimefun sf, @Nonnull Logger logger) {
        try {
            Class.forName("net.minecraftforge.common.MinecraftForge");
            logger.log(Level.WARNING, "检测到正在使用混合端, Slimefun 将会被禁用!");
            Bukkit.getPluginManager().disablePlugin(sf);

            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    static void scheduleSlimeGlueCheck(@Nonnull Slimefun sf, @Nonnull Logger logger) {
        Bukkit.getScheduler().runTaskLater(sf, () -> {
            if (Bukkit.getPluginManager().getPlugin("SlimeGlue") == null) {
                logger.log(Level.WARNING, "检测到没有安装 SlimeGlue (粘液胶), 你将缺失对一些插件的额外保护检查!");
                logger.log(Level.WARNING, "下载: https://github.com/Xzavier0722/SlimeGlue");
            }
        }, 300); // 15s
    }
}
