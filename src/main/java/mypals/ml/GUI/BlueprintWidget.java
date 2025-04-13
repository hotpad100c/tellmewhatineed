package mypals.ml.GUI;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import mypals.ml.MaterialBreakdown;
import mypals.ml.TellMeWhatINeed;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static mypals.ml.GUI.BlueprintBookResults.PAGE_BACKWARD_TEXTURES;
import static mypals.ml.GUI.BlueprintBookResults.PAGE_FORWARD_TEXTURES;
import static mypals.ml.MaterialBreakdown.getBaseMaterials;
import static mypals.ml.MaterialBreakdown.mergeItemStacks;
import static mypals.ml.TellMeWhatINeed.MOD_ID;

@Environment(EnvType.CLIENT)
public class BlueprintWidget implements Drawable, Element, Selectable {
    //private static final Identifier TEXTURE = Identifier.of(MOD_ID, "assets/tellmewhatineed/textures/gui/blueprint_widget.png");
    protected static final Identifier TEXTURE = new Identifier("textures/gui/recipe_book.png");
    private static final Logger log = LoggerFactory.getLogger(BlueprintWidget.class);
    private int leftOffset;
    private int parentWidth;
    private int parentHeight;
    private final int LOADING_TIMEOUT = 5000;
    private ToggleButtonWidget toggleMaterialBreakDownButton;
    public TextFieldWidget depthField;
    private MinecraftClient client;
    private CraftingScreenHandler craftingScreenHandler;
    public final BlueprintBookResults materialArea = new BlueprintBookResults();
    private boolean narrow;
    public boolean open;
    private List<SchematicPlacement> placements;
    private final List<BlueprintGroupButtonWidget> tabButtons = Lists.newArrayList();
    private int x, y;
    private int loadingTime = 0;
    private ToggleButtonWidget nextTabPageButton;
    private ToggleButtonWidget prevTabPageButton;
    private int tabPageCount;
    private int currentTabPage;

    public BlueprintGroupButtonWidget currentTab;
    public static final ButtonTextures BLUEPRINT_BUTTON_TEXTURES = new ButtonTextures(
            Identifier.of(MOD_ID, "textures/blueprint_button.png"),
            Identifier.of(MOD_ID, "textures/blueprint_button_highlighted.png")
    );
    private static final ButtonTextures BREAKDOWN_BUTTON_TEXTURES = new ButtonTextures(
            Identifier.of(MOD_ID,"textures/breakdown_enabled.png"),
            Identifier.of(MOD_ID,"textures/breakdown_disabled.png"),
            Identifier.of(MOD_ID,"textures/breakdown_enabled_highlighted.png"),
            Identifier.of(MOD_ID,"textures/breakdown_disabled_highlighted.png"));


