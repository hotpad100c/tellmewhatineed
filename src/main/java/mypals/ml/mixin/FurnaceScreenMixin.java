package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.GUI.BlueprintWidget;
import mypals.ml.TellMeWhatINeed;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.recipebook.AbstractFurnaceRecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static mypals.ml.GUI.BlueprintWidget.BLUEPRINT_BUTTON_TEXTURES;

@Mixin(AbstractFurnaceScreen.class)
public abstract class FurnaceScreenMixin<T extends AbstractFurnaceScreenHandler> extends HandledScreen<T> implements RecipeBookProvider {
    @Final
    @Shadow
    public AbstractFurnaceRecipeBookScreen recipeBook;
    @Shadow private boolean narrow;
    @Unique
    private BlueprintWidget blueprintWidget;
    @Unique
    private TexturedButtonWidget blueprintButton;
    @Unique
    private TexturedButtonWidget recipeBookButton;


    public FurnaceScreenMixin(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @WrapMethod(method = "init")
    public void init(Operation<Void> original) {
        super.init();
        this.narrow = this.width < 379;
        this.blueprintWidget = new BlueprintWidget();
        this.blueprintWidget.initialize(this.width, this.height, this.client, this.narrow);
        this.recipeBook.initialize(this.width, this.height, this.client, this.narrow, this.handler);
        this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);

        this.addDrawableChild(recipeBookButton = new TexturedButtonWidget(this.x + 20, this.height / 2 - 49, 20, 18, RecipeBookWidget.BUTTON_TEXTURES, button -> {
            this.recipeBook.toggleOpen();
            if(this.blueprintWidget.isOpen()) this.blueprintWidget.toggleOpen();
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
            button.setPosition(this.x + 20, this.height / 2 - 49);
            this.blueprintButton.setPosition(this.x + 20, this.blueprintButton.getY());
        }));
        this.addSelectableChild(this.recipeBook);
        this.titleX = 29;

        int buttonY = this.height / 2 - 70;
        this.blueprintButton = new TexturedButtonWidget(
                this.x + 20, buttonY, 20, 18,
                BLUEPRINT_BUTTON_TEXTURES,
                button -> {
                    if(this.recipeBook.isOpen()) this.recipeBook.toggleOpen();

                    this.blueprintWidget.toggleOpen();
                    this.x = this.blueprintWidget.findLeftEdge(this.width, this.backgroundWidth);
                    button.setPosition(this.x + 20, buttonY);
                    this.recipeBookButton.setPosition(this.x + 20, this.recipeBookButton.getY());
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
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
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
