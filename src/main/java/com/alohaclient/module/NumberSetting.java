package com.alohaclient.module;

public class NumberSetting extends Setting {
    private double value;
    private final double min, max, step;

    public NumberSetting(String name, double def, double min, double max, double step) {
        super(name);
        this.value = def;
        this.min   = min;
        this.max   = max;
        this.step  = step;
    }

    public double getValue()  { return value; }
    public double getMin()    { return min;   }
    public double getMax()    { return max;   }
    public double getStep()   { return step;  }

    public void increment() { setValue(value + step); }
    public void decrement() { setValue(value - step); }

    public void setValue(double v) {
        this.value = Math.round(Math.max(min, Math.min(max, v)) / step) * step;
    }

    public String getDisplay() {
        if (step == 1.0 || value == (int) value) return String.valueOf((int) value);
        return String.format("%.1f", value);
    }
}
