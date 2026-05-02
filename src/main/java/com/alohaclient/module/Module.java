package com.alohaclient.module;

import java.util.ArrayList;
import java.util.List;

public class Module {
    private final String name;
    private final String category;
    private boolean enabled = false;
    private final List<Setting> settings = new ArrayList<>();

    public Module(String name, String category) {
        this.name     = name;
        this.category = category;
    }

    protected void addSettings(Setting... s) {
        for (Setting set : s) settings.add(set);
    }

    public String  getName()      { return name;     }
    public String  getCategory()  { return category; }
    public boolean isEnabled()    { return enabled;  }
    public List<Setting> getSettings() { return settings; }
    public boolean hasSettings()  { return !settings.isEmpty(); }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    protected void onEnable()  {}
    protected void onDisable() {}
}
