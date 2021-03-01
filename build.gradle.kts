buildscript {
    val kotlinVersion: String by extra("1.4.31")
    val buildGradleVersion: String by extra("4.1.2")

    repositories {
        google()
        jcenter()
        mavenLocal()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:$buildGradleVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenLocal()
    }
}
