package mypals.ml.GUI;

import com.google.common.collect.Lists;
import mypals.ml.TellMeWhatINeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import java.util.Iterator;
import java.util.List;

public class BlueprintBookResults {
    public static final WidgetSprites PAGE_FORWARD_TEXTURES = new WidgetSprites(Identifier.withDefaultNamespace("recipe_book/page_forward"), Identifier.withDefaultNamespace("recipe_book/page_forward_highlighted"));
    public static final WidgetSprites PAGE_BACKWARD_TEXTURES = new WidgetSprites(Identifier.withDefaultNamespace("recipe_book/page_backward"), Identifier.withDefaultNamespace("recipe_book/page_backward_highlighted"));
    private final List<BlueprintResultButton> resultButtons = Lists.newArrayListWithCapacity(20);
    private BlueprintResultButton hoveredResultButton;
    private ToggleButton nextPageButton;
    private ToggleButton prevPageButton;

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
        ToggleButton.click(this.nextPageButton, mouseX, mouseY, button);
        ToggleButton.click(this.prevPageButton, mouseX, mouseY, button);
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
    public void draw(GuiGraphicsExtractor context, int x, int y, int mouseX, int mouseY, float delta) {

        for (BlueprintResultButton button : this.resultButtons) {
            button.extractRenderState(context, mouseX, mouseY, delta);
            if(button.visible && button.isMouseOver(mouseX, mouseY)) {
                this.hoveredResultButton = button;
            }
        }
        if (this.pageCount > 1) {
            Component pageText = Component.translatable("gui.recipebook.page", this.currentPage + 1, this.pageCount);
            int textWidth = Minecraft.getInstance().font.width(pageText);
            context.text(Minecraft.getInstance().font, pageText, x - textWidth / 2 + 73+35, y + 141, -1, false);
        }

        this.hoveredResultButton = null;
        this.prevPageButton.extractRenderState(context, mouseX, mouseY, delta);
        this.nextPageButton.extractRenderState(context, mouseX, mouseY, delta);
    }
    public void drawTooltip(GuiGraphicsExtractor context, int x, int y) {
        if (Minecraft.getInstance().gui.screen() != null && this.hoveredResultButton != null) {
            context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, this.hoveredResultButton.$getTooltip(), x, y);
        }

    }
    public void initialize(Minecraft client, int parentLeft, int parentTop) {
        setItemPos(parentLeft,parentTop);

        this.nextPageButton = new ToggleButton(parentLeft + 93+35, parentTop + 137, 12, 17, false){
            @Override
            public void onPress() {
                if(currentPage < pageCount) {
                    currentPage++;
                    refreshResultButtons();
                }
            }
        };
        this.nextPageButton.initTextureValues(PAGE_FORWARD_TEXTURES);
        this.nextPageButton.visible = false;
        this.prevPageButton = new ToggleButton(parentLeft + 38+35, parentTop + 137, 12, 17, true){
            @Override
            public void onPress() {
                if(currentPage > 0) {
                    currentPage--;
                    refreshResultButtons();
                    //TellMeWhatINeed.LOGGER.info("currentPage: " + currentPage);
                }
            }
        };;
        this.prevPageButton.initTextureValues(PAGE_BACKWARD_TEXTURES);
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

