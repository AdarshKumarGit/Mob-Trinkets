package org.chubby.github.mobtrinkets.trinket;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum TrinketSkill {
    POLLEN_DASH("pollen_dash", 20),
    HONEY_SHIELD("honey_shield", 45),
    BONE_ARMOR("bone_armor", 30),
    RATTLE("rattle", 40),
    RAM_CHARGE("ram_charge", 18),
    HORN_BLAST("horn_blast", 40),
    TIDAL_SURGE("tidal_surge", 25),
    RIPTIDE_BURST("riptide_burst", 30),
    SLIME_BOUNCE("slime_bounce", 15),
    SLIME_WAVE("slime_wave", 35),
    GLIDE("glide", 20),
    HAUNT("haunt", 45),
    PRISMATIC_BEAM("prismatic_beam", 20),
    GUARDIANS_WARD("guardians_ward", 60),
    VOLATILE_BURST("volatile_burst", 25),
    CHAIN_DETONATION("chain_detonation", 50),
    FLAME_LASH("flame_lash", 15),
    INFERNO_RING("inferno_ring", 40),
    VENOM_STRIKE("venom_strike", 15),
    SILK_SNARE("silk_snare", 35),
    MAGMA_SHIELD("magma_shield", 45),
    ERUPTION("eruption", 40),
    VOID_PULL("void_pull", 20),
    PHASE_SHIFT("phase_shift", 45);

    private final String key;
    private final int cooldownSeconds;

    TrinketSkill(String key, int cooldownSeconds) {
        this.key = key;
        this.cooldownSeconds = cooldownSeconds;
    }

    public int cooldownSeconds() {
        return cooldownSeconds;
    }

    public MutableComponent displayName() {
        return Component.translatable("skill.mobtrinkets." + key);
    }

    public MutableComponent description() {
        return Component.translatable("skill.mobtrinkets." + key + ".desc");
    }
}
