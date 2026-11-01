package mypals.ml.GUI;

import com.google.common.collect.Lists;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialListEntry;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager;
import mypals.ml.MaterialBreakdown;
import mypals.ml.TellMeWhatINeed;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static mypals.ml.GUI.BlueprintBookResults.PAGE_BACKWARD_TEXTURES;
import static mypals.ml.GUI.BlueprintBookResults.PAGE_FORWARD_TEXTURES;
import static mypals.ml.MaterialBreakdown.getBaseMaterials;
import static mypals.ml.MaterialBreakdown.mergeItemStacks;
import static mypals.ml.TellMeWhatINeed.MOD_ID;

@Environment(EnvType.CLIENT)
public class BlueprintWidget implements Renderable, GuiEventListener, NarratableEntry {
    //private static final Identifier TEXTURE = Identifier.of(MOD_ID, "assets/tellmewhatineed/textures/gui/blueprint_widget.png");
    protected static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final Logger log = LoggerFactory.getLogger(BlueprintWidget.class);
    private int leftOffset;
    private int parentWidth;
    private int parentHeight;
    private final int LOADING_TIMEOUT = 5000;
    private ToggleButton toggleMaterialBreakDownButton;
    public EditBox depthField;
    private Minecraft client;
    private CraftingMenu craftingScreenHandler;
    public final BlueprintBookResults materialArea = new BlueprintBookResults();
    private boolean narrow;
    public boolean open;
    private List<SchematicPlacement> placements;
    private final List<BlueprintGroupButtonWidget> tabButtons = Lists.newArrayList();
    private int x, y;
    private int loadingTime = 0;
    private ToggleButton nextTabPageButton;
    private ToggleButton prevTabPageButton;
    private int tabPageCount;
    private int currentTabPage;

