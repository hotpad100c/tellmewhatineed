package mypals.ml.GUI;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BlueprintResultButton extends AbstractWidget {
    private int x;
    private int y;
    private ItemStack itemStack;
    public boolean visible = false;
    private static final int BUTTON_SIZE = 25;
    private static final Identifier SLOT_MATCH = Identifier.withDefaultNamespace("recipe_book/slot_many_craftable");
    private static final Identifier SLOT_OK = Identifier.withDefaultNamespace("recipe_book/slot_craftable");
    private static final Identifier SLOT_MISSING = Identifier.withDefaultNamespace("recipe_book/slot_uncraftable");

    public BlueprintResultButton() {
        super(0, 0, 25, 25, CommonComponents.EMPTY);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setItemStack(ItemStack stack) {
        this.itemStack = stack;
    }

    public List<Component> $getTooltip() {
        List<Component> list = new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), itemStack));
        list.add(Component.nullToEmpty(formatToBoxStackCount(getMissingItemCount(Minecraft.getInstance(), this.itemStack.getItem(), this.itemStack.getCount()),this.itemStack.getMaxStackSize()).replace("/","")));
        return list;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (this.itemStack != null) {
            boolean enough = hasItemInInventory(Minecraft.getInstance(), this.itemStack.getItem(), this.itemStack.getCount());

            if (this.itemStack.getCount() >= 1) {
                int missingCount = getMissingItemCount(Minecraft.getInstance(), this.itemStack.getItem(), this.itemStack.getCount());

                String text;
                if(enough) {

                    context.blitSprite(RenderPipelines.GUI_TEXTURED,SLOT_OK, x, y,BUTTON_SIZE, BUTTON_SIZE);
                    context.fill(x,y,x+BUTTON_SIZE,y+BUTTON_SIZE,rgbaToInt(10, 150, 10, 100));
                    text = "√";
                } else {
                    text = formatToBoxStackCount(missingCount,this.itemStack.getMaxStackSize());
                    if(missingCount >= this.itemStack.getCount()){
                    //    context.fill(x,y,BUTTON_SIZE,BUTTON_SIZE,0xFFFFAA01);
                        context.blitSprite(RenderPipelines.GUI_TEXTURED,SLOT_MISSING, x, y, BUTTON_SIZE, BUTTON_SIZE);
                        context.fill(x,y,x+BUTTON_SIZE,y+BUTTON_SIZE,rgbaToInt(150, 10, 10, 100));

                    }else{
                        //context.fill(x,y,BUTTON_SIZE,BUTTON_SIZE,0xFFAAAA01);
                        context.blitSprite(RenderPipelines.GUI_TEXTURED,SLOT_MATCH, x, y,BUTTON_SIZE, BUTTON_SIZE);
                        context.fill(x,y,x+BUTTON_SIZE,y+BUTTON_SIZE,rgbaToInt(150, 150, 10, 100));
                    }
                }
                //RenderSystem.setShaderColor(1f,1f,1f,1);
                GlStateManager._disableDepthTest();
                if(missingCount < itemStack.getMaxStackSize())context.renderFakeItem(itemStack, x+4, y+4);
                else if(missingCount >= itemStack.getMaxStackSize() && missingCount < itemStack.getMaxStackSize()*27) {
                    context.renderFakeItem(itemStack, x+2, y+2);
                    context.renderFakeItem(itemStack, x+6, y+6);
                }
                else{
                    context.renderFakeItem(itemStack, x+4, y+4);

                    context.pose().pushMatrix();

                    context.pose().scale(0.5f,0.5f);
                    context.pose().translate(x+23,y-2);
                    context.renderFakeItem(Items.SHULKER_BOX.getDefaultInstance(), x+6, y+6);
                    context.pose().popMatrix();
                }
                GlStateManager._enableDepthTest();
                drawItemCount(context, Minecraft.getInstance().font, x+4, y+4,text);
            }
        }
        if(isHovered(mouseX, mouseY) && this.visible) {
            context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, $getTooltip(), mouseX,mouseY);
        }
    }
    public static int rgbaToInt(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                (b & 0xFF);
    }
    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {

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
    public void drawItemCount(GuiGraphics drawContext, Font textRenderer, int x, int y, String text) {
        drawContext.pose().pushMatrix();

        if(text.getBytes().length > 3) {
            text = text.replace("Box(es) + ","|");
            text = text.replace("Stack(s) + ","|");
            if(text.split("/").length ==2){
                drawContext.pose().translate(x, y);
                drawContext.pose().scale(0.5f,0.5f);
                drawMultiColorText(drawContext, 40 - textRenderer.width(text), 28, text.split("/"), new int[]{Color.CYAN.getRGB(),Color.WHITE.getRGB()});
            }else{
                drawContext.pose().translate(x, y);
                drawContext.pose().scale(0.5f,0.5f);
                drawMultiColorText(drawContext, 48 - textRenderer.width(text), 28, text.split("/"), new int[]{Color.GREEN.getRGB(),Color.CYAN.getRGB(),Color.WHITE.getRGB()});
            }
            }else{
            drawContext.pose().translate(x, y);
            drawContext.drawString(textRenderer, text, 18 - textRenderer.width(text),  10, 16777215, true);
        }
        drawContext.pose().popMatrix();
    }
    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= y && mouseY < y + BUTTON_SIZE;
    }
    public static void drawMultiColorText(GuiGraphics context, int x, int y, String[] parts, int[] colors) {
        if (parts.length == 0 || colors.length == 0) return;

        int currentX = x;
        int lastColor = colors[colors.length - 1];

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            int color = (i < colors.length) ? colors[i] : lastColor;
            context.drawString(Minecraft.getInstance().font, part, currentX, y, color, true);
            currentX += Minecraft.getInstance().font.width(part);
        }
    }
    public static int getMissingItemCount(Minecraft client, Item itemToCheck, int requiredCount) {
        if (client == null || client.player == null) return requiredCount;

        Inventory inventory = client.player.getInventory();
        int totalCount = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem().equals(itemToCheck)) {
                totalCount += stack.getCount();
            }
        }
        return Math.max(0, requiredCount - totalCount);
    }

    public static boolean hasItemInInventory(Minecraft client, Item itemToCheck, int requiredCount) {
        return getMissingItemCount(client, itemToCheck, requiredCount) == 0;
    }
}
