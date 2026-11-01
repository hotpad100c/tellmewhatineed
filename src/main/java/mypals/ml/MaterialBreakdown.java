package mypals.ml;

import net.minecraft.client.Minecraft;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

public class MaterialBreakdown {
    private static final Logger log = LoggerFactory.getLogger(MaterialBreakdown.class);

    //? if >=26.2 {
    /*private static final List<Item> WOOLS = Items.WOOL.asList();
    private static final List<Item> BEDS = Items.BED.asList();
    private static final List<Item> DYES = Items.DYE.asList();
    *///?} else {
    private static final List<Item> WOOLS = List.of(
            Items.WHITE_WOOL, Items.ORANGE_WOOL, Items.MAGENTA_WOOL, Items.LIGHT_BLUE_WOOL,
            Items.YELLOW_WOOL, Items.LIME_WOOL, Items.PINK_WOOL, Items.GRAY_WOOL,
            Items.LIGHT_GRAY_WOOL, Items.CYAN_WOOL, Items.PURPLE_WOOL, Items.BLUE_WOOL,
            Items.BROWN_WOOL, Items.GREEN_WOOL, Items.RED_WOOL, Items.BLACK_WOOL
    );
    private static final List<Item> BEDS = List.of(
            Items.WHITE_BED, Items.ORANGE_BED, Items.MAGENTA_BED, Items.LIGHT_BLUE_BED,
            Items.YELLOW_BED, Items.LIME_BED, Items.PINK_BED, Items.GRAY_BED,
            Items.LIGHT_GRAY_BED, Items.CYAN_BED, Items.PURPLE_BED, Items.BLUE_BED,
            Items.BROWN_BED, Items.GREEN_BED, Items.RED_BED, Items.BLACK_BED
    );
    private static final List<Item> DYES = List.of(
            Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE,
            Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE,
            Items.LIGHT_GRAY_DYE, Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE,
            Items.BROWN_DYE, Items.GREEN_DYE, Items.RED_DYE, Items.BLACK_DYE
    );
    //?}

    private static final Set<Item> BASE_MATERIALS = Set.of(
            Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND, Items.STICK, Items.COAL,
            Items.COPPER_INGOT, Items.NETHERITE_INGOT, Items.REDSTONE, Items.LAPIS_LAZULI,
            Items.RAW_COPPER, Items.RAW_GOLD, Items.RAW_IRON, Items.IRON_NUGGET, Items.GOLD_NUGGET,
            WOOLS.getFirst(), BEDS.getFirst(), Items.LEATHER
    );
    private static final Set<Item> WOOL_VARIANTS = new HashSet<>(WOOLS);
    private static final Set<Item> BED_VARIANTS = new HashSet<>(BEDS);

    public static class BreakdownResult {
        private final List<ItemStack> materials;
        private final int maxDepth;

        public BreakdownResult(List<ItemStack> materials, int maxDepth) {
            this.materials = materials;
            this.maxDepth = maxDepth;
        }

        public List<ItemStack> getMaterials() {
            return materials;
        }

        public int getMaxDepth() {
            return maxDepth;
        }
    }

    public static BreakdownResult getBaseMaterials(List<ItemStack> targetStacks, int targetDepth) {
        if (targetStacks == null || targetStacks.isEmpty()) {
            return new BreakdownResult(new ArrayList<>(), 0);
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            log.warn("World is null, cannot access RecipeManager");
            return new BreakdownResult(new ArrayList<>(), 0);
        }

        RecipeAccess recipeManager = client.level.recipeAccess();
        Map<Item, Integer> baseMaterials = new HashMap<>();
        Set<Item> visited = new HashSet<>();
        int[] maxDepth = {0};

        for (ItemStack stack : targetStacks) {
            if (!stack.isEmpty()) {
                breakdownItemStack(stack, recipeManager, baseMaterials, visited, 1, 0, targetDepth, maxDepth);
            }
        }

        List<ItemStack> result = new ArrayList<>();
        baseMaterials.forEach((item, count) -> {
            if (item != Items.AIR) {
                result.add(new ItemStack(item, count));
            }
        });

        return new BreakdownResult(result, maxDepth[0]);
    }

