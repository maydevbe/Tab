package io.github.maydevbe.layout.impl;

import io.github.maydevbe.layout.AbstractTabLayout;
import io.github.maydevbe.layout.TabLayoutManager;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class TestTabLayout extends AbstractTabLayout {

    @Override
    public TabLayoutManager createLayout(Player player) {
        TabLayoutManager manager = new TabLayoutManager();

        for (int tabSlot = 0; tabSlot < 80; tabSlot++) {
            manager.addSlot(tabSlot, "&bTabSlot: &f" + tabSlot, ThreadLocalRandom.current().nextInt(1, 1000));
        }

        return manager;
    }
}