    public BlueprintGroupButtonWidget currentTab;
    public static final WidgetSprites BLUEPRINT_BUTTON_TEXTURES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(MOD_ID, "textures/blueprint_button.png"),
            Identifier.fromNamespaceAndPath(MOD_ID, "textures/blueprint_button_highlighted.png")
    );
    private static final WidgetSprites BREAKDOWN_BUTTON_TEXTURES = new WidgetSprites(
            Identifier.fromNamespaceAndPath(MOD_ID,"textures/breakdown_enabled.png"),
            Identifier.fromNamespaceAndPath(MOD_ID,"textures/breakdown_disabled.png"),
            Identifier.fromNamespaceAndPath(MOD_ID,"textures/breakdown_enabled_highlighted.png"),
            Identifier.fromNamespaceAndPath(MOD_ID,"textures/breakdown_disabled_highlighted.png"));


    public void initialize(int parentWidth, int parentHeight, Minecraft client, boolean narrow) {
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
        this.client = client;
        this.craftingScreenHandler = craftingScreenHandler;
        this.narrow = narrow;
        this.leftOffset = narrow ? 0 : 86;
        this.x = (parentWidth - 147) / 2 - this.leftOffset;
        this.y = (parentHeight - 166) / 2;
        this.placements = getPlacements();
        this.open = false;
        this.loadingTime = 0;
        int buttonX = (this.parentWidth - 147) / 2 - this.leftOffset - 30;
        int buttonY = (this.parentHeight - 166) / 2 + 166 - 17;
        this.nextTabPageButton = new ToggleButton(buttonX + 20, buttonY, 12, 17, false){
            @Override
            public void onPress(){
                if(currentTabPage < tabPageCount) {
                    currentTabPage++;
                    refreshTabButtons();
                    refreshItems();
                }
            }
        };
        this.nextTabPageButton.initTextureValues(PAGE_FORWARD_TEXTURES);
        this.prevTabPageButton = new ToggleButton(buttonX-15 , buttonY, 12, 17, true){
            @Override
            public void onPress(){
                if(currentTabPage > 0){
                    currentTabPage--;
                    refreshTabButtons();
                    refreshItems();
                }
            }
        };
        this.prevTabPageButton.initTextureValues(PAGE_BACKWARD_TEXTURES);
        this.toggleMaterialBreakDownButton = new ToggleButton(buttonX+40, buttonY - 11, 26, 16, false){
            @Override
            public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
                int i = (parentWidth - 147) / 2 - leftOffset;
                int j = (parentHeight - 166) / 2;
                if (this.sprites != null) {
                    context.blit(RenderPipelines.GUI_TEXTURED,this.sprites.get(this.isStateTriggered, this.isHoveredOrFocused()), this.getX(), this.getY(),0,0,26,16, 26, 16);
                }
            }
            @Override
            public void onPress(){
                toggleBreakdownMaterials();
                updateTooltip();
                refreshItems();
            }
        };
        toggleMaterialBreakDownButton.initTextureValues(BREAKDOWN_BUTTON_TEXTURES);
        this.depthField = new EditBox(Minecraft.getInstance().font, buttonX+70, buttonY -11, 20, 16, Component.literal("Target Depth")){
            //? if >=1.21.9 {
            @Override
            public void onClick(MouseButtonEvent event, boolean doubleClick) {
                this.setFocused(true);
            }
            //?} else {
            /*@Override
            public void onClick(double mouseX, double mouseY) {
                this.setFocused(true);
            }
            *///?}

        };
        this.depthField.setValue("-1");
        this.depthField.setTextColor(Color.WHITE.getRGB());
        this.depthField.setHint(Component.nullToEmpty("-1"));
        updateTooltip();
    }
    private void updateTooltip() {
        this.toggleMaterialBreakDownButton.setTooltip(this.toggleMaterialBreakDownButton.isStateTriggered() ?
                Tooltip.create(Component.nullToEmpty("Break down into base materials.")) : Tooltip.create(Component.nullToEmpty("Display actual materials.")));
    }
    private void toggleBreakdownMaterials() {
        this.toggleMaterialBreakDownButton.setStateTriggered(!this.toggleMaterialBreakDownButton.isStateTriggered());
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
    public void drawTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if (this.isOpen()) {
            this.materialArea.drawTooltip(context, mouseX, mouseY);
        }
    }
    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (this.isOpen()) {
            int i = (this.parentWidth - 147) / 2 - this.leftOffset;
            int j = (this.parentHeight - 166) / 2;
            context.pose().pushMatrix();
            context.pose().translate(0.0F, 0.0F);




            context.blit(RenderPipelines.GUI_TEXTURED,TEXTURE, i, j, 1, 1, 147, 166,256,256);
            for (BlueprintGroupButtonWidget blueprintGroupButtonWidget : this.tabButtons) {
                blueprintGroupButtonWidget.extractRenderState(context, mouseX, mouseY, delta);
            }
            if(tabPageCount > 1) {
                context.text(Minecraft.getInstance().font, currentTabPage + 1 + "/" + tabPageCount, nextTabPageButton.getX() - 18, nextTabPageButton.getY() + 5, -1, false);
            }
            nextTabPageButton.extractRenderState(context, mouseX, mouseY, delta);
            depthField.extractRenderState(context, mouseX, mouseY, delta);
            prevTabPageButton.extractRenderState(context, mouseX, mouseY, delta);
            toggleMaterialBreakDownButton.extractRenderState(context, mouseX, mouseY, delta);
            if(currentTab != null) {
                if (currentTab.getBlueprintGroup().getState() == BlueprintGroup.BlueprintState.LOADING) {
                    context.text(Minecraft.getInstance().font,
                            "Loading materials...",
                            i + 147 / 2 - client.font.width("Load Timeout") / 2,
                            j + 166 / 2 - client.font.lineHeight / 2,
                            0x555555,
                            false);
                    tryLoadItems();
                } else if (currentTab.getBlueprintGroup().getState() == BlueprintGroup.BlueprintState.LOADED) {
                    this.materialArea.draw(context, i, j, mouseX, mouseY, delta);

                } else if (currentTab.getBlueprintGroup().getState() == BlueprintGroup.BlueprintState.TIMEOUT) {
                    context.text(Minecraft.getInstance().font,
                            "Load Timeout",
                            i + 147 / 2 - client.font.width("Load Timeout") / 2,
                            j + 166 / 2 - client.font.lineHeight / 2,
                            0xFF5555,
                            false);
                }
            }


            context.pose().popMatrix();


            //context.getMatrices().push();
            //context.getMatrices().translate(0,0,0);
            //context.getMatrices().pop();
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
            button.setStateTriggered(false);
            button.visible = false;
        });
        for (int k = startIndex; k < endIndex; k++) {
            BlueprintGroupButtonWidget button = tabButtons.get(k);
            button.setPosition(i, j + 27 * l++);
            button.visible = true;
        }
        if(!this.tabButtons.isEmpty()) {
            currentTab = this.tabButtons.get(startIndex);
            currentTab.setStateTriggered(true);
        }
        this.nextTabPageButton.visible = this.tabPageCount > 1 && this.currentTabPage < this.tabPageCount - 1;
        this.prevTabPageButton.visible = this.tabPageCount > 1 && this.currentTabPage > 0;
    }
    private List<SchematicPlacement> getPlacements() {
        List<SchematicPlacement> placementList = new ArrayList<>();
        Minecraft client = Minecraft.getInstance();

        Vec3 playerPos = client.player.position();
        int viewDistance = client.options.renderDistance().get();
        int playerChunkX = Mth.floor(playerPos.x / 16.0);
        int playerChunkZ = Mth.floor(playerPos.z / 16.0);

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
                //? if >=26.1 {
                int chunkX = chunkPos.x();
                int chunkZ = chunkPos.z();
                //?} else {
                /*int chunkX = chunkPos.x;
                int chunkZ = chunkPos.z;
                *///?}
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
    public NarrationPriority narrationPriority() {
        return this.open ? NarrationPriority.HOVERED : NarrationPriority.NONE;
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
    public void updateNarration(NarrationElementOutput builder) {

    }

    //? if >=1.21.9 {
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return this.handleClick(event.x(), event.y(), event.button());
    }
    //?} else {
    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.handleClick(mouseX, mouseY, button);
    }
    *///?}

    public boolean handleClick(double mouseX, double mouseY, int button) {
        if(this.isOpen()) {

            this.materialArea.mouseClicked(mouseX, mouseY, button);
            ToggleButton.click(this.nextTabPageButton, mouseX, mouseY, button);
            ToggleButton.click(this.prevTabPageButton, mouseX, mouseY, button);
            ToggleButton.click(this.toggleMaterialBreakDownButton, mouseX, mouseY, button);
            if(!ToggleButton.click(this.depthField, mouseX, mouseY, button)){
                this.depthField.setFocused(false);
            };
            int startIndex = currentTabPage * 5;
            int endIndex = Math.min(startIndex + 5, tabButtons.size());
            for (int k = startIndex; k < endIndex; k++) {
                BlueprintGroupButtonWidget tabButton = tabButtons.get(k);
                if(!tabButton.visible) continue;
                if (ToggleButton.click(tabButton, mouseX, mouseY, button)) {
                    if (this.currentTab != tabButton) {
                        if (this.currentTab != null) {
                            this.currentTab.setStateTriggered(false);
                        }
                        this.currentTab = tabButton;
                        this.currentTab.setStateTriggered(true);
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
    //? if >=1.21.9 {
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.depthField.keyPressed(event)) {
            this.refreshItems();
            return true;
        }
        return false;
    }
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (this.depthField.charTyped(event)) {
            this.refreshItems();
            return true;
        } else {
            return GuiEventListener.super.charTyped(event);
        }
    }
    //?} else {
    /*@Override
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
            return GuiEventListener.super.charTyped(chr, modifiers);
        }
    }
    *///?}
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
        if(toggleMaterialBreakDownButton.isStateTriggered()){
            int depth = -1;
            try {
                if(Integer.parseInt(depthField.getValue()) > 0) {
                    depth = Integer.parseInt(depthField.getValue());
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