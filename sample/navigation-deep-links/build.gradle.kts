@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("java-library")
    id("kotlin")
}

sourceSets {
    val main by getting {
        java.srcDirs("src/main/kotlin")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.31")
}