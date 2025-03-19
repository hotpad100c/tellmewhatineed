package mypals.ml;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MaterialBreakdown {
    private static final Logger log = LoggerFactory.getLogger(MaterialBreakdown.class);

    private static final Set<Item> BASE_MATERIALS = Set.of(
            Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND, Items.STICK, Items.COAL,
            Items.COPPER_INGOT, Items.NETHERITE_INGOT, Items.REDSTONE, Items.LAPIS_LAZULI,
            Items.RAW_COPPER, Items.RAW_GOLD, Items.RAW_IRON, Items.IRON_NUGGET, Items.GOLD_NUGGET,
            Items.WHITE_WOOL, Items.WHITE_BED,Items.LEATHER
    );
    private static final Set<Item> WOOL_VARIANTS = new HashSet<>(Arrays.asList(
            Items.WHITE_WOOL, Items.RED_WOOL, Items.BLUE_WOOL, Items.GREEN_WOOL, Items.YELLOW_WOOL,
            Items.BLACK_WOOL, Items.BROWN_WOOL, Items.CYAN_WOOL, Items.GRAY_WOOL, Items.LIGHT_BLUE_WOOL,
            Items.LIGHT_GRAY_WOOL, Items.LIME_WOOL, Items.MAGENTA_WOOL, Items.ORANGE_WOOL,
            Items.PINK_WOOL, Items.PURPLE_WOOL
    ));
    private static final Set<Item> BED_VARIANTS = new HashSet<>(Arrays.asList(
            Items.WHITE_BED, Items.RED_BED, Items.BLUE_BED, Items.GREEN_BED, Items.YELLOW_BED,
            Items.BLACK_BED, Items.BROWN_BED, Items.CYAN_BED, Items.GRAY_BED, Items.LIGHT_BLUE_BED,
            Items.LIGHT_GRAY_BED, Items.LIME_BED, Items.MAGENTA_BED, Items.ORANGE_BED,
            Items.PINK_BED, Items.PURPLE_BED
    ));

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

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            log.warn("World is null, cannot access RecipeManager");
            return new BreakdownResult(new ArrayList<>(), 0);
        }

        RecipeManager recipeManager = client.world.getRecipeManager();
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

    private static void breakdownItemStack(ItemStack stack, RecipeManager recipeManager,
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
            handleColoredItem(stack, Items.WHITE_WOOL, baseMaterials, multiplier);
            visited.remove(item);
            return;
        } else if (BED_VARIANTS.contains(item)) {
            handleColoredItem(stack, Items.WHITE_BED, baseMaterials, multiplier);
            visited.remove(item);
            return;
        }

        Optional<CraftingRecipe> recipeOpt = findRecipeByOutput(stack, recipeManager);
        if (recipeOpt.isEmpty()) {
            addBaseMaterial(item, baseMaterials, stack.getCount() * multiplier);
            return;
        }

        CraftingRecipe recipe = recipeOpt.get();
        ItemStack output = recipe.getOutput(null);
        int outputCount = output.getCount();
        int requiredCrafts = (int) Math.ceil((double) stack.getCount() * multiplier / outputCount);

        DefaultedList<Ingredient> ingredients = recipe.getIngredients();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isEmpty()) continue;

            ItemStack[] matchingStacks = ingredient.getMatchingStacks();
            if (matchingStacks.length == 0) continue;

            ItemStack ingredientStack = matchingStacks[0].copy();
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
        if (item == Items.RED_WOOL || item == Items.RED_BED) return Items.RED_DYE;
        if (item == Items.BLUE_WOOL || item == Items.BLUE_BED) return Items.BLUE_DYE;
        if (item == Items.GREEN_WOOL || item == Items.GREEN_BED) return Items.GREEN_DYE;
        if (item == Items.YELLOW_WOOL || item == Items.YELLOW_BED) return Items.YELLOW_DYE;
        if (item == Items.BLACK_WOOL || item == Items.BLACK_BED) return Items.BLACK_DYE;
        if (item == Items.BROWN_WOOL || item == Items.BROWN_BED) return Items.BROWN_DYE;
        if (item == Items.CYAN_WOOL || item == Items.CYAN_BED) return Items.CYAN_DYE;
        if (item == Items.GRAY_WOOL || item == Items.GRAY_BED) return Items.GRAY_DYE;
        if (item == Items.LIGHT_BLUE_WOOL || item == Items.LIGHT_BLUE_BED) return Items.LIGHT_BLUE_DYE;
        if (item == Items.LIGHT_GRAY_WOOL || item == Items.LIGHT_GRAY_BED) return Items.LIGHT_GRAY_DYE;
        if (item == Items.LIME_WOOL || item == Items.LIME_BED) return Items.LIME_DYE;
        if (item == Items.MAGENTA_WOOL || item == Items.MAGENTA_BED) return Items.MAGENTA_DYE;
        if (item == Items.ORANGE_WOOL || item == Items.ORANGE_BED) return Items.ORANGE_DYE;
        if (item == Items.PINK_WOOL || item == Items.PINK_BED) return Items.PINK_DYE;
        if (item == Items.PURPLE_WOOL || item == Items.PURPLE_BED) return Items.PURPLE_DYE;
        return null;
    }

    private static Optional<CraftingRecipe> findRecipeByOutput(ItemStack output, RecipeManager recipeManager) {
        return recipeManager.listAllOfType(RecipeType.CRAFTING).stream()
                .filter(recipe -> ItemStack.areItemsEqual(recipe.getOutput(null), output))
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