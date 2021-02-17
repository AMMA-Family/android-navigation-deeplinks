buildscript {
    val kotlinVersion: String by extra("1.4.30")
    val buildGradleVersion: String by extra("4.1.2")
    val navVersion: String by extra("2.3.0")

    repositories {
        google()
        jcenter()
        mavenLocal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:$buildGradleVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$navVersion")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenLocal()
    }
}
