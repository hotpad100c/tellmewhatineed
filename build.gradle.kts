plugins {
    id("net.fabricmc.fabric-loom-remap")
}

version = "${property("mod.version")}+${stonecutter.current.version}"
group = property("mod.group") as String
base.archivesName = property("mod.id") as String

val requiredJava = JavaVersion.VERSION_21
val accesswidener = "tellmewhatineed-1.21.accesswidener"

repositories {
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    modImplementation("maven.modrinth:malilib:${property("deps.malilib")}")
    modImplementation("maven.modrinth:litematica:${property("deps.litematica")}")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    accessWidenerPath = rootProject.file("src/main/resources/accesswideners/$accesswidener")

    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run"
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks {
    processResources {
        val props = mapOf(
            "version" to project.property("mod.version"),
            "minecraft" to project.property("mod.mc_dep"),
            "java_version" to requiredJava.majorVersion,
            "aw_file" to accesswidener
        )
        inputs.properties(props)

        filesMatching("fabric.mod.json") { expand(props) }
        filesMatching("accesswideners/*") {
            if (name != accesswidener) exclude()
        }
        filesMatching("*.mixins.json") { expand("java" to "JAVA_${requiredJava.majorVersion}") }
    }

    jar {
        from(rootProject.file("LICENSE")) { rename { "${it}_${project.base.archivesName.get()}" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
        dependsOn("build")
    }
}
