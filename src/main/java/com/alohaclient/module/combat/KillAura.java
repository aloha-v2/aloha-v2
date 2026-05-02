package com.alohaclient.module.combat;

import com.alohaclient.module.BooleanSetting;
import com.alohaclient.module.Module;
import com.alohaclient.module.NumberSetting;

public class KillAura extends Module {
    public final NumberSetting  range         = new NumberSetting ("Range",      4.0, 1.0,  8.0, 0.5);
    public final NumberSetting  cps           = new NumberSetting ("CPS",       12.0, 1.0, 20.0, 1.0);
    public final BooleanSetting attackPlayers = new BooleanSetting("Players",  false);
    public final BooleanSetting rotations     = new BooleanSetting("Rotations", true);

    public KillAura() {
        super("KillAura", "Combat");
        addSettings(range, cps, attackPlayers, rotations);
    }
}