    public void initialize(int parentWidth, int parentHeight, MinecraftClient client, boolean narrow) {
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
        this.client = client;
        this.narrow = narrow;
        this.leftOffset = narrow ? 0 : 86;
        this.x = (parentWidth - 147) / 2 - this.leftOffset;
        this.y = (parentHeight - 166) / 2;
        this.placements = getPlacements();
        this.open = false;
        this.loadingTime = 0;
        int buttonX = (this.parentWidth - 147) / 2 - this.leftOffset - 30;
        int buttonY = (this.parentHeight - 166) / 2 + 166 - 17;
        this.nextTabPageButton = new ToggleButtonWidget(buttonX + 20, buttonY, 12, 17, false){
            @Override
            public void onClick(double mouseX, double mouseY){
                if(currentTabPage < tabPageCount) {
                    currentTabPage++;
                    refreshTabButtons();
                    refreshItems();
                }
            }
        };
        this.nextTabPageButton.setTextures(PAGE_FORWARD_TEXTURES);
        this.prevTabPageButton = new ToggleButtonWidget(buttonX-15 , buttonY, 12, 17, true){
            @Override
            public void onClick(double mouseX, double mouseY){
                if(currentTabPage > 0){
                    currentTabPage--;
                    refreshTabButtons();
                    refreshItems();
                }
            }
        };
        this.prevTabPageButton.setTextures(PAGE_BACKWARD_TEXTURES);
        this.toggleMaterialBreakDownButton = new ToggleButtonWidget(buttonX+40, buttonY - 11, 26, 16, false){
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                int i = (parentWidth - 147) / 2 - leftOffset;
                int j = (parentHeight - 166) / 2;
                if (this.textures != null) {
                    RenderSystem.disableDepthTest();
                    context.drawTexture(this.textures.get(this.toggled, this.isSelected()), this.getX(), this.getY(),0,0,26,16, 26, 16);
                    RenderSystem.enableDepthTest();
                }
            }
            @Override
            public void onClick(double mouseX, double mouseY){
                toggleBreakdownMaterials();
                updateTooltip();
                refreshItems();
            }
        };
        toggleMaterialBreakDownButton.setTextures(BREAKDOWN_BUTTON_TEXTURES);
        this.depthField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, buttonX+70, buttonY -11, 20, 16, Text.literal("Target Depth")){
            @Override
            public void onClick(double mouseX, double mouseY) {
                this.setFocused(true);
            }

        };
        this.depthField.setText("-1");
        this.depthField.setEditableColor(16777215);
        this.depthField.setPlaceholder(Text.of("-1"));
        updateTooltip();
    }
    private void updateTooltip() {
        this.toggleMaterialBreakDownButton.setTooltip(this.toggleMaterialBreakDownButton.isToggled() ?
                Tooltip.of(Text.of("Break down into base materials.")) : Tooltip.of(Text.of("Display actual materials.")));
    }
    private void toggleBreakdownMaterials() {
        this.toggleMaterialBreakDownButton.setToggled(!this.toggleMaterialBreakDownButton.isToggled());
    }
    public void toggleOpen() {
        this.open = !this.open;
        if (this.open) {
            this.reset();
        }
        TellMeWhatINeed.bluePrintBookEnabled = this.open;
    }

    public boolean isOpen() {
        return this.open;
    }

    public int findLeftEdge(int width, int backgroundWidth) {
        int i;
        if (this.isOpen() && !this.narrow) {
            i = 177 + (width - backgroundWidth - 200) / 2;
        } else {
            i = (width - backgroundWidth) / 2;
        }

        return i;
    }
    public void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        if (this.isOpen()) {
            this.materialArea.drawTooltip(context, mouseX, mouseY);
        }
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.isOpen()) {
            context.getMatrices().push();
            context.getMatrices().translate(0.0F, 0.0F, 100.0F);
            int i = (this.parentWidth - 147) / 2 - this.leftOffset;
            int j = (this.parentHeight - 166) / 2;
            context.drawTexture(TEXTURE, i, j, 1, 1, 147, 166); // 使用自定义纹理
            for (BlueprintGroupButtonWidget blueprintGroupButtonWidget : this.tabButtons) {
                blueprintGroupButtonWidget.render(context, mouseX, mouseY, delta);
            }
            if(currentTab != null) {
                if (currentTab.getBlueprintGroup().getState() == BlueprintGroup.BlueprintState.LOADING) {
                    context.drawText(MinecraftClient.getInstance().textRenderer,
                            "Loading materials...",
                            i + 147 / 2 - client.textRenderer.getWidth("Load Timeout") / 2, // 居中显示
                            j + 166 / 2 - client.textRenderer.fontHeight / 2,
                            0x555555,
                            false);
                    tryLoadItems();
                } else if (currentTab.getBlueprintGroup().getState() == BlueprintGroup.BlueprintState.LOADED) {
                    this.materialArea.draw(context, i, j, mouseX, mouseY, delta);
                    depthField.render(context, mouseX, mouseY, delta);
                } else if (currentTab.getBlueprintGroup().getState() == BlueprintGroup.BlueprintState.TIMEOUT) {
                    context.drawText(MinecraftClient.getInstance().textRenderer,
                            "Load Timeout",
                            i + 147 / 2 - client.textRenderer.getWidth("Load Timeout") / 2,
                            j + 166 / 2 - client.textRenderer.fontHeight / 2,
                            0xFF5555,
                            false);
                }
            }
            context.getMatrices().pop();

            nextTabPageButton.render(context, mouseX, mouseY, delta);
            if(tabPageCount > 1) {
                context.drawText(MinecraftClient.getInstance().textRenderer, currentTabPage + 1 + "/" + tabPageCount, nextTabPageButton.getX() - 18, nextTabPageButton.getY() + 5, -1, false);
            }
            prevTabPageButton.render(context, mouseX, mouseY, delta);
            toggleMaterialBreakDownButton.render(context, mouseX, mouseY, delta);

        }
    }
    private void refreshTabButtons() {
        int i = (this.parentWidth - 147) / 2 - this.leftOffset - 30;
        int j = (this.parentHeight - 166) / 2 + 3;

        this.tabPageCount = (int) Math.ceil((double) tabButtons.size() / 5);
        if (this.currentTabPage >= this.tabPageCount) {
            this.currentTabPage = Math.max(0, this.tabPageCount - 1);
        }

        int startIndex = currentTabPage * 5;
        int endIndex = Math.min(startIndex + 5, tabButtons.size());
        int l = 0;
        tabButtons.forEach(button -> {
            button.setToggled(false);
            button.visible = false;
        });
        for (int k = startIndex; k < endIndex; k++) {
            BlueprintGroupButtonWidget button = tabButtons.get(k);
            button.setPosition(i, j + 27 * l++);
            button.visible = true;
        }
        if(!this.tabButtons.isEmpty()) {
            currentTab = this.tabButtons.get(startIndex);
            currentTab.setToggled(true);
        }
        this.nextTabPageButton.visible = this.tabPageCount > 1 && this.currentTabPage < this.tabPageCount - 1;
        this.prevTabPageButton.visible = this.tabPageCount > 1 && this.currentTabPage > 0;
    }
    private List<SchematicPlacement> getPlacements() {
        List<SchematicPlacement> placementList = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();

        Vec3d playerPos = client.player.getPos();
        int viewDistance = client.options.getViewDistance().getValue();
        int playerChunkX = MathHelper.floor(playerPos.x / 16.0);
        int playerChunkZ = MathHelper.floor(playerPos.z / 16.0);

        if (client.player == null) {
            return placementList;
        }
        SchematicPlacementManager manager = DataManager.getSchematicPlacementManager();
        manager.getAllSchematicsPlacements().forEach(placement -> {
            Set<ChunkPos> touchedChunks = placement.getTouchedChunks();
            if (touchedChunks == null || touchedChunks.isEmpty()) {
                return;
            }

            boolean allInView = touchedChunks.stream().allMatch(chunkPos -> {
                int chunkX = chunkPos.x;
                int chunkZ = chunkPos.z;
                int chunkDistance = Math.max(Math.abs(chunkX - playerChunkX), Math.abs(chunkZ - playerChunkZ));
                return chunkDistance <= viewDistance;
            });

            if (allInView || !placement.getMaterialList().getMaterialsAll().isEmpty()) {
                placementList.add(placement);
            }
        });
        return placementList;
    }

    @Override
    public SelectionType getType() {
        return this.open ? SelectionType.HOVERED : SelectionType.NONE;
    }


    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }


    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.isOpen()) {

            this.materialArea.mouseClicked(mouseX, mouseY, button);
            this.nextTabPageButton.mouseClicked(mouseX, mouseY, button);
            this.prevTabPageButton.mouseClicked(mouseX, mouseY, button);
            this.toggleMaterialBreakDownButton.mouseClicked(mouseX, mouseY, button);
            if(!this.depthField.mouseClicked(mouseX,mouseY,button)){
                this.depthField.setFocused(false);
            };
            int startIndex = currentTabPage * 5;
            int endIndex = Math.min(startIndex + 5, tabButtons.size());
            for (int k = startIndex; k < endIndex; k++) {
                BlueprintGroupButtonWidget tabButton = tabButtons.get(k);
                if(!tabButton.visible) continue;
                if (tabButton.mouseClicked(mouseX, mouseY, button)) {
                    if (this.currentTab != tabButton) {
                        if (this.currentTab != null) {
                            this.currentTab.setToggled(false);
                        }
                        this.currentTab = tabButton;
                        this.currentTab.setToggled(true);
                    }
                    refreshItems();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {

    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.depthField.keyPressed(keyCode, scanCode, modifiers)) {
            this.refreshItems();
            return true;
        }
        return false;
    }
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.depthField.charTyped(chr, modifiers)) {
            this.refreshItems();
            return true;
        } else {
            return Element.super.charTyped(chr, modifiers);
        }
    }
    @Override
    public boolean isFocused() {
        return false;
    }
    public void reset() {
        this.loadingTime = 0;
        int i = (this.parentWidth - 147) / 2 - this.leftOffset;
        int j = (this.parentHeight - 166) / 2;
        this.placements = getPlacements();
        this.tabButtons.clear();
        for(SchematicPlacement placement : placements) {
            tabButtons.add(new BlueprintGroupButtonWidget(new BlueprintGroup(placement.getName(), placement)));
        }
        this.refreshTabButtons();
        refreshItems();
        this.materialArea.initialize(this.client, i, j);
        this.materialArea.updatePageButtons();
    }
    public void tryLoadItems(){
        if(this.currentTab != null) {
            if(loadingTime < LOADING_TIMEOUT){
                List<MaterialListEntry> entries = currentTab.getBlueprintGroup().getItems();
                if(!entries.isEmpty()){
                    setItems(entries,(this.parentWidth - 147) / 2 - this.leftOffset,(this.parentHeight - 166) / 2);
                    loadingTime = 0;
                }else{
                    loadingTime++;
                }
            }else {
                currentTab.getBlueprintGroup().setState(BlueprintGroup.BlueprintState.TIMEOUT);
                loadingTime = 0;
            }

        }
    }
    public void refreshItems(){
        int x = (this.parentWidth - 147) / 2 - this.leftOffset;
        int y = (this.parentHeight - 166) / 2;
        if(this.tabButtons.size() <= 0) return;

        List<MaterialListEntry> entries = currentTab.getBlueprintGroup().getItems();
        if(entries.isEmpty()){
            currentTab.getBlueprintGroup().refreshList();
            currentTab.getBlueprintGroup().setState(BlueprintGroup.BlueprintState.LOADING);
        }else {
            setItems(entries,x,y);
        }
    }
    public void setItems(List<MaterialListEntry> entries, int x, int y) {
        List<ItemStack> itemStacks = new ArrayList<>();
        entries.forEach(entry -> {
            itemStacks.add(new ItemStack(entry.getStack().getItem(), entry.getCountMissing()));
        });
        if(toggleMaterialBreakDownButton.isToggled()){
            int depth = -1;
            try {
                if(Integer.parseInt(depthField.getText()) > 0) {
                    depth = Integer.parseInt(depthField.getText());
                }
            }catch (NumberFormatException e) {
                //this.depthField.setText("-1");
            }

            MaterialBreakdown.BreakdownResult result = getBaseMaterials(itemStacks,depth);
            this.materialArea.setItems(mergeItemStacks(result.getMaterials()), true);
            //this.depthField.setText(String.valueOf(result.getMaxDepth()));
            this.depthField.setVisible(true);
        } else{
            this.materialArea.setItems(itemStacks, true);
            this.depthField.setVisible(false);
        }
        //this.materialArea.setItems(itemStacks, true);
        this.materialArea.setItemPos(x, y);
        currentTab.getBlueprintGroup().setState(BlueprintGroup.BlueprintState.LOADED);
    }
}