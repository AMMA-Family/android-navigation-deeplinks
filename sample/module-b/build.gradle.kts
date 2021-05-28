@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("com.android.library")
    kotlin("android")
    id("family.amma.deeplinks")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(AndroidProject.compileSdkVersion)
    buildToolsVersion(AndroidProject.buildToolsVersion)

    defaultConfig {
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

configure<family.amma.deep_link.gradle_plugin.DeepLinksPluginExtension> {
    generateByDestinations = true
    generateUriHierarchy = true
    generateAdditionalInfo = true
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(Dependency.AndroidX.coreKtx)
    implementation(Dependency.AndroidX.appcompat)
    implementation(Dependency.AndroidX.constraintlayout)

    implementation(Dependency.AndroidX.Navigation.fragment)
    implementation(Dependency.AndroidX.Navigation.ui)
}
