@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(AndroidProject.compileSdkVersion)
    buildToolsVersion(AndroidProject.buildToolsVersion)

    defaultConfig {
        applicationId = "family.amma.deeplinks"
        minSdkVersion(AndroidProject.minSdkVersion)
        targetSdkVersion(AndroidProject.targetSdkVersion)
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    sourceSets {
        val main by getting {
            java.srcDirs("src/main/kotlin")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = AndroidProject.jvmTarget
    }
}

dependencies {
    implementation(project(":module-a"))
    implementation(project(":module-b"))

    implementation(kotlin("stdlib"))

    implementation(Dependency.AndroidX.coreKtx)
    implementation(Dependency.AndroidX.appcompat)
    implementation(Dependency.AndroidX.constraintlayout)

    implementation(Dependency.AndroidX.Navigation.fragment)
    implementation(Dependency.AndroidX.Navigation.ui)
}
