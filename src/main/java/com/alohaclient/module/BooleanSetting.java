package com.alohaclient.module;

public class BooleanSetting extends Setting {
    private boolean value;

    public BooleanSetting(String name, boolean def) {
        super(name);
        this.value = def;
    }

    public boolean getValue() { return value; }
    public void toggle()      { value = !value; }
    public void setValue(boolean v) { value = v; }
}
