pluginManagement {
    repositories {
        maven {
            setUrl("./repo")
        }

        gradlePluginPortal()
    }
}

include(":generator", ":gradle-plugin")

rootProject.name = "android-navigation-deeplinks"