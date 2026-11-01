plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.19.3" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.19.3" apply false
}

stonecutter active "1.21.11"

stonecutter tasks {
    order("runClient")
}

stonecutter parameters {
    replacements {
        string(eval(current.version, "<1.21.11")) {
            replace("Identifier", "ResourceLocation")
        }
        string(eval(current.version, "<26.1")) {
            replace("GuiGraphicsExtractor;", "GuiGraphics;")
        }
        string(eval(current.version, "<26.1")) {
            replace("GuiGraphicsExtractor context", "GuiGraphics context")
        }
        string(eval(current.version, "<26.1")) {
            replace("GuiGraphicsExtractor drawContext", "GuiGraphics drawContext")
        }
        string(eval(current.version, "<26.1")) {
            replace("extractWidgetRenderState(", "renderWidget(")
        }
        string(eval(current.version, "<26.1")) {
            replace("extractContents(", "renderContents(")
        }
        string(eval(current.version, "<26.1")) {
            replace(".extractRenderState(context", ".render(context")
        }
        string(eval(current.version, "<26.1")) {
            replace("public void extractRenderState(", "public void render(")
        }
        string(eval(current.version, "<26.1")) {
            replace("method = \"extractRenderState\"", "method = \"render\"")
        }
        string(eval(current.version, "<26.1")) {
            replace("this.extractBackground(context", "this.renderBackground(context")
        }
        string(eval(current.version, "<26.1")) {
            replace(".text(", ".drawString(")
        }
        string(eval(current.version, "<26.1")) {
            replace(".fakeItem(", ".renderFakeItem(")
        }
        string(eval(current.version, "<26.2")) {
            replace("Minecraft.getInstance().gui.screen()", "Minecraft.getInstance().screen")
        }
    }
}
