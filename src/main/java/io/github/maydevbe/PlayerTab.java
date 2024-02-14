package io.github.maydevbe;

import io.github.maydevbe.layout.TabLayout;
import io.github.maydevbe.reflect.TabReflection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class PlayerTab {

    private final List<TabLayout> layouts = new ArrayList<>();

    private final Player player;
    private final long startCurrentMS;

    public PlayerTab(Player player) {
        this.player = player;
        this.startCurrentMS = System.currentTimeMillis();
    }

    public void removeTab() {
        hideFakePlayers();
        showRealPlayers();

        layouts.clear();
    }

    public void showTab() {
        showFakePlayers();
        hideRealPlayers();
    }

    private void showRealPlayers() {
        for (Player target : Bukkit.getOnlinePlayers()) {
            TabReflection.sendPlayerInfoPacketPlayer(player, target, TabReflection.EnumPlayerInfoAction.ADD_PLAYER);
        }
    }

    private void hideRealPlayers() {
        for (Player target : Bukkit.getOnlinePlayers()) {
            TabReflection.sendPlayerInfoPacketPlayer(player, target, TabReflection.EnumPlayerInfoAction.REMOVE_PLAYER);
        }
    }

    private void showFakePlayers() {
        for (int tabSlot = 0; tabSlot < 80; tabSlot++) {
            TabLayout layout = new TabLayout(UUID.randomUUID(), getTeamName(tabSlot), tabSlot, 0, " ", TabReflection.DARK_GRAY_SKIN_ARRAY);
            layouts.add(layout);

            Object profile = TabReflection.createGameProfileWithProperties(layout.getId(), layout.getName(), layout.getSkinArray());

            TabReflection.sendPlayerInfoPacketData(player, profile, layout.getPing(), layout.getName(), TabReflection.EnumPlayerInfoAction.ADD_PLAYER);
        }
    }

    private void hideFakePlayers() {
        for (TabLayout layout : layouts) {
            TabReflection.sendPlayerInfoPacketData(player, TabReflection.createGameProfile(layout.getId(), layout.getName()), layout.getPing(), layout.getName(), TabReflection.EnumPlayerInfoAction.REMOVE_PLAYER);
        }
    }

    public void updateTextLine(int tabSlot, String textLine) {
        TabLayout layout = getLayoutBySlot(tabSlot);
        if (layout == null) return;
        if (layout.getTextLine().equalsIgnoreCase(textLine)) return;

        TabReflection.sendPlayerInfoPacketData(player, TabReflection.createGameProfileWithProperties(layout.getId(), layout.getName(), layout.getSkinArray()), layout.getPing(), textLine, TabReflection.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME);
        layout.setTextLine(textLine);
    }

    public void updatePing(int tabSlot, int ping) {
        TabLayout layout = getLayoutBySlot(tabSlot);
        if (layout == null) return;
        if (layout.getPing() == ping) return;

        TabReflection.sendPlayerInfoPacketData(player, TabReflection.createGameProfileWithProperties(layout.getId(), layout.getName(), layout.getSkinArray()), ping, layout.getTextLine(), TabReflection.EnumPlayerInfoAction.UPDATE_LATENCY);
        layout.setPing(ping);
    }

    public void updateSkin(int tabSlot, String[] skinArray) {
        TabLayout layout = getLayoutBySlot(tabSlot);
        if (layout == null) return;

        if (layout.equalsSkinArray(skinArray)) return;

        TabReflection.sendPlayerInfoPacketData(player, TabReflection.createGameProfileWithProperties(layout.getId(), layout.getName(), skinArray), layout.getPing(), layout.getTextLine(), TabReflection.EnumPlayerInfoAction.REMOVE_PLAYER);
        TabReflection.sendPlayerInfoPacketData(player, TabReflection.createGameProfileWithProperties(layout.getId(), layout.getName(), skinArray), layout.getPing(), layout.getTextLine(), TabReflection.EnumPlayerInfoAction.ADD_PLAYER);
        layout.setSkinArray(skinArray);
    }

    private String getTeamName(final int valueToFormat) {
        if (valueToFormat >= 10) {
            int firstDigit = valueToFormat / 10;
            int secondDigit = valueToFormat % 10;
            return ChatColor.BOLD + "" + ChatColor.GREEN + ChatColor.UNDERLINE + ChatColor.YELLOW + ChatColor.COLOR_CHAR + firstDigit + ChatColor.COLOR_CHAR + secondDigit;
        } else {
            return ChatColor.BOLD + "" + ChatColor.BLACK + ChatColor.COLOR_CHAR + valueToFormat;
        }
    }

    public TabLayout getLayoutBySlot(int tabSlot) {
        return layouts.stream().filter(tabEntry -> tabEntry.getTabSlot() == tabSlot).findFirst().orElse(null);
    }
}
