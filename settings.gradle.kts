pluginManagement {
    repositories {
        maven {
            setUrl("./repo")
        }

        gradlePluginPortal()
    }
}

include(
    ":generator",
    ":gradle-plugin",
    ":sample:app",
    ":sample:module-a",
    ":sample:module-b",
    ":navigation-deep-links"
)

rootProject.name = "android-navigation-deeplinks"