package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.GUI.BlueprintWidget;
import mypals.ml.TellMeWhatINeed;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.GUI.BlueprintWidget.BLUEPRINT_BUTTON_TEXTURES;

@Mixin(RecipeBookScreen.class)
public abstract class RecipeBookScreenMixin<T extends AbstractRecipeScreenHandler> extends HandledScreen<T> implements RecipeBookProvider {
    @Final
    @Shadow
    private RecipeBookWidget recipeBook;
    @Shadow private boolean narrow;
    @Unique
    private BlueprintWidget blueprintWidget;
    @Unique
    private TexturedButtonWidget blueprintButton;
    @Unique
    private TexturedButtonWidget recipeBookButton;

    public RecipeBookScreenMixin(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }


    @Override
    public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = this.x;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.ofVanilla("textures/gui/container/crafting_table.png"), i, j, 0, 0, this.backgroundWidth, this.backgroundHeight,256,256);
    }
    @Unique
    private ScreenPos getRecipeBookButtonPos(){
        if(this.handler instanceof CraftingScreenHandler){
            return new ScreenPos(this.x + 5, this.height / 2 - 49);
        }
        if(this.handler instanceof AbstractFurnaceScreenHandler){
            return new ScreenPos(this.x + 20, this.height / 2 - 49);
        }
        if(this.handler instanceof PlayerScreenHandler){
            return new ScreenPos(this.x + 104, this.height / 2 - 22);
        }
        return new ScreenPos(this.x + 5, this.height / 2 - 49);
    }
    @Unique
    private ScreenPos getRecipeBlueprintButtonPos(ScreenPos recipeBookButtonPos){
        if(this.handler instanceof CraftingScreenHandler){
            return new ScreenPos(recipeBookButtonPos.x(), recipeBookButtonPos.y() - 21);
        }
        if(this.handler instanceof AbstractFurnaceScreenHandler){
            return new ScreenPos(recipeBookButtonPos.x(), recipeBookButtonPos.y() - 21);
        }
        if(this.handler instanceof PlayerScreenHandler){
            return new ScreenPos(recipeBookButtonPos.x() + 21, recipeBookButtonPos.y());
        }
        return new ScreenPos(recipeBookButtonPos.x(), recipeBookButtonPos.y() - 21);
    }

    @WrapMethod(method = "addRecipeBook")
    private void addRecipeBook(Operation<Void> original) {
        super.init();
        this.narrow = this.width < 379;
        this.blueprintWidget = new BlueprintWidget();
        this.blueprintWidget.initialize(this.width, this.height, this.client, this.narrow);
        this.recipeBook.initialize(this.width, this.height, this.client, this.narrow);
        this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);

        ScreenPos screenPos = this.getRecipeBookButtonPos();
        ScreenPos screenPos2 = this.getRecipeBlueprintButtonPos(screenPos);
        this.addDrawableChild(recipeBookButton = new TexturedButtonWidget(screenPos.x(), screenPos.y(), 20, 18, RecipeBookWidget.BUTTON_TEXTURES, button -> {
            this.recipeBook.toggleOpen();
            if(this.blueprintWidget.isOpen()) this.blueprintWidget.toggleOpen();
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            ScreenPos screenPos3 = this.getRecipeBookButtonPos();
            ScreenPos screenPos4 = this.getRecipeBlueprintButtonPos(screenPos3);
            button.setPosition(screenPos3.x(), screenPos3.y());
            this.blueprintButton.setPosition(screenPos4.x(), this.blueprintButton.getY());
        }));
        this.addSelectableChild(this.recipeBook);
        this.titleX = 29;

        this.blueprintButton = new TexturedButtonWidget(
                screenPos2.x(), screenPos2.y(), 20, 18,
                BLUEPRINT_BUTTON_TEXTURES,
                button -> {
                    if(this.recipeBook.isOpen()) this.recipeBook.toggleOpen();

                    this.blueprintWidget.toggleOpen();
                    this.x = this.blueprintWidget.findLeftEdge(this.width, this.backgroundWidth);
                    ScreenPos screenPos5 = this.getRecipeBookButtonPos();
                    ScreenPos screenPos6 = this.getRecipeBlueprintButtonPos(screenPos5);
                    button.setPosition(screenPos6.x(), screenPos6.y());
                    this.recipeBookButton.setPosition(screenPos5.x(), this.recipeBookButton.getY());
                }
        ){
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                Identifier identifier = this.textures.get(this.isNarratable(), this.isSelected());
                context.drawTexture(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(),0,0, this.width, this.height, this.width, this.height);
            }

        };
        this.addDrawableChild(this.blueprintButton);
        this.addSelectableChild((Element & Selectable)this.blueprintWidget);

        if(TellMeWhatINeed.bluePrintBookEnabled) {
            if(this.recipeBook.isOpen()) this.recipeBook.toggleOpen();
            this.x = this.blueprintWidget.findLeftEdge(this.width, this.backgroundWidth);
            this.blueprintButton.onPress();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderBlueprintWidget(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.blueprintWidget.isOpen() && this.narrow) {
            this.renderBackground(context, mouseX, mouseY, delta);
            this.blueprintWidget.render(context, mouseX, mouseY, delta);
        } else {
            this.blueprintWidget.render(context, mouseX, mouseY, delta);
        }
        this.blueprintWidget.drawTooltip(context, mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void handleBlueprintClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        if(this.blueprintWidget.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.blueprintWidget);
            ci.setReturnValue(true);
        }
        if (this.narrow && this.blueprintWidget.isOpen()) {
            ci.cancel();
       }
    }

}
