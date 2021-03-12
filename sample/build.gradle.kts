buildscript {
    repositories {
        google()
        maven("https://plugins.gradle.org/m2/")
        mavenLocal()
    }

    dependencies {
        classpath(BuildPlugin.gradle)
        classpath(BuildPlugin.kotlin)
        classpath(BuildPlugin.safeArgs)
        classpath("family.amma:deepLinks:0.1.5")
    }
}

allprojects {
    repositories {
        google()
        maven("https://plugins.gradle.org/m2/")
        mavenLocal()
    }
}
