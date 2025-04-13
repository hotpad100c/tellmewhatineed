package mypals.ml.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mypals.ml.GUI.BlueprintWidget;
import mypals.ml.TellMeWhatINeed;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
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
import static net.minecraft.client.gui.screen.ingame.InventoryScreen.drawEntity;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin  extends AbstractInventoryScreen<PlayerScreenHandler> implements RecipeBookProvider{
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


    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.titleX = 97;
    }

    @Override
    public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int i = this.x;
        int j = this.y;
        context.drawTexture(BACKGROUND_TEXTURE, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        drawEntity(context, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F, mouseX, mouseY, this.client.player);
    }

    @WrapMethod(method = "init")
    private void init(Operation<Void> original) {
        if (this.client.interactionManager.hasCreativeInventory()) {
            this.client.setScreen(new CreativeInventoryScreen(this.client.player, this.client.player.networkHandler.getEnabledFeatures(), (Boolean)this.client.options.getOperatorItemsTab().getValue()));
        } else {
            super.init();
            this.narrow = this.width < 379;
            this.blueprintWidget = new BlueprintWidget();
            this.blueprintWidget.initialize(this.width, this.height, this.client, this.narrow);
            this.recipeBook.initialize(this.width, this.height, this.client, this.narrow, this.handler);
            this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);

            this.addDrawableChild(recipeBookButton = new TexturedButtonWidget(this.x + 104, this.height / 2 - 22, 20, 18, RecipeBookWidget.BUTTON_TEXTURES, button -> {
                this.recipeBook.toggleOpen();
                if (this.blueprintWidget.isOpen()) this.blueprintWidget.toggleOpen();
                this.x = this.recipeBook.findLeftEdge(this.width, this.backgroundWidth);
                button.setPosition(this.x + 104, this.height / 2 - 22);
                this.blueprintButton.setPosition(this.x + 104 + 21, this.blueprintButton.getY());
            }));
            this.addSelectableChild(this.recipeBook);
            this.titleX = 29;

            int buttonY = this.height / 2 - 22;
            this.blueprintButton = new TexturedButtonWidget(
                    this.x + 104 + 21, buttonY, 20, 18,
                    BLUEPRINT_BUTTON_TEXTURES,
                    button -> {
                        if (this.recipeBook.isOpen()) this.recipeBook.toggleOpen();

                        this.blueprintWidget.toggleOpen();
                        this.x = this.blueprintWidget.findLeftEdge(this.width, this.backgroundWidth);
                        button.setPosition(this.x + 104 + 21, buttonY);
                        this.recipeBookButton.setPosition(this.x + 104, this.recipeBookButton.getY());
                    }
            ) {
                @Override
                public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                    Identifier identifier = this.textures.get(this.isNarratable(), this.isSelected());
                    context.drawTexture(identifier, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
                }

            };
            this.addDrawableChild(this.blueprintButton);
            this.addSelectableChild((Element & Selectable) this.blueprintWidget);

            if (TellMeWhatINeed.bluePrintBookEnabled) {
                if (this.recipeBook.isOpen()) this.recipeBook.toggleOpen();
                this.x = this.blueprintWidget.findLeftEdge(this.width, this.backgroundWidth);
                this.blueprintButton.onPress();
            }
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

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void handleBlueprintClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> ci) {
        if (this.narrow && this.blueprintWidget.isOpen()) {
            ci.cancel();
        }
    }

    @Override
    public void refreshRecipeBook() {
        this.recipeBook.refresh();
    }

    @Override
    public RecipeBookWidget getRecipeBookWidget() {
        return this.recipeBook;
    }
    @Override
    public void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, 4210752, false);
    }
}