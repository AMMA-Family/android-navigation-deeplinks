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

publishing {
    publications {
        register("mavenPublish", MavenPublication::class.java) {
            groupId = "family.amma"
            artifactId = "deepLinks"
            version = "0.1.7"

            from(components.getByName("java"))
        }
    }
}

sourceSets {
    val main by getting {
        java.srcDirs("src/main/kotlin")
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(BuildPlugin.gradle)
    implementation(gradleApi())
    implementation(Dependency.Kotlin.X.Serialization.core)
    implementation(Dependency.Kotlin.X.Coroutines.core)

    implementation("family.amma:generator:0.1.5")
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
