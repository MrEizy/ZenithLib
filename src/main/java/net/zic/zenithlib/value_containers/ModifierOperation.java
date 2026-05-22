package net.zic.zenithlib.value_containers;

public enum ModifierOperation {
    MULTIPLY_BASE, // total += base*(1+val)
    ADD_BASE,
    MULTIPLY_FINAL,
    ADD_FINAL, // total += val;
}
