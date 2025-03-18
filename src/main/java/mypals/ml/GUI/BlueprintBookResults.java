package mypals.ml.GUI;

import com.google.common.collect.Lists;
import mypals.ml.TellMeWhatINeed;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.Iterator;
import java.util.List;

public class BlueprintBookResults {
    public static final ButtonTextures PAGE_FORWARD_TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/page_forward"), Identifier.ofVanilla("recipe_book/page_forward_highlighted"));
    public static final ButtonTextures PAGE_BACKWARD_TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/page_backward"), Identifier.ofVanilla("recipe_book/page_backward_highlighted"));
    private final List<BlueprintResultButton> resultButtons = Lists.newArrayListWithCapacity(20);
    private BlueprintResultButton hoveredResultButton;
    private ToggleButtonWidget nextPageButton;
    private ToggleButtonWidget prevPageButton;
    private int pageCount;
    private int currentPage;
    private int parentLeft, parentTop;
    private List<ItemStack> itemStacks = Lists.newArrayList();
    public BlueprintBookResults() {
        for(int i = 0; i < 20; ++i) {
            this.resultButtons.add(new BlueprintResultButton());
        }

    }
    public void setItems(List<ItemStack> itemStacks, boolean resetCurrentPage) {
        this.itemStacks = itemStacks;
        this.pageCount = (int)Math.ceil((double)itemStacks.size() / 25.0);
        if (this.pageCount <= this.currentPage || resetCurrentPage) {
            this.currentPage = 0;
        }
        refreshResultButtons();
    }
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.nextPageButton.mouseClicked(mouseX,mouseY,button);
        this.prevPageButton.mouseClicked(mouseX,mouseY,button);
        return false;
    }
    public void refreshResultButtons() {
        int startIndex = 25 * this.currentPage;
        resultButtons.clear();
        while (resultButtons.size() < 25) {
            resultButtons.add(new BlueprintResultButton());
        }
        setItemPos(parentLeft,parentTop);
        for(int i = 0; i < 25; ++i) {
            BlueprintResultButton button = this.resultButtons.get(i);
            if (startIndex + i < this.itemStacks.size()) {
                button.setItemStack(this.itemStacks.get(startIndex + i));
                button.visible = true;
            } else {
                button.visible = false;
            }
        }
        if(nextPageButton != null && prevPageButton != null) {
            this.updatePageButtons();
        }
    }
    void updatePageButtons() {
        this.nextPageButton.visible = this.pageCount > 1 && this.currentPage < this.pageCount - 1;
        this.prevPageButton.visible = this.pageCount > 1 && this.currentPage > 0;
    }
    public void draw(DrawContext context, int x, int y, int mouseX, int mouseY, float delta) {
        if (this.pageCount > 1) {
            Text pageText = Text.translatable("gui.recipebook.page", this.currentPage + 1, this.pageCount);
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(pageText);
            context.drawText(MinecraftClient.getInstance().textRenderer, pageText, x - textWidth / 2 + 73, y + 141, -1, false);
        }

        this.hoveredResultButton = null;


        for (BlueprintResultButton button : this.resultButtons) {
            button.render(context, mouseX, mouseY, delta);
            if(button.visible && button.isMouseOver(mouseX, mouseY)) {
                this.hoveredResultButton = button;
            }
        }

        this.prevPageButton.render(context, mouseX, mouseY, delta);
        this.nextPageButton.render(context, mouseX, mouseY, delta);
    }
    public void drawTooltip(DrawContext context, int x, int y) {
        if (MinecraftClient.getInstance().currentScreen != null && this.hoveredResultButton != null) {
            context.drawTooltip(MinecraftClient.getInstance().textRenderer, this.hoveredResultButton.$getTooltip(), x, y);
        }

    }
    public void initialize(MinecraftClient client, int parentLeft, int parentTop) {
        setItemPos(parentLeft,parentTop);

        this.nextPageButton = new ToggleButtonWidget(parentLeft + 93, parentTop + 137, 12, 17, false){
            @Override
            public void onClick(double mouseX, double mouseY) {
                if(currentPage < pageCount) {
                    currentPage++;
                    refreshResultButtons();
                }
            }
        };
        this.nextPageButton.setTextures(PAGE_FORWARD_TEXTURES);
        this.nextPageButton.visible = false;
        this.prevPageButton = new ToggleButtonWidget(parentLeft + 38, parentTop + 137, 12, 17, true){
            @Override
            public void onClick(double mouseX, double mouseY) {
                if(currentPage > 0) {
                    currentPage--;
                    refreshResultButtons();
                    TellMeWhatINeed.LOGGER.info("currentPage: " + currentPage);
                }
            }
        };;
        this.prevPageButton.setTextures(PAGE_BACKWARD_TEXTURES);
        this.prevPageButton.visible = false;
    }
    public void setItemPos(int parentLeft, int parentTop){
        this.parentLeft = parentLeft;
        this.parentTop = parentTop;
        for(int i = 0; i < this.resultButtons.size(); ++i) {
            (this.resultButtons.get(i)).setPosition(parentLeft + 11 + 25 * (i % 5), parentTop + 10 + 25 * (i / 5));
        }
    }
}

