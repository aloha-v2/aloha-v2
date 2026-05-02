package com.alohaclient.module.combat;

import com.alohaclient.module.Module;
import com.alohaclient.module.NumberSetting;

public class Velocity extends Module {
    public final NumberSetting mult = new NumberSetting("Multiplier", 0.0, 0.0, 1.0, 0.1);

    public Velocity() {
        super("Velocity", "Combat");
        addSettings(mult);
    }
}
