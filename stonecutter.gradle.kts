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
    }
}