    private static void breakdownItemStack(ItemStack stack, RecipeAccess recipeManager,
                                           Map<Item, Integer> baseMaterials, Set<Item> visited,
                                           int multiplier, int currentDepth, int targetDepth, int[] maxDepth) {
        if (stack.isEmpty() || stack.getItem() == Items.AIR) {
            return;
        }

        Item item = stack.getItem();
        maxDepth[0] = Math.max(maxDepth[0], currentDepth);

        if ((targetDepth != -1 && currentDepth >= targetDepth) || BASE_MATERIALS.contains(item)) {
            addBaseMaterial(item, baseMaterials, stack.getCount() * multiplier);
            return;
        }

        if (visited.contains(item)) {
            addBaseMaterial(item, baseMaterials, stack.getCount() * multiplier);
            return;
        }
        visited.add(item);

        if (WOOL_VARIANTS.contains(item)) {
            handleColoredItem(stack, WOOLS.getFirst(), baseMaterials, multiplier);
            visited.remove(item);
            return;
        } else if (BED_VARIANTS.contains(item)) {
            handleColoredItem(stack, BEDS.getFirst(), baseMaterials, multiplier);
            visited.remove(item);
            return;
        }

        Optional<Map.Entry<RecipeDisplayId, RecipeDisplayEntry>> recipeOpt = findRecipeByOutput(stack, recipeManager);
        if (recipeOpt.isEmpty()) {
            addBaseMaterial(item, baseMaterials, stack.getCount() * multiplier);
            return;
        }

        ContextMap contextParameterMap = SlotDisplayContext.fromLevel((Level)Objects.requireNonNull(Minecraft.getInstance().level));
        RecipeDisplayEntry recipe = recipeOpt.get().getValue();
        ItemStack output = recipe.display().result().resolveForFirstStack(contextParameterMap);
        int outputCount = output.getCount();
        int requiredCrafts = (int) Math.ceil((double) stack.getCount() * multiplier / outputCount);

        List<Ingredient> ingredients = recipe.craftingRequirements().get();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.items().toList().isEmpty()) continue;

            List<ItemStack> matchingStacks = ingredient.display().resolveForStacks(contextParameterMap);
            if (matchingStacks.size() == 0) continue;

            ItemStack ingredientStack = matchingStacks.get(0).copy();
            ingredientStack.setCount(requiredCrafts);
            breakdownItemStack(ingredientStack, recipeManager, baseMaterials, visited, 1, currentDepth + 1, targetDepth, maxDepth);
        }

        visited.remove(item);
    }
    private static void handleColoredItem(ItemStack stack, Item baseItem, Map<Item, Integer> baseMaterials, int multiplier) {
        int count = stack.getCount() * multiplier;
        Item item = stack.getItem();

        addBaseMaterial(baseItem, baseMaterials, count);

        Item dye = getDyeForItem(item);
        if (dye != null) {
            addBaseMaterial(dye, baseMaterials, count);
        }
    }

    private static Item getDyeForItem(Item item) {
        int index = WOOLS.indexOf(item);
        if (index < 0) index = BEDS.indexOf(item);
        return index > 0 ? DYES.get(index) : null;
    }

    private static Optional<Map.Entry<RecipeDisplayId, RecipeDisplayEntry>> findRecipeByOutput(ItemStack output, RecipeAccess recipeManager) {
        Map<RecipeDisplayId, RecipeDisplayEntry> recipes = Minecraft.getInstance().player.getRecipeBook().known;
        ContextMap contextParameterMap = SlotDisplayContext.fromLevel((Level)Objects.requireNonNull(Minecraft.getInstance().level));
        return recipes.entrySet().stream()
                .filter(recipe -> ItemStack.isSameItem(recipe.getValue().display().result().resolveForFirstStack(contextParameterMap), output))
                .findFirst();
    }

    private static void addBaseMaterial(Item item, Map<Item, Integer> baseMaterials, int count) {
        baseMaterials.merge(item, count, Integer::sum);
    }

    public static List<ItemStack> mergeItemStacks(List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Item, Integer> mergedStacks = stacks.stream()
                .filter(stack -> !stack.isEmpty())
                .collect(Collectors.groupingBy(ItemStack::getItem, Collectors.summingInt(ItemStack::getCount)));

        List<ItemStack> result = new ArrayList<>();
        mergedStacks.forEach((item, totalCount) -> result.add(new ItemStack(item, totalCount)));

        return result;
    }
}
