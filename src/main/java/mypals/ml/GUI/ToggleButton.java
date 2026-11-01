package mypals.ml.GUI;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}

public class ToggleButton extends AbstractWidget {
    protected WidgetSprites sprites;
    protected boolean isStateTriggered;

    public ToggleButton(int x, int y, int width, int height, boolean triggered) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.isStateTriggered = triggered;
    }

    public void initTextureValues(WidgetSprites sprites) {
        this.sprites = sprites;
    }

    public void setStateTriggered(boolean triggered) {
        this.isStateTriggered = triggered;
    }

    public boolean isStateTriggered() {
        return this.isStateTriggered;
    }

    public void onPress() {
    }

    //? if >=1.21.9 {
    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        this.onPress();
    }
    //?} else {
    /*@Override
    public void onClick(double mouseX, double mouseY) {
        this.onPress();
    }
    *///?}

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (this.sprites != null) {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprites.get(this.isStateTriggered, this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    public static boolean click(AbstractWidget widget, double mouseX, double mouseY, int button) {
        //? if >=1.21.9 {
        return widget.mouseClicked(new MouseButtonEvent(mouseX, mouseY, new net.minecraft.client.input.MouseButtonInfo(button, 0)), false);
        //?} else
        /*return widget.mouseClicked(mouseX, mouseY, button);*/
    }
}
