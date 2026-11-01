plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.15.3" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.15.3" apply false
}

stonecutter active "1.21.7"

stonecutter tasks {
    order("runClient")
}
