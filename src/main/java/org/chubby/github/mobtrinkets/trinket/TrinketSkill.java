package org.chubby.github.mobtrinkets.trinket;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.chubby.github.mobtrinkets.handler.SkillEffects;

public enum TrinketSkill {
    POLLEN_DASH("pollen_dash", 20, SkillEffects::pollenDash),
    HONEY_SHIELD("honey_shield", 45, SkillEffects::honeyShield),
    BONE_ARMOR("bone_armor", 30, SkillEffects::boneArmor),
    RATTLE("rattle", 40, SkillEffects::rattle),
    RAM_CHARGE("ram_charge", 18, SkillEffects::ramCharge),
    HORN_BLAST("horn_blast", 40, SkillEffects::hornBlast),
    TIDAL_SURGE("tidal_surge", 25, SkillEffects::tidalSurge),
    RIPTIDE_BURST("riptide_burst", 30, SkillEffects::riptideBurst),
    SLIME_BOUNCE("slime_bounce", 15, SkillEffects::slimeBounce),
    SLIME_WAVE("slime_wave", 35, SkillEffects::slimeWave),
    GLIDE("glide", 20, SkillEffects::glide),
    HAUNT("haunt", 45, SkillEffects::haunt),
    PRISMATIC_BEAM("prismatic_beam", 20, SkillEffects::prismaticBeam),
    GUARDIANS_WARD("guardians_ward", 60, SkillEffects::guardiansWard),
    VOLATILE_BURST("volatile_burst", 25, SkillEffects::volatileBurst),
    CHAIN_DETONATION("chain_detonation", 50, SkillEffects::chainDetonation),
    FLAME_LASH("flame_lash", 15, SkillEffects::flameLash),
    INFERNO_RING("inferno_ring", 40, SkillEffects::infernoRing),
    VENOM_STRIKE("venom_strike", 15, SkillEffects::venomStrike),
    SILK_SNARE("silk_snare", 35, SkillEffects::silkSnare),
    MAGMA_SHIELD("magma_shield", 45, SkillEffects::magmaShield),
    ERUPTION("eruption", 40, SkillEffects::eruption),
    VOID_PULL("void_pull", 20, SkillEffects::voidPull),
    PHASE_SHIFT("phase_shift", 45, SkillEffects::phaseShift);

    @FunctionalInterface
    public interface Action {
        boolean run(ServerPlayer player);
    }

    private final String key;
    private final int cooldownSeconds;
    private final Action action;

    TrinketSkill(String key, int cooldownSeconds, Action action) {
        this.key = key;
        this.cooldownSeconds = cooldownSeconds;
        this.action = action;
    }

    public int cooldownSeconds() {
        return cooldownSeconds;
    }

    public boolean activate(ServerPlayer player) {
        return action.run(player);
    }

    public MutableComponent displayName() {
        return Component.translatable("skill.mobtrinkets." + key);
    }

    public MutableComponent description() {
        return Component.translatable("skill.mobtrinkets." + key + ".desc");
    }
}
