package de.project.ae2virtualmine.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class VirtualMineConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue BASE_TICK_INTERVAL;
    public static final ModConfigSpec.DoubleValue ENERGY_PER_DROP;
    public static final ModConfigSpec.BooleanValue REQUIRE_AE_ENERGY;
    public static final ModConfigSpec.BooleanValue ENABLE_BUILTIN_DROPS;

    // Drop rates per tier
    public static final ModConfigSpec.IntValue TIER_1K_DROPS;
    public static final ModConfigSpec.IntValue TIER_4K_DROPS;
    public static final ModConfigSpec.IntValue TIER_16K_DROPS;
    public static final ModConfigSpec.IntValue TIER_64K_DROPS;
    public static final ModConfigSpec.IntValue TIER_256K_DROPS;

    // Cell Capacities (Total Bytes)
    public static final ModConfigSpec.IntValue TIER_1K_BYTES;
    public static final ModConfigSpec.IntValue TIER_4K_BYTES;
    public static final ModConfigSpec.IntValue TIER_16K_BYTES;
    public static final ModConfigSpec.IntValue TIER_64K_BYTES;
    public static final ModConfigSpec.IntValue TIER_256K_BYTES;

    // Bytes per Type
    public static final ModConfigSpec.IntValue TIER_1K_BYTES_PER_TYPE;
    public static final ModConfigSpec.IntValue TIER_4K_BYTES_PER_TYPE;
    public static final ModConfigSpec.IntValue TIER_16K_BYTES_PER_TYPE;
    public static final ModConfigSpec.IntValue TIER_64K_BYTES_PER_TYPE;
    public static final ModConfigSpec.IntValue TIER_256K_BYTES_PER_TYPE;

    // Idle Drain (AE/t)
    public static final ModConfigSpec.DoubleValue TIER_1K_IDLE_DRAIN;
    public static final ModConfigSpec.DoubleValue TIER_4K_IDLE_DRAIN;
    public static final ModConfigSpec.DoubleValue TIER_16K_IDLE_DRAIN;
    public static final ModConfigSpec.DoubleValue TIER_64K_IDLE_DRAIN;
    public static final ModConfigSpec.DoubleValue TIER_256K_IDLE_DRAIN;

    // Max types per cell
    public static final ModConfigSpec.IntValue TOTAL_TYPES;

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

        ENABLE_BUILTIN_DROPS = builder
                .comment("Whether hardcoded built-in drop tables and modded ore tags should be used when no custom datapack recipe exists. If set to false, only datapack recipes will generate drops.")
                .define("enableBuiltinDrops", true);

        builder.pop();

        builder.comment("Tier Drop Amounts (Drops per generation cycle)").push("tiers");

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

        builder.comment("Cell Storage & Electrical Stats").push("cell_stats");

        TOTAL_TYPES = builder
                .comment("Maximum distinct item types that can be stored in any Virtual Mine Cell")
                .defineInRange("totalTypes", 63, 1, 63);

        TIER_1K_BYTES = builder
                .comment("Total byte capacity of 1k Virtual Mine Storage Cell")
                .defineInRange("tier1kBytes", 1024, 1, Integer.MAX_VALUE);
        TIER_4K_BYTES = builder
                .comment("Total byte capacity of 4k Virtual Mine Storage Cell")
                .defineInRange("tier4kBytes", 4096, 1, Integer.MAX_VALUE);
        TIER_16K_BYTES = builder
                .comment("Total byte capacity of 16k Virtual Mine Storage Cell")
                .defineInRange("tier16kBytes", 16384, 1, Integer.MAX_VALUE);
        TIER_64K_BYTES = builder
                .comment("Total byte capacity of 64k Virtual Mine Storage Cell")
                .defineInRange("tier64kBytes", 65536, 1, Integer.MAX_VALUE);
        TIER_256K_BYTES = builder
                .comment("Total byte capacity of 256k Virtual Mine Storage Cell")
                .defineInRange("tier256kBytes", 262144, 1, Integer.MAX_VALUE);

        TIER_1K_BYTES_PER_TYPE = builder
                .comment("Bytes required per distinct item type in 1k cell")
                .defineInRange("tier1kBytesPerType", 8, 1, Integer.MAX_VALUE);
        TIER_4K_BYTES_PER_TYPE = builder
                .comment("Bytes required per distinct item type in 4k cell")
                .defineInRange("tier4kBytesPerType", 32, 1, Integer.MAX_VALUE);
        TIER_16K_BYTES_PER_TYPE = builder
                .comment("Bytes required per distinct item type in 16k cell")
                .defineInRange("tier16kBytesPerType", 128, 1, Integer.MAX_VALUE);
        TIER_64K_BYTES_PER_TYPE = builder
                .comment("Bytes required per distinct item type in 64k cell")
                .defineInRange("tier64kBytesPerType", 512, 1, Integer.MAX_VALUE);
        TIER_256K_BYTES_PER_TYPE = builder
                .comment("Bytes required per distinct item type in 256k cell")
                .defineInRange("tier256kBytesPerType", 2048, 1, Integer.MAX_VALUE);

        TIER_1K_IDLE_DRAIN = builder
                .comment("Passive AE idle power drain (AE/t) for 1k cell")
                .defineInRange("tier1kIdleDrain", 0.5, 0.0, 100000.0);
        TIER_4K_IDLE_DRAIN = builder
                .comment("Passive AE idle power drain (AE/t) for 4k cell")
                .defineInRange("tier4kIdleDrain", 1.0, 0.0, 100000.0);
        TIER_16K_IDLE_DRAIN = builder
                .comment("Passive AE idle power drain (AE/t) for 16k cell")
                .defineInRange("tier16kIdleDrain", 2.0, 0.0, 100000.0);
        TIER_64K_IDLE_DRAIN = builder
                .comment("Passive AE idle power drain (AE/t) for 64k cell")
                .defineInRange("tier64kIdleDrain", 4.0, 0.0, 100000.0);
        TIER_256K_IDLE_DRAIN = builder
                .comment("Passive AE idle power drain (AE/t) for 256k cell")
                .defineInRange("tier256kIdleDrain", 8.0, 0.0, 100000.0);

        builder.pop();

        SPEC = builder.build();
    }
}
