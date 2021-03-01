pluginManagement {
    repositories {
        maven {
            setUrl("./repo")
        }

        gradlePluginPortal()
    }
}

include(":generator", ":gradle-plugin")
includeBuild("sample")

rootProject.name = "android-navigation-deeplinks"