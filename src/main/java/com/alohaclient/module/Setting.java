package com.alohaclient.module;

public abstract class Setting {
    private final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
