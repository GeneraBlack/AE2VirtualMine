package de.project.ae2virtualmine.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class VirtualMineConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue BASE_TICK_INTERVAL;
    public static final ModConfigSpec.DoubleValue ENERGY_PER_DROP;
    public static final ModConfigSpec.BooleanValue REQUIRE_AE_ENERGY;
    public static final ModConfigSpec.BooleanValue ENABLE_BUILTIN_DROPS;

    public static final ModConfigSpec.IntValue TIER_1K_DROPS;
    public static final ModConfigSpec.IntValue TIER_4K_DROPS;
    public static final ModConfigSpec.IntValue TIER_16K_DROPS;
    public static final ModConfigSpec.IntValue TIER_64K_DROPS;
    public static final ModConfigSpec.IntValue TIER_256K_DROPS;

    public static final ModConfigSpec.IntValue TOTAL_TYPES;
    public static final ModConfigSpec.IntValue TIER_1K_BYTES;
    public static final ModConfigSpec.IntValue TIER_4K_BYTES;
    public static final ModConfigSpec.IntValue TIER_16K_BYTES;
    public static final ModConfigSpec.IntValue TIER_64K_BYTES;
    public static final ModConfigSpec.IntValue TIER_256K_BYTES;

    public static final ModConfigSpec.IntValue TIER_1K_BYTES_PER_TYPE;
    public static final ModConfigSpec.IntValue TIER_4K_BYTES_PER_TYPE;
    public static final ModConfigSpec.IntValue TIER_16K_BYTES_PER_TYPE;
    public static final ModConfigSpec.IntValue TIER_64K_BYTES_PER_TYPE;
    public static final ModConfigSpec.IntValue TIER_256K_BYTES_PER_TYPE;

    public static final ModConfigSpec.DoubleValue TIER_1K_IDLE_DRAIN;
    public static final ModConfigSpec.DoubleValue TIER_4K_IDLE_DRAIN;
    public static final ModConfigSpec.DoubleValue TIER_16K_IDLE_DRAIN;
    public static final ModConfigSpec.DoubleValue TIER_64K_IDLE_DRAIN;
    public static final ModConfigSpec.DoubleValue TIER_256K_IDLE_DRAIN;

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
                .comment("Whether to enable built-in drop tables and automatic ore-tag drop generation as fallback. Set false to only use datapack recipes.")
                .define("enableBuiltinDrops", true);

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

        builder.comment("Cell Capacities and AE Power Drain").push("cell_stats");

        TOTAL_TYPES = builder
                .comment("Maximum distinct item types that can be stored in a Virtual Mine Storage Cell (default 63)")
                .defineInRange("totalTypes", 63, 1, 63);

        TIER_1K_BYTES = builder
                .comment("Total storage byte capacity for 1k cell (default 1024)")
                .defineInRange("tier1kBytes", 1024, 1, 100_000_000);
        TIER_4K_BYTES = builder
                .comment("Total storage byte capacity for 4k cell (default 4096)")
                .defineInRange("tier4kBytes", 4096, 1, 100_000_000);
        TIER_16K_BYTES = builder
                .comment("Total storage byte capacity for 16k cell (default 16384)")
                .defineInRange("tier16kBytes", 16384, 1, 100_000_000);
        TIER_64K_BYTES = builder
                .comment("Total storage byte capacity for 64k cell (default 65536)")
                .defineInRange("tier64kBytes", 65536, 1, 100_000_000);
        TIER_256K_BYTES = builder
                .comment("Total storage byte capacity for 256k cell (default 262144)")
                .defineInRange("tier256kBytes", 262144, 1, 100_000_000);

        TIER_1K_BYTES_PER_TYPE = builder
                .comment("Bytes required per item type for 1k cell (default 8)")
                .defineInRange("tier1kBytesPerType", 8, 1, 100_000);
        TIER_4K_BYTES_PER_TYPE = builder
                .comment("Bytes required per item type for 4k cell (default 32)")
                .defineInRange("tier4kBytesPerType", 32, 1, 100_000);
        TIER_16K_BYTES_PER_TYPE = builder
                .comment("Bytes required per item type for 16k cell (default 128)")
                .defineInRange("tier16kBytesPerType", 128, 1, 100_000);
        TIER_64K_BYTES_PER_TYPE = builder
                .comment("Bytes required per item type for 64k cell (default 512)")
                .defineInRange("tier64kBytesPerType", 512, 1, 100_000);
        TIER_256K_BYTES_PER_TYPE = builder
                .comment("Bytes required per item type for 256k cell (default 2048)")
                .defineInRange("tier256kBytesPerType", 2048, 1, 100_000);

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
