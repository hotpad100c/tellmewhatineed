package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.GUI.BlueprintWidget;
import mypals.ml.TellMeWhatINeed;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
//?}
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.GUI.BlueprintWidget.BLUEPRINT_BUTTON_TEXTURES;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class RecipeBookScreenMixin<T extends RecipeBookMenu> extends AbstractContainerScreen<T> implements RecipeUpdateListener {
    @Final
    @Shadow
    private RecipeBookComponent recipeBookComponent;
    @Shadow private boolean widthTooNarrow;
    @Unique
    private BlueprintWidget blueprintWidget;
    @Unique
    private ImageButton blueprintButton;
    @Unique
    private ImageButton recipeBookButton;

    public RecipeBookScreenMixin(T handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }


    @Override
    public void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        int i = this.leftPos;
        int j = (this.height - this.imageHeight) / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("textures/gui/container/crafting_table.png"), i, j, 0, 0, this.imageWidth, this.imageHeight,256,256);
    }
    @Unique
    private ScreenPosition getRecipeBookButtonPos(){
        if(this.menu instanceof CraftingMenu){
            return new ScreenPosition(this.leftPos + 5, this.height / 2 - 49);
        }
        if(this.menu instanceof AbstractFurnaceMenu){
            return new ScreenPosition(this.leftPos + 20, this.height / 2 - 49);
        }
        if(this.menu instanceof InventoryMenu){
            return new ScreenPosition(this.leftPos + 104, this.height / 2 - 22);
        }
        return new ScreenPosition(this.leftPos + 5, this.height / 2 - 49);
    }
    @Unique
    private ScreenPosition getRecipeBlueprintButtonPos(ScreenPosition recipeBookButtonPos){
        if(this.menu instanceof CraftingMenu){
            return new ScreenPosition(recipeBookButtonPos.x(), recipeBookButtonPos.y() - 21);
        }
        if(this.menu instanceof AbstractFurnaceMenu){
            return new ScreenPosition(recipeBookButtonPos.x(), recipeBookButtonPos.y() - 21);
        }
        if(this.menu instanceof InventoryMenu){
            return new ScreenPosition(recipeBookButtonPos.x() + 21, recipeBookButtonPos.y());
        }
        return new ScreenPosition(recipeBookButtonPos.x(), recipeBookButtonPos.y() - 21);
    }

    @WrapMethod(method = "initButton")
    private void addRecipeBook(Operation<Void> original) {
        super.init();
        this.widthTooNarrow = this.width < 379;
        this.blueprintWidget = new BlueprintWidget();
        this.blueprintWidget.initialize(this.width, this.height, this.minecraft, this.widthTooNarrow);
        this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
        this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);

        ScreenPosition screenPos = this.getRecipeBookButtonPos();
        ScreenPosition screenPos2 = this.getRecipeBlueprintButtonPos(screenPos);
        this.addRenderableWidget(recipeBookButton = new ImageButton(screenPos.x(), screenPos.y(), 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
            this.recipeBookComponent.toggleVisibility();
            if(this.blueprintWidget.isOpen()) this.blueprintWidget.toggleOpen();
            this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
            ScreenPosition screenPos3 = this.getRecipeBookButtonPos();
            ScreenPosition screenPos4 = this.getRecipeBlueprintButtonPos(screenPos3);
            button.setPosition(screenPos3.x(), screenPos3.y());
            this.blueprintButton.setPosition(screenPos4.x(), this.blueprintButton.getY());
        }));
        this.addWidget(this.recipeBookComponent);
        this.titleLabelX = 29;

        this.blueprintButton = new ImageButton(
                screenPos2.x(), screenPos2.y(), 20, 18,
                BLUEPRINT_BUTTON_TEXTURES,
                button -> {
                    if(this.recipeBookComponent.isVisible()) this.recipeBookComponent.toggleVisibility();

                    this.blueprintWidget.toggleOpen();
                    this.leftPos = this.blueprintWidget.findLeftEdge(this.width, this.imageWidth);
                    ScreenPosition screenPos5 = this.getRecipeBookButtonPos();
                    ScreenPosition screenPos6 = this.getRecipeBlueprintButtonPos(screenPos5);
                    button.setPosition(screenPos6.x(), screenPos6.y());
                    this.recipeBookButton.setPosition(screenPos5.x(), this.recipeBookButton.getY());
                }
        ){
            @Override
            //? if >=1.21.11 {
            public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
            //?} else
            /*public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {*/
                Identifier identifier = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
                context.blit(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(),0,0, this.width, this.height, this.width, this.height);
            }

        };
        this.addRenderableWidget(this.blueprintButton);
        this.addWidget((GuiEventListener & NarratableEntry)this.blueprintWidget);

        if(TellMeWhatINeed.bluePrintBookEnabled) {
            if(this.recipeBookComponent.isVisible()) this.recipeBookComponent.toggleVisibility();
            this.leftPos = this.blueprintWidget.findLeftEdge(this.width, this.imageWidth);
            //? if >=1.21.9 {
            this.blueprintButton.onPress(new MouseButtonInfo(0, 0));
            //?} else
            /*this.blueprintButton.onPress();*/
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderBlueprintWidget(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.blueprintWidget.isOpen() && this.widthTooNarrow) {
            this.renderBackground(context, mouseX, mouseY, delta);
            this.blueprintWidget.render(context, mouseX, mouseY, delta);
        } else {
            this.blueprintWidget.render(context, mouseX, mouseY, delta);
        }
        this.blueprintWidget.drawTooltip(context, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    //? if >=1.21.9 {
    private void handleBlueprintClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> ci) {
        if(this.blueprintWidget.handleClick(event.x(), event.y(), event.button())) {
    //?} else {
    /*private void handleBlueprintClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        if(this.blueprintWidget.handleClick(mouseX, mouseY, button)) {
    *///?}
            this.setFocused(this.blueprintWidget);
            ci.setReturnValue(true);
        }
        if (this.widthTooNarrow && this.blueprintWidget.isOpen()) {
            ci.cancel();
       }
    }

}
