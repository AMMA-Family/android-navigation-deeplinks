@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("java-library")
    id("kotlin")
    id("maven-publish")
}

sourceSets {
    val main by getting {
        java.srcDirs("src/main/kotlin")
    }
}

publishing {
    publications {
        register("mavenPublish", MavenPublication::class.java) {
            groupId = "family.amma"
            artifactId = "generator"
            version = "0.1.0"

            from(components.getByName("java"))
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xno-kotlin-nothing-value-exception", "-Xinline-classes")
    }
}

dependencies {
    val kotlinVersion: String by rootProject.extra

    implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = kotlinVersion)
    implementation(group = "xmlpull", name = "xmlpull", version = "1.1.3.1")
    implementation(group = "com.squareup", name = "kotlinpoet", version = "1.7.2")
}

/*
val publicationGroupId: String = project.requireProperty(name = "publication.plugin.groupId")
val publicationVersionName: String = project.requireProperty(name = "publication.deepLinkGenerator.versionName")

group = publicationGroupId
version = publicationVersionName

afterEvaluate {
    publishing {
        configure(
            project = project,
            groupId = publicationGroupId,
            artifactId = project.requireProperty(name = "publication.deepLinkGenerator.artifactId"),
            versionName = publicationVersionName,
            publicationType = PublicationType.JavaLib
        )
    }
}
*/
