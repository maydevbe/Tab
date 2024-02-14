package io.github.maydevbe.layout;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.maydevbe.PlayerTab;
import io.github.maydevbe.TabManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.*;

public class TabLayoutUpdater implements Runnable {

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("Tab Thread - %s")
            .build();

    private final TabManager tabManager;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> updater;

    public TabLayoutUpdater(TabManager tabManager) {
        this.tabManager = tabManager;
        Bukkit.getScheduler().runTaskLater(tabManager.getPluginInstanced(), this::initialize, 10L);
    }

    private void initialize() {
        this.executor = Executors.newScheduledThreadPool(1, threadFactory);
        this.updater = this.executor.scheduleAtFixedRate(this, 0L, 200L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerTab tab = tabManager.getTabs().get(player.getUniqueId());
            if (tab == null) continue;

            long currentTime = System.currentTimeMillis();
            if ((currentTime - tab.getStartCurrentMS()) < 1_000L) continue;

            AbstractTabLayout abstractTabLayout = tabManager.getAbstractTabLayout();
            for (TabLayout layout : abstractTabLayout.createLayout(player).getLayouts()) {
                tab.updateTextLine(layout.getTabSlot(), layout.getTextLine());
                tab.updatePing(layout.getTabSlot(), layout.getPing());
                tab.updateSkin(layout.getTabSlot(), layout.getSkinArray());
            }
        }
    }

    public void shutdown() {
        if (this.updater != null) {
            this.updater.cancel(true);
        }
        if (this.executor != null) {
            this.executor.shutdownNow();
        }
    }
}
