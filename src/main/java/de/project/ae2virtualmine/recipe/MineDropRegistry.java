package de.project.ae2virtualmine.recipe;

import de.project.ae2virtualmine.registry.ModRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;

import java.util.*;

public class MineDropRegistry {

    private static final Map<Item, List<MineDropEntry>> BUILTIN_DROPS = new HashMap<>();
    private static final Map<Item, List<MineDropEntry>> DYNAMIC_CACHE = new HashMap<>();

    // Common NeoForge tags for mining resources
    private static final TagKey<Item> C_ORES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "ores"));
    private static final TagKey<Item> C_RAW_MATERIALS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "raw_materials"));
    private static final TagKey<Item> C_GEMS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "gems"));
    private static final TagKey<Item> C_DUSTS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "dusts"));
    private static final TagKey<Item> C_STONES = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "stones"));

    static {
        registerOreDefaults();
        registerStoneDefaults();
    }

    private static void registerOreDefaults() {
        // Iron
        List<MineDropEntry> ironDrops = List.of(
                new MineDropEntry(new ItemStack(Items.RAW_IRON), 75, 1, 1),
                new MineDropEntry(new ItemStack(Items.COBBLESTONE), 15, 1, 2),
                new MineDropEntry(new ItemStack(Items.GRAVEL), 10, 1, 1)
        );
        addBuiltin(Items.RAW_IRON, ironDrops);
        addBuiltin(Items.IRON_ORE, ironDrops);
        addBuiltin(Items.DEEPSLATE_IRON_ORE, List.of(
                new MineDropEntry(new ItemStack(Items.RAW_IRON), 75, 1, 1),
                new MineDropEntry(new ItemStack(Items.COBBLED_DEEPSLATE), 15, 1, 2),
                new MineDropEntry(new ItemStack(Items.GRAVEL), 10, 1, 1)
        ));

        // Copper
        List<MineDropEntry> copperDrops = List.of(
                new MineDropEntry(new ItemStack(Items.RAW_COPPER), 75, 1, 2),
                new MineDropEntry(new ItemStack(Items.COBBLESTONE), 15, 1, 1),
                new MineDropEntry(new ItemStack(Items.GRANITE), 10, 1, 1)
        );
        addBuiltin(Items.RAW_COPPER, copperDrops);
        addBuiltin(Items.COPPER_ORE, copperDrops);
        addBuiltin(Items.DEEPSLATE_COPPER_ORE, copperDrops);

        // Gold
        List<MineDropEntry> goldDrops = List.of(
                new MineDropEntry(new ItemStack(Items.RAW_GOLD), 70, 1, 1),
                new MineDropEntry(new ItemStack(Items.COBBLESTONE), 15, 1, 1),
                new MineDropEntry(new ItemStack(Items.QUARTZ), 15, 1, 1)
        );
        addBuiltin(Items.RAW_GOLD, goldDrops);
        addBuiltin(Items.GOLD_ORE, goldDrops);
        addBuiltin(Items.DEEPSLATE_GOLD_ORE, goldDrops);
        addBuiltin(Items.NETHER_GOLD_ORE, List.of(
                new MineDropEntry(new ItemStack(Items.GOLD_NUGGET), 75, 2, 6),
                new MineDropEntry(new ItemStack(Items.NETHERRACK), 25, 1, 2)
        ));

        // Coal
        List<MineDropEntry> coalDrops = List.of(
                new MineDropEntry(new ItemStack(Items.COAL), 85, 1, 2),
                new MineDropEntry(new ItemStack(Items.COBBLESTONE), 15, 1, 1)
        );
        addBuiltin(Items.COAL, coalDrops);
        addBuiltin(Items.COAL_ORE, coalDrops);
        addBuiltin(Items.DEEPSLATE_COAL_ORE, coalDrops);

        // Redstone
        List<MineDropEntry> redstoneDrops = List.of(
                new MineDropEntry(new ItemStack(Items.REDSTONE), 80, 1, 3),
                new MineDropEntry(new ItemStack(Items.COBBLESTONE), 15, 1, 1),
                new MineDropEntry(new ItemStack(Items.GLOWSTONE_DUST), 5, 1, 1)
        );
        addBuiltin(Items.REDSTONE, redstoneDrops);
        addBuiltin(Items.REDSTONE_ORE, redstoneDrops);
        addBuiltin(Items.DEEPSLATE_REDSTONE_ORE, redstoneDrops);

        // Lapis Lazuli
        List<MineDropEntry> lapisDrops = List.of(
                new MineDropEntry(new ItemStack(Items.LAPIS_LAZULI), 80, 1, 3),
                new MineDropEntry(new ItemStack(Items.COBBLESTONE), 20, 1, 1)
        );
        addBuiltin(Items.LAPIS_LAZULI, lapisDrops);
        addBuiltin(Items.LAPIS_ORE, lapisDrops);
        addBuiltin(Items.DEEPSLATE_LAPIS_ORE, lapisDrops);

        // Diamond
        List<MineDropEntry> diamondDrops = List.of(
                new MineDropEntry(new ItemStack(Items.DIAMOND), 60, 1, 1),
                new MineDropEntry(new ItemStack(Items.COBBLED_DEEPSLATE), 25, 1, 1),
                new MineDropEntry(new ItemStack(Items.COBBLESTONE), 15, 1, 1)
        );
        addBuiltin(Items.DIAMOND, diamondDrops);
        addBuiltin(Items.DIAMOND_ORE, diamondDrops);
        addBuiltin(Items.DEEPSLATE_DIAMOND_ORE, diamondDrops);

        // Emerald
        List<MineDropEntry> emeraldDrops = List.of(
                new MineDropEntry(new ItemStack(Items.EMERALD), 60, 1, 1),
                new MineDropEntry(new ItemStack(Items.STONE), 25, 1, 1),
                new MineDropEntry(new ItemStack(Items.DIORITE), 15, 1, 1)
        );
        addBuiltin(Items.EMERALD, emeraldDrops);
        addBuiltin(Items.EMERALD_ORE, emeraldDrops);
        addBuiltin(Items.DEEPSLATE_EMERALD_ORE, emeraldDrops);

        // Ancient Debris
        addBuiltin(Items.ANCIENT_DEBRIS, List.of(
                new MineDropEntry(new ItemStack(Items.ANCIENT_DEBRIS), 40, 1, 1),
                new MineDropEntry(new ItemStack(Items.NETHERRACK), 40, 1, 2),
                new MineDropEntry(new ItemStack(Items.BASALT), 10, 1, 1),
                new MineDropEntry(new ItemStack(Items.BLACKSTONE), 10, 1, 1)
        ));

        // Nether Quartz
        List<MineDropEntry> quartzDrops = List.of(
                new MineDropEntry(new ItemStack(Items.QUARTZ), 80, 1, 2),
                new MineDropEntry(new ItemStack(Items.NETHERRACK), 20, 1, 1)
        );
        addBuiltin(Items.QUARTZ, quartzDrops);
        addBuiltin(Items.NETHER_QUARTZ_ORE, quartzDrops);

        // Glowstone
        List<MineDropEntry> glowstoneDrops = List.of(
                new MineDropEntry(new ItemStack(Items.GLOWSTONE_DUST), 80, 1, 2),
                new MineDropEntry(new ItemStack(Items.NETHERRACK), 20, 1, 1)
        );
        addBuiltin(Items.GLOWSTONE, glowstoneDrops);
        addBuiltin(Items.GLOWSTONE_DUST, glowstoneDrops);

        // Amethyst
        List<MineDropEntry> amethystDrops = List.of(
                new MineDropEntry(new ItemStack(Items.AMETHYST_SHARD), 75, 1, 2),
                new MineDropEntry(new ItemStack(Items.CALCITE), 15, 1, 1),
                new MineDropEntry(new ItemStack(Items.SMOOTH_BASALT), 10, 1, 1)
        );
        addBuiltin(Items.AMETHYST_SHARD, amethystDrops);
        addBuiltin(Items.AMETHYST_CLUSTER, amethystDrops);
        addBuiltin(Items.AMETHYST_BLOCK, amethystDrops);
    }

    private static void registerStoneDefaults() {
        // Stone & Cobblestone
        List<MineDropEntry> stoneDrops = List.of(
                new MineDropEntry(new ItemStack(Items.COBBLESTONE), 75, 1, 2),
                new MineDropEntry(new ItemStack(Items.GRAVEL), 15, 1, 1),
                new MineDropEntry(new ItemStack(Items.FLINT), 10, 1, 1)
        );
        addBuiltin(Items.COBBLESTONE, stoneDrops);
        addBuiltin(Items.STONE, stoneDrops);
        addBuiltin(Items.SMOOTH_STONE, stoneDrops);

        // Deepslate
        List<MineDropEntry> deepslateDrops = List.of(
                new MineDropEntry(new ItemStack(Items.COBBLED_DEEPSLATE), 75, 1, 2),
                new MineDropEntry(new ItemStack(Items.TUFF), 15, 1, 1),
                new MineDropEntry(new ItemStack(Items.FLINT), 10, 1, 1)
        );
        addBuiltin(Items.DEEPSLATE, deepslateDrops);
        addBuiltin(Items.COBBLED_DEEPSLATE, deepslateDrops);

        // Variants
        addBuiltin(Items.GRANITE, List.of(new MineDropEntry(new ItemStack(Items.GRANITE), 80, 1, 2), new MineDropEntry(new ItemStack(Items.COBBLESTONE), 20, 1, 1)));
        addBuiltin(Items.DIORITE, List.of(new MineDropEntry(new ItemStack(Items.DIORITE), 80, 1, 2), new MineDropEntry(new ItemStack(Items.COBBLESTONE), 20, 1, 1)));
        addBuiltin(Items.ANDESITE, List.of(new MineDropEntry(new ItemStack(Items.ANDESITE), 80, 1, 2), new MineDropEntry(new ItemStack(Items.COBBLESTONE), 20, 1, 1)));
        addBuiltin(Items.TUFF, List.of(new MineDropEntry(new ItemStack(Items.TUFF), 80, 1, 2), new MineDropEntry(new ItemStack(Items.COBBLED_DEEPSLATE), 20, 1, 1)));
        addBuiltin(Items.CALCITE, List.of(new MineDropEntry(new ItemStack(Items.CALCITE), 80, 1, 2), new MineDropEntry(new ItemStack(Items.STONE), 20, 1, 1)));
        addBuiltin(Items.DRIPSTONE_BLOCK, List.of(new MineDropEntry(new ItemStack(Items.POINTED_DRIPSTONE), 70, 1, 2), new MineDropEntry(new ItemStack(Items.DRIPSTONE_BLOCK), 30, 1, 1)));
        addBuiltin(Items.POINTED_DRIPSTONE, List.of(new MineDropEntry(new ItemStack(Items.POINTED_DRIPSTONE), 70, 1, 2), new MineDropEntry(new ItemStack(Items.DRIPSTONE_BLOCK), 30, 1, 1)));

        // Obsidian
        List<MineDropEntry> obsidianDrops = List.of(
                new MineDropEntry(new ItemStack(Items.OBSIDIAN), 80, 1, 1),
                new MineDropEntry(new ItemStack(Items.CRYING_OBSIDIAN), 10, 1, 1),
                new MineDropEntry(new ItemStack(Items.BASALT), 10, 1, 1)
        );
        addBuiltin(Items.OBSIDIAN, obsidianDrops);
        addBuiltin(Items.CRYING_OBSIDIAN, obsidianDrops);

        // Nether & End
        addBuiltin(Items.NETHERRACK, List.of(new MineDropEntry(new ItemStack(Items.NETHERRACK), 80, 1, 2), new MineDropEntry(new ItemStack(Items.BASALT), 10, 1, 1), new MineDropEntry(new ItemStack(Items.BLACKSTONE), 10, 1, 1)));
        addBuiltin(Items.BASALT, List.of(new MineDropEntry(new ItemStack(Items.BASALT), 80, 1, 2), new MineDropEntry(new ItemStack(Items.BLACKSTONE), 20, 1, 1)));
        addBuiltin(Items.SMOOTH_BASALT, List.of(new MineDropEntry(new ItemStack(Items.SMOOTH_BASALT), 80, 1, 2), new MineDropEntry(new ItemStack(Items.BLACKSTONE), 20, 1, 1)));
        addBuiltin(Items.BLACKSTONE, List.of(new MineDropEntry(new ItemStack(Items.BLACKSTONE), 80, 1, 2), new MineDropEntry(new ItemStack(Items.BASALT), 20, 1, 1)));
        addBuiltin(Items.END_STONE, List.of(new MineDropEntry(new ItemStack(Items.END_STONE), 100, 1, 2)));

        // Quarry & Excavation
        addBuiltin(Items.SAND, List.of(new MineDropEntry(new ItemStack(Items.SAND), 80, 1, 2), new MineDropEntry(new ItemStack(Items.SANDSTONE), 20, 1, 1)));
        addBuiltin(Items.RED_SAND, List.of(new MineDropEntry(new ItemStack(Items.RED_SAND), 80, 1, 2), new MineDropEntry(new ItemStack(Items.RED_SANDSTONE), 20, 1, 1)));
        addBuiltin(Items.GRAVEL, List.of(new MineDropEntry(new ItemStack(Items.GRAVEL), 80, 1, 2), new MineDropEntry(new ItemStack(Items.FLINT), 20, 1, 1)));
        addBuiltin(Items.CLAY_BALL, List.of(new MineDropEntry(new ItemStack(Items.CLAY_BALL), 80, 1, 3), new MineDropEntry(new ItemStack(Items.SAND), 20, 1, 1)));
        addBuiltin(Items.CLAY, List.of(new MineDropEntry(new ItemStack(Items.CLAY_BALL), 80, 1, 3), new MineDropEntry(new ItemStack(Items.SAND), 20, 1, 1)));
    }

    private static void addBuiltin(Item target, List<MineDropEntry> drops) {
        BUILTIN_DROPS.put(target, drops);
    }

    public static boolean isValidMiningTarget(Item item, Level level) {
        if (BUILTIN_DROPS.containsKey(item) || DYNAMIC_CACHE.containsKey(item)) {
            return true;
        }
        if (level != null) {
            SingleRecipeInput input = new SingleRecipeInput(new ItemStack(item));
            if (level.getRecipeManager().getRecipeFor(ModRecipes.MINE_DROP_TYPE.get(), input, level).isPresent()) {
                return true;
            }
        }
        ItemStack stack = new ItemStack(item);
        if (stack.is(C_ORES) || stack.is(C_RAW_MATERIALS) || stack.is(C_GEMS) || stack.is(C_DUSTS) || stack.is(C_STONES)) {
            return true;
        }
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof DropExperienceBlock) {
                return true;
            }
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        return path.contains("ore") || path.startsWith("raw_") || path.endsWith("_raw") || path.endsWith("_cluster") || path.endsWith("_shard");
    }

    public static List<MineDropEntry> getDropEntries(Item target, Level level) {
        if (BUILTIN_DROPS.containsKey(target)) {
            return BUILTIN_DROPS.get(target);
        }

        if (DYNAMIC_CACHE.containsKey(target)) {
            return DYNAMIC_CACHE.get(target);
        }

        // 1. Check datapack custom recipes
        if (level != null) {
            SingleRecipeInput input = new SingleRecipeInput(new ItemStack(target));
            Optional<RecipeHolder<MineDropRecipe>> match = level.getRecipeManager().getRecipeFor(
                    ModRecipes.MINE_DROP_TYPE.get(),
                    input,
                    level
            );
            if (match.isPresent()) {
                List<MineDropEntry> recipeDrops = match.get().value().drops();
                DYNAMIC_CACHE.put(target, recipeDrops);
                return recipeDrops;
            }
        }

        // 2. Dynamic generation for modded ores or raw materials
        ItemStack targetStack = new ItemStack(target);
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(target);
        String path = id.getPath();

        boolean isDeepslateOre = path.contains("deepslate");
        ItemStack byproduct = isDeepslateOre ? new ItemStack(Items.COBBLED_DEEPSLATE) : new ItemStack(Items.COBBLESTONE);

        List<MineDropEntry> generated = List.of(
                new MineDropEntry(targetStack, 80, 1, 1),
                new MineDropEntry(byproduct, 20, 1, 1)
        );
        DYNAMIC_CACHE.put(target, generated);
        return generated;
    }

    public static ItemStack rollDrop(List<MineDropEntry> entries, RandomSource random) {
        if (entries == null || entries.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int totalWeight = 0;
        for (MineDropEntry entry : entries) {
            totalWeight += entry.weight();
        }

        if (totalWeight <= 0) {
            return ItemStack.EMPTY;
        }

        int roll = random.nextInt(totalWeight);
        int current = 0;
        for (MineDropEntry entry : entries) {
            current += entry.weight();
            if (roll < current) {
                int count = entry.minCount();
                if (entry.maxCount() > entry.minCount()) {
                    count += random.nextInt(entry.maxCount() - entry.minCount() + 1);
                }
                ItemStack result = entry.item().copy();
                result.setCount(count);
                return result;
            }
        }

        return ItemStack.EMPTY;
    }

    public static void clearCache() {
        DYNAMIC_CACHE.clear();
    }
}
