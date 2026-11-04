pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
    }
}

plugins {
    id("com.possible-triangle.helper") version ("1.4")
}

// TODO: remove. somethingcatchy registry is 502ing so the helper cant fetch plugin versions
helper {
    versionStrategy.set(com.possible_triangle.gradle.settings.ResolutionStrategy.NONE)
}
pluginManagement.resolutionStrategy.eachPlugin {
    if (requested.id.namespace == "com.possible-triangle" && requested.version == null) useVersion("1.4.259")
}

include("common", "fabric", "neoforge")