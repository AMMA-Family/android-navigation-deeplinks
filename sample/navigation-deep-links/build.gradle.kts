@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("java-library")
    id("kotlin")
}

sourceSets {
    named("main") {
        java.srcDirs("src/main/kotlin")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
}