buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        mavenLocal()
    }

    dependencies {
        classpath(BuildPlugin.gradle)
        classpath(BuildPlugin.kotlin)
        classpath(BuildPlugin.safeArgs)
        classpath("family.amma:deeplinks-gradle-plugin:1.5.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
}
