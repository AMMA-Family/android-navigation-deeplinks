@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("kotlin")
    `java-library`
    `maven-publish`
    signing
}

sourceSets.forEach { it.java.srcDirs("src/${it.name}/kotlin") }

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xno-kotlin-nothing-value-exception", "-Xinline-classes")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(Dependency.Kotlin.X.Coroutines.core)

    implementation(Dependency.xmlpull)
    implementation(Dependency.kotlinpoet)

    testImplementation(kotlin("test"))
}

publish(
    publishing = publishing,
    signing = signing,
    artifactId = project.requireProperty(name = "publication.generator.artifactId"),
    publicationType = PublicationType.JavaLib(java)
)
