package mypals.ml.GUI;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
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
        List<Text> list = new ArrayList<>(Screen.getTooltipFromItem(MinecraftClient.getInstance(), itemStack));
        list.add(Text.of(formatToBoxStackCount(getMissingItemCount(MinecraftClient.getInstance(), this.itemStack.getItem(), this.itemStack.getCount()),this.itemStack.getMaxCount()).replace("/","")));
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

                    context.drawGuiTexture(RenderLayer::getGuiTextured,SLOT_OK, x, y,BUTTON_SIZE, BUTTON_SIZE);
                    context.fill(x,y,x+BUTTON_SIZE,y+BUTTON_SIZE,rgbaToInt(10, 150, 10, 100));
                    text = "√";
                } else {
                    text = formatToBoxStackCount(missingCount,this.itemStack.getMaxCount());
                    if(missingCount >= this.itemStack.getCount()){
                    //    context.fill(x,y,BUTTON_SIZE,BUTTON_SIZE,0xFFFFAA01);
                        context.drawGuiTexture(RenderLayer::getGuiTextured,SLOT_MISSING, x, y, BUTTON_SIZE, BUTTON_SIZE);
                        context.fill(x,y,x+BUTTON_SIZE,y+BUTTON_SIZE,rgbaToInt(150, 10, 10, 100));

                    }else{
                        //context.fill(x,y,BUTTON_SIZE,BUTTON_SIZE,0xFFAAAA01);
                        context.drawGuiTexture(RenderLayer::getGuiTextured,SLOT_MATCH, x, y,BUTTON_SIZE, BUTTON_SIZE);
                        context.fill(x,y,x+BUTTON_SIZE,y+BUTTON_SIZE,rgbaToInt(150, 150, 10, 100));
                    }
                }
                //RenderSystem.setShaderColor(1f,1f,1f,1);
                RenderSystem.disableDepthTest();
                if(missingCount < itemStack.getMaxCount())context.drawItemWithoutEntity(itemStack, x+4, y+4);
                else if(missingCount >= itemStack.getMaxCount() && missingCount < itemStack.getMaxCount()*27) {
                    context.drawItemWithoutEntity(itemStack, x+2, y+2);
                    context.drawItemWithoutEntity(itemStack, x+6, y+6);
                }
                else{
                    context.drawItemWithoutEntity(itemStack, x+4, y+4);

                    context.getMatrices().push();

                    context.getMatrices().scale(0.5f,0.5f,0.5f);
                    context.getMatrices().translate(x+23,y-2,200);
                    context.drawItemWithoutEntity(Items.SHULKER_BOX.getDefaultStack(), x+6, y+6);
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
    public static int rgbaToInt(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                (b & 0xFF);
    }
    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }


    public static String formatToBoxStackCount(int number, int maxStackCount) {
        if (number < 0) return "0";

        int box = number / (maxStackCount*27);
        int remaining = number % (maxStackCount*27);
        int stack = remaining / maxStackCount;
        int c = remaining % maxStackCount;

        StringBuilder result = new StringBuilder();

        if (box > 0) {
            result.append(box).append("Box(es) + ");
        }
        if (stack > 0 || box > 0) {
            if (!result.isEmpty()) result.append("/");
            result.append(stack).append("Stack(s) + ");
        }
        if (c > 0 || stack > 0 || box > 0) {
            if (!result.isEmpty()) result.append("/");
            result.append(c);
        }

        return !result.isEmpty() ? result.toString() : "0";
    }
    public void drawItemCount(DrawContext drawContext, TextRenderer textRenderer, int x, int y, String text) {
        drawContext.getMatrices().push();

        if(text.getBytes().length > 3) {
            text = text.replace("Box(es) + ","|");
            text = text.replace("Stack(s) + ","|");
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
        if (client == null || client.player == null) return requiredCount;

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
