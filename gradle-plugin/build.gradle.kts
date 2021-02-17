@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("java-library")
    id("java-gradle-plugin")
    id("kotlin")
    id("maven-publish")
}

gradlePlugin {
    plugins {
        create("deepLinks") {
            id = "family.amma.deepLinks"
            implementationClass = "family.amma.deep_link.gradle_plugin.DeepLinksPlugin"
        }
    }
}

sourceSets {
    val main by getting {
        java.srcDirs("src/main/kotlin")
    }
}

dependencies {
    val kotlinVersion: String by rootProject.extra
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    val buildGradleVersion: String by rootProject.extra
    implementation("com.android.tools.build:gradle:$buildGradleVersion")

    implementation(gradleApi())
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0-RC")
    implementation("family.amma:generator:0.1.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xno-kotlin-nothing-value-exception")
    }
}

/*
val publicationGroupId: String = project.requireProperty(name = "publication.plugin.groupId")
val publicationVersionName: String = project.requireProperty(name = "publication.deepLinkPlugin.versionName")

group = publicationGroupId
version = publicationVersionName

afterEvaluate {
    publishing {
        configure(
            project = project,
            groupId = publicationGroupId,
            artifactId = project.requireProperty(name = "publication.deepLinkPlugin.artifactId"),
            versionName = publicationVersionName,
            publicationType = PublicationType.JavaLib
        )
    }
}
*/
