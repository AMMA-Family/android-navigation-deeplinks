@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("kotlin")
    `java-library`
    `java-gradle-plugin`
    `maven-publish`
    signing
}

gradlePlugin {
    plugins {
        create("DeepLinksPlugin") {
            id = "family.amma.deeplinks"
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
    implementation(kotlin("stdlib"))

    implementation(BuildPlugin.gradle)
    implementation(gradleApi())
    implementation(Dependency.Kotlin.X.Serialization.core)
    implementation(Dependency.Kotlin.X.Coroutines.core)

    val publicationVersionName = project.requireProperty(name = "publication.versionName")
    implementation("family.amma:deeplinks-generator:$publicationVersionName")
}

publish(
    publishing = publishing,
    signing = signing,
    artifactId = project.requireProperty(name = "publication.gradleplugin.artifactId"),
    publicationType = PublicationType.JavaLib(java)
)
