package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mypals.ml.GUI.BlueprintWidget;
import mypals.ml.TellMeWhatINeed;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.GUI.BlueprintWidget.BLUEPRINT_BUTTON_TEXTURES;
import static mypals.ml.TellMeWhatINeed.MOD_ID;

@Mixin(CraftingScreen.class)
public abstract class CraftingScreenMixin extends HandledScreen<CraftingScreenHandler> {
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


    public CraftingScreenMixin(CraftingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @WrapMethod(method = "init")
    protected void init(Operation<Void> original) {
        super.init();
        this.narrow = this.width < 379;
        this.blueprintWidget = new BlueprintWidget();
        this.blueprintWidget.initialize(this.width, this.height, this.client, this.narrow, this.handler);
        this.recipeBook.initialize(this.width, this.height, this.client, this.narrow, this.handler);
        this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);

        this.addDrawableChild(recipeBookButton = new TexturedButtonWidget(this.x + 5, this.height / 2 - 49, 20, 18, RecipeBookWidget.BUTTON_TEXTURES, button -> {
            this.recipeBook.toggleOpen();
            if(this.blueprintWidget.isOpen()) this.blueprintWidget.toggleOpen();
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            button.setPosition(this.x + 5, this.height / 2 - 49);
            this.blueprintButton.setPosition(this.x + 5, this.blueprintButton.getY());
        }));
        this.addSelectableChild(this.recipeBook);
        this.titleX = 29;

        int buttonY = this.height / 2 - 70;
        this.blueprintButton = new TexturedButtonWidget(
                this.x + 5, buttonY, 20, 18,
                BLUEPRINT_BUTTON_TEXTURES,
                button -> {
                    if(this.recipeBook.isOpen()) this.recipeBook.toggleOpen();

                    this.blueprintWidget.toggleOpen();
                    this.x = this.blueprintWidget.findLeftEdge(this.width, this.backgroundWidth);
                    button.setPosition(this.x + 5, buttonY);
                    this.recipeBookButton.setPosition(this.x + 5, this.recipeBookButton.getY());
                }
        ){
            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                Identifier identifier = this.textures.get(this.isNarratable(), this.isSelected());
                context.drawTexture(identifier, this.getX(), this.getY(),0,0, this.width, this.height, this.width, this.height);
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
       if (this.narrow && this.blueprintWidget.isOpen()) {
            ci.cancel();
       }
    }

}
