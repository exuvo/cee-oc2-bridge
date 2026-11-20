pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "cee_oc2"

// Build against the local oc2 checkout. Remove to use the published artifact instead.
includeBuild("../oc2") {
    dependencySubstitution {
        substitute(module("li.cil.oc2:oc2-1.21.1-neoforge")).using(project(":neoforge"))
    }
}
gradle.rootProject {
    plugins.withId("java") {
        configurations.named("compileClasspath") {
            attributes { attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR)) }
        }
    }
}
