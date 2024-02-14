package io.github.maydevbe.layout;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TabLayoutManager {

    private List<TabLayout> layouts = new ArrayList<>();

    public void addSlot(int x, int y, String text, int ping, String value, String signature) {
        addSlot(convertXandYToTabSlot(x, y), text, ping, value, signature);
    }

    public void addSlot(int tabSlot, String text, int ping, String value, String signature) {
        layouts.add(new TabLayout().setTabSlot(tabSlot).setTextLine(text).setPing(ping).setSkinArray(new String[]{value, signature}));
    }

    public void addSlot(int x, int y, String text, int ping) {
        addSlot(convertXandYToTabSlot(x, y), text, ping);
    }

    public void addSlot(int tabSlot, String text, int ping) {
        layouts.add(new TabLayout().setTabSlot(tabSlot).setTextLine(text).setPing(ping));
    }

    public void addSlot(int x, int y, String text, String value, String signature) {
        addSlot(convertXandYToTabSlot(x, y), text, value, signature);
    }

    public void addSlot(int tabSlot, String text, String value, String signature) {
        layouts.add(new TabLayout().setTabSlot(tabSlot).setTextLine(text).setSkinArray(new String[]{value, signature}));
    }

    public void addSlot(int x, int y, String text) {
        addSlot(convertXandYToTabSlot(x, y), text);
    }

    public void addSlot(int tabSlot, String text) {
        layouts.add(new TabLayout().setTabSlot(tabSlot).setTextLine(text));
    }

    public int convertXandYToTabSlot(int x, int y) {
        return y + x * 20;
    }
}
