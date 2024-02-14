package io.github.maydevbe;

import io.github.maydevbe.layout.AbstractTabLayout;
import io.github.maydevbe.layout.TabLayoutUpdater;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class TabManager {

    private final Map<UUID, PlayerTab> tabs = new ConcurrentHashMap<>();

    private final JavaPlugin pluginInstanced;
    private final AbstractTabLayout abstractTabLayout;
    private final TabLayoutUpdater tabLayoutUpdater;

    public TabManager(JavaPlugin plugin, AbstractTabLayout abstractTabLayout) {
        this.pluginInstanced = plugin;
        this.abstractTabLayout = abstractTabLayout;
        this.tabLayoutUpdater = new TabLayoutUpdater(this);

        plugin.getServer().getPluginManager().registerEvents(new TabListener(this), plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, new TabLayoutUpdater(this), 20L, 20L);
    }

    public void loadTab(Player player) {
        PlayerTab playerTab = new PlayerTab(player);
        tabs.put(player.getUniqueId(), playerTab);

        pluginInstanced.getServer().getScheduler().scheduleSyncDelayedTask(pluginInstanced, playerTab::showTab, 5L);
    }

    public void removeTab(Player player) {
        PlayerTab playerTab = tabs.remove(player.getUniqueId());
        pluginInstanced.getServer().getScheduler().runTaskAsynchronously(pluginInstanced, playerTab::removeTab);
    }
}
