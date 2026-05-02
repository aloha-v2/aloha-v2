package com.alohaclient.module.movement;

import com.alohaclient.module.Module;
import com.alohaclient.module.NumberSetting;

public class Speed extends Module {
    public final NumberSetting multiplier = new NumberSetting("Multiplier", 1.6, 1.1, 4.0, 0.1);

    public Speed() {
        super("Speed", "Movement");
        addSettings(multiplier);
    }
}
