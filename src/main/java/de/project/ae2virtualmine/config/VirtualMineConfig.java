package de.project.ae2virtualmine.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class VirtualMineConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue BASE_TICK_INTERVAL;
    public static final ModConfigSpec.DoubleValue ENERGY_PER_DROP;
    public static final ModConfigSpec.BooleanValue REQUIRE_AE_ENERGY;

    public static final ModConfigSpec.IntValue TIER_1K_DROPS;
    public static final ModConfigSpec.IntValue TIER_4K_DROPS;
    public static final ModConfigSpec.IntValue TIER_16K_DROPS;
    public static final ModConfigSpec.IntValue TIER_64K_DROPS;
    public static final ModConfigSpec.IntValue TIER_256K_DROPS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("General Virtual Mine Settings").push("general");

        BASE_TICK_INTERVAL = builder
                .comment("Interval in world ticks between mining drop generation cycles (20 ticks = 1 second, default 60 = 3 seconds)")
                .defineInRange("baseTickInterval", 60, 1, 72000);

        REQUIRE_AE_ENERGY = builder
                .comment("Whether generating mining drops requires AE energy from the ME Network")
                .define("requireAeEnergy", true);

        ENERGY_PER_DROP = builder
                .comment("AE energy consumed per generated item drop")
                .defineInRange("energyPerDrop", 10.0, 0.0, 100000.0);

        builder.pop();

        builder.comment("Tier Drop Amounts").push("tiers");

        TIER_1K_DROPS = builder
                .comment("Number of drops per cycle for 1k Virtual Mine Storage Cell")
                .defineInRange("tier1kDrops", 1, 1, 64);

        TIER_4K_DROPS = builder
                .comment("Number of drops per cycle for 4k Virtual Mine Storage Cell")
                .defineInRange("tier4kDrops", 4, 1, 256);

        TIER_16K_DROPS = builder
                .comment("Number of drops per cycle for 16k Virtual Mine Storage Cell")
                .defineInRange("tier16kDrops", 16, 1, 1024);

        TIER_64K_DROPS = builder
                .comment("Number of drops per cycle for 64k Virtual Mine Storage Cell")
                .defineInRange("tier64kDrops", 64, 1, 4096);

        TIER_256K_DROPS = builder
                .comment("Number of drops per cycle for 256k Virtual Mine Storage Cell")
                .defineInRange("tier256kDrops", 256, 1, 16384);

        builder.pop();

        SPEC = builder.build();
    }
}
