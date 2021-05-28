@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("kotlin")
    `java-gradle-plugin`
    `maven-publish`
    signing
}

group = requireProperty(name = "publication.groupId")
version = requireProperty(name = "publication.versionName")

localProperties().let {
    extra["gradle.publish.key"] = it.getProperty("gradle.publish.key")
    extra["gradle.publish.secret"] = it.getProperty("gradle.publish.secret")
}

gradlePlugin {
    plugins {
        create("DeepLinksPlugin") {
            id = "family.amma.deeplinks"
            displayName = "Generate kotlin files by navigation deep links"
            implementationClass = "family.amma.deep_link.gradle_plugin.DeepLinksPlugin"
            description = "This library goes through your navigation files, pulls out information about deep links and generates kotlin code"
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

configure<PublishingExtension> {
    publications {
        afterEvaluate {
            named<MavenPublication>("pluginMaven") {
                signing.sign(this)
                pom { config(project) }
            }
            named<MavenPublication>("DeepLinksPluginPluginMarkerMaven") {
                signing.sign(this)
                pom { config(project) }
            }
        }
    }
}
