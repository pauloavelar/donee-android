package me.avelar.donee.model;

public class NavDrawerItem {

    public static final int NO_ICON = 0;

    public int     icon;
    public String  title;
    public int     counter;
    public boolean isHeader;

    public NavDrawerItem(String title, int icon) {
        this(title, icon, false);
    }

    public NavDrawerItem(String title, int icon, boolean header) {
        this(title, icon, header, 0);
    }

    public NavDrawerItem(String title, int icon, int counter) {
        this(title, icon, false, counter);
    }

    private NavDrawerItem(String title, int icon, boolean header, int counter) {
        this.title    = title;
        this.icon     = icon;
        this.isHeader = header;
        this.counter  = counter;
    }

}
