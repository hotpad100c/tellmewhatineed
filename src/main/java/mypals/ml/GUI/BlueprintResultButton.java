package mypals.ml.GUI;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public class BlueprintResultButton extends ClickableWidget {
    private int x;
    private int y;
    private ItemStack itemStack;
    public boolean visible = false;
    private static final int BUTTON_SIZE = 25;
    private static final Identifier SLOT_MATCH = Identifier.ofVanilla("recipe_book/slot_many_craftable");
    private static final Identifier SLOT_OK = Identifier.ofVanilla("recipe_book/slot_craftable");
    private static final Identifier SLOT_MISSING = Identifier.ofVanilla("recipe_book/slot_uncraftable");

    public BlueprintResultButton() {
        super(0, 0, 25, 25, ScreenTexts.EMPTY);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setItemStack(ItemStack stack) {
        this.itemStack = stack;
    }

    public List<Text> $getTooltip() {
        List<Text> list = Lists.newArrayList((Iterable) Screen.getTooltipFromItem(MinecraftClient.getInstance(), itemStack));
        return list;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.itemStack != null) {
            boolean enough = hasItemInInventory(MinecraftClient.getInstance(), this.itemStack.getItem(), this.itemStack.getCount());

            if (this.itemStack.getCount() >= 1) {
                int missingCount = getMissingItemCount(MinecraftClient.getInstance(), this.itemStack.getItem(), this.itemStack.getCount());

                String text;
                if(enough) {
                    RenderSystem.setShaderColor(0.8f,1f,0.8f,1);
                    context.drawGuiTexture(SLOT_OK, x, y,BUTTON_SIZE, BUTTON_SIZE);
                    text = "√";
                    RenderSystem.setShaderColor(1,1,1,1);
                } else {
                    text = formatTo64xAplusB(missingCount);
                    if(missingCount >= this.itemStack.getCount()){
                        RenderSystem.setShaderColor(1f,0.8f,0.8f,1);
                        context.drawGuiTexture(SLOT_MISSING, x, y, BUTTON_SIZE, BUTTON_SIZE);
                        RenderSystem.setShaderColor(1f,1f,1f,1);
                    }else{
                        RenderSystem.setShaderColor(1f,1f,0.8f,1);
                        context.drawGuiTexture(SLOT_MATCH, x, y,BUTTON_SIZE, BUTTON_SIZE);
                        RenderSystem.setShaderColor(1f,1f,1f,1);
                    }
                }

                RenderSystem.disableDepthTest();
                if(missingCount < itemStack.getMaxCount())context.drawItemWithoutEntity(itemStack, x+4, y+4);
                else if(missingCount > itemStack.getMaxCount() && missingCount < itemStack.getMaxCount()*27) {
                    context.drawItemWithoutEntity(itemStack, x+2, y+2);
                    context.drawItemWithoutEntity(itemStack, x+6, y+6);
                }
                else{
                    context.drawItemWithoutEntity(Items.SHULKER_BOX.getDefaultStack(), x+4, y+4);

                    context.getMatrices().push();

                    context.getMatrices().scale(0.5f,0.5f,0.5f);
                    context.getMatrices().translate(x+10,y+10,200);
                    context.drawItemWithoutEntity(itemStack, x+6, y+6);
                    context.getMatrices().pop();
                }
                RenderSystem.enableDepthTest();
                drawItemCount(context, MinecraftClient.getInstance().textRenderer, x+4, y+4,text);
            }
        }
        if(isHovered(mouseX, mouseY) && this.visible) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, $getTooltip(), mouseX,mouseY);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public static String formatTo64xAplusB(int number) {
        if (number < 0) return "0";

        int box = number / 1728;
        int remaining = number % 1728;
        int stack = remaining / 64;
        int c = remaining % 64;

        StringBuilder result = new StringBuilder();

        if (box > 0) {
            result.append(box).append("|");
        }
        if (stack > 0) {
            if (!result.isEmpty()) result.append("/");
            result.append(stack).append("|");
        }
        if (c > 0) {
            if (!result.isEmpty()) result.append("/");
            result.append(c);
        }

        return !result.isEmpty() ? result.toString() : "0";
    }
    public void drawItemCount(DrawContext drawContext, TextRenderer textRenderer, int x, int y, String text) {
        drawContext.getMatrices().push();

        if(text.getBytes().length > 3) {
            if(text.split("/").length ==2){
                drawContext.getMatrices().translate(x, y, 200.0F);
                drawContext.getMatrices().scale(0.5f,0.5f,0.5f);
                drawMultiColorText(drawContext, 40 - textRenderer.getWidth(text), 28, text.split("/"), new int[]{Color.CYAN.getRGB(),Color.WHITE.getRGB()});
            }else{
                drawContext.getMatrices().translate(x, y, 200.0F);
                drawContext.getMatrices().scale(0.5f,0.5f,0.5f);
                drawMultiColorText(drawContext, 48 - textRenderer.getWidth(text), 28, text.split("/"), new int[]{Color.GREEN.getRGB(),Color.CYAN.getRGB(),Color.WHITE.getRGB()});
            }
            }else{
            drawContext.getMatrices().translate(x, y, 200.0F);
            drawContext.drawText(textRenderer, text, 18 - textRenderer.getWidth(text),  10, 16777215, true);
        }
        drawContext.getMatrices().pop();
    }
    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= y && mouseY < y + BUTTON_SIZE;
    }
    public static void drawMultiColorText(DrawContext context, int x, int y, String[] parts, int[] colors) {
        if (parts.length == 0 || colors.length == 0) return;

        int currentX = x;
        int lastColor = colors[colors.length - 1];

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int color = (i < colors.length) ? colors[i] : lastColor;
            context.drawText(MinecraftClient.getInstance().textRenderer, part, currentX, y, color, true);
            currentX += MinecraftClient.getInstance().textRenderer.getWidth(part);
        }
    }
    public static int getMissingItemCount(MinecraftClient client, Item itemToCheck, int requiredCount) {
        if (client == null || client.player == null) return requiredCount; // 如果玩家不存在，返回全部所需数量

        PlayerInventory inventory = client.player.getInventory();
        int totalCount = 0;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && stack.getItem().equals(itemToCheck)) {
                totalCount += stack.getCount();
            }
        }
        return Math.max(0, requiredCount - totalCount);
    }

    public static boolean hasItemInInventory(MinecraftClient client, Item itemToCheck, int requiredCount) {
        return getMissingItemCount(client, itemToCheck, requiredCount) == 0;
    }
}
