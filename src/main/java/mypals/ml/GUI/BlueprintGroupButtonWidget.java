package mypals.ml.GUI;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ToggleButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static mypals.ml.TellMeWhatINeed.MOD_ID;

public class BlueprintGroupButtonWidget extends ToggleButtonWidget {
    private static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("recipe_book/tab"), Identifier.ofVanilla("recipe_book/tab_selected"));
    private final BlueprintGroup blueprintGroup;
    private float bounce;
    public final Identifier icon = Identifier.of(MOD_ID,"textures/blueprint.png");
    public BlueprintGroupButtonWidget(BlueprintGroup category) {
        super(0, 0, 35, 27, false);
        this.blueprintGroup = category;
        this.setTextures(TEXTURES);
        this.bounce = 5f;
    }
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.textures != null) {
            if (this.bounce > 0.0F) {
                float f = 1.0F + 0.1F * (float)Math.sin(this.bounce / 15.0F * (float) Math.PI);
                context.getMatrices().push();
                context.getMatrices().translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
                context.getMatrices().scale(1.0F, f, 1.0F);
                context.getMatrices().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
            }

            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            RenderSystem.disableDepthTest();
            Identifier identifier = this.textures.get(true, this.toggled);
            int i = this.getX();
            if (this.toggled) {
                i -= 2;
            }

            context.drawGuiTexture(RenderLayer::getGuiTextured,identifier, i, this.getY(), this.width, this.height);
            boolean mousePointed = this.active && this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && mouseX < (double)(this.getX() + this.getWidth()) && mouseY < (double)(this.getY() + this.getHeight());

            if(mousePointed){
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, getToolTips(), mouseX-5, mouseY - 30);
            }
            this.renderIcons(context, minecraftClient.getItemRenderer());

            RenderSystem.enableDepthTest();
            if (this.bounce > 0.0F) {
                context.getMatrices().pop();
                this.bounce -= delta;
            }

        }
    }

    public BlueprintGroup getBlueprintGroup() {
        return blueprintGroup;
    }
    public List<Text> getToolTips() {
        BlockPos pos = blueprintGroup.placement.getOrigin();
        return List.of(Text.of(this.blueprintGroup.getName()),
                Text.of("SubRegionCount : " + blueprintGroup.placement.getSubRegionCount()),
                Text.of("Origin : { " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " }"),
                Text.of("Author : " + blueprintGroup.placement.getSchematic().getMetadata().getAuthor())
        );
    }
    private void renderIcons(DrawContext context, ItemRenderer itemRenderer) {
        int i = this.toggled ? -2 : 0;
        Color4f color = blueprintGroup.placement.getBoxesBBColor();
        //RenderSystem.setShaderColor(color.r,color.g,color.b,1);
        context.drawItemWithoutEntity(Items.PAPER.getDefaultStack(), this.getX() + 9 + i, this.getY() + 5);
        //RenderSystem.setShaderColor(1,1,1,1);
    }
}
