package com.alohaclient.module;

import com.alohaclient.module.combat.Criticals;
import com.alohaclient.module.combat.KillAura;
import com.alohaclient.module.combat.Velocity;
import com.alohaclient.module.movement.NoFall;
import com.alohaclient.module.movement.Speed;
import com.alohaclient.module.movement.Spider;
import com.alohaclient.module.visual.FullBright;
import com.alohaclient.module.visual.HUD;
import com.alohaclient.module.visual.Trails;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();

    private ModuleManager() {
        register(new KillAura());
        register(new Criticals());
        register(new Velocity());
        register(new Spider());
        register(new Speed());
        register(new NoFall());
        register(new Trails());
        register(new FullBright());
        register(new HUD());
    }

    public static ModuleManager getInstance() { return INSTANCE; }
    public void register(Module m)            { modules.add(m); }
    public List<Module> getModules()          { return modules; }

    public List<Module> getByCategory(String cat) {
        List<Module> res = new ArrayList<>();
        for (Module m : modules) if (m.getCategory().equals(cat)) res.add(m);
        return res;
    }

    public List<String> getCategories() {
        List<String> cats = new ArrayList<>();
        for (Module m : modules) if (!cats.contains(m.getCategory())) cats.add(m.getCategory());
        return cats;
    }

    public boolean isEnabled(String name) {
        for (Module m : modules) if (m.getName().equalsIgnoreCase(name)) return m.isEnabled();
        return false;
    }

    public Module get(String name) {
        for (Module m : modules) if (m.getName().equalsIgnoreCase(name)) return m;
        return null;
    }
}
