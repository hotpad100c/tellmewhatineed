package mypals.ml.GUI;

import fi.dy.masa.malilib.util.data.Color4f;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import static mypals.ml.TellMeWhatINeed.MOD_ID;

public class BlueprintGroupButtonWidget extends ToggleButton {
    private static final WidgetSprites TEXTURES = new WidgetSprites(Identifier.withDefaultNamespace("recipe_book/tab"), Identifier.withDefaultNamespace("recipe_book/tab_selected"));
    private final BlueprintGroup blueprintGroup;
    private float bounce;
    public final Identifier icon = Identifier.fromNamespaceAndPath(MOD_ID,"textures/blueprint.png");
    public BlueprintGroupButtonWidget(BlueprintGroup category) {
        super(0, 0, 35, 27, false);
        this.blueprintGroup = category;
        this.initTextureValues(TEXTURES);
        this.bounce = 5f;
    }
    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (this.sprites != null) {
            if (this.bounce > 0.0F) {
                float f = 1.0F + 0.1F * (float)Math.sin(this.bounce / 15.0F * (float) Math.PI);
                context.pose().pushMatrix();
                context.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12));
                context.pose().scale(1.0F, f);
                context.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)));
            }

            Minecraft minecraftClient = Minecraft.getInstance();
            Identifier identifier = this.sprites.get(true, this.isStateTriggered);
            int i = this.getX();
            if (this.isStateTriggered) {
                i -= 2;
            }

            context.blitSprite(RenderPipelines.GUI_TEXTURED,identifier, i, this.getY(), this.width, this.height);
            boolean mousePointed = this.active && this.visible && mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && mouseX < (double)(this.getX() + this.getWidth()) && mouseY < (double)(this.getY() + this.getHeight());

            if(mousePointed){
                context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, getToolTips(), mouseX-5, mouseY - 30);
            }
            this.renderIcons(context);

            if (this.bounce > 0.0F) {
                context.pose().popMatrix();
                this.bounce -= delta;
            }

        }
    }

    public BlueprintGroup getBlueprintGroup() {
        return blueprintGroup;
    }
    public List<Component> getToolTips() {
        BlockPos pos = blueprintGroup.placement.getOrigin();
        return List.of(Component.nullToEmpty(this.blueprintGroup.getName()),
                Component.nullToEmpty("SubRegionCount : " + blueprintGroup.placement.getSubRegionCount()),
                Component.nullToEmpty("Origin : { " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " }"),
                Component.nullToEmpty("Author : " + blueprintGroup.placement.getSchematic().getMetadata().getAuthor())
        );
    }
    private void renderIcons(GuiGraphicsExtractor context) {
        int i = this.isStateTriggered ? -2 : 0;
        Color4f color = blueprintGroup.placement.getBoxesBBColor();
        //RenderSystem.setShaderColor(color.r,color.g,color.b,1);
        context.fakeItem(Items.PAPER.getDefaultInstance(), this.getX() + 9 + i, this.getY() + 5);
        //RenderSystem.setShaderColor(1,1,1,1);
    }
}
