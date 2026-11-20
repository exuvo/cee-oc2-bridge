plugins {
    `java-library`
    id("net.neoforged.moddev") version "2.0.147"
}

version = "1.0.0"
group = "se.exuvo.ceeoc2"

base {
    archivesName = "cee_oc2-1.21.1"
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

repositories {
    maven("https://fnuecke.github.io/maven") {
        content {
            includeGroup("li.cil.oc2")
            includeGroup("li.cil.markdown_manual")
        }
    }
    maven("https://maven.architectury.dev") {
        content { includeGroup("dev.architectury") }
    }
    maven("https://maven.createmod.net") {
        content { includeGroup("com.simibubi.create") }
    }
}

neoForge {
    version = "21.1.252"

    mods {
        register("cee_oc2") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    compileOnly("li.cil.oc2:oc2-1.21.1-neoforge:0.7.1") { isTransitive = false }
    // Create Electro Energetics is not published; build it with `./gradlew jar` in the sibling checkout first.
    compileOnly(fileTree("../Create-Electro-Energetics/build/libs") { include("electroenergetics-*.jar") })
    compileOnly("com.simibubi.create:create-1.21.1:6.0.10-280:slim") { isTransitive = false }
}
