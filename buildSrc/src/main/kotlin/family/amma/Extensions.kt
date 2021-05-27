import groovy.lang.MissingPropertyException
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
import java.io.File
import java.util.Properties

fun Project.requireProperty(name: String): String =
    findProperty(name)?.toString() ?: throw MissingPropertyException("Not found property with name: $name")

fun Project.localProperties(): Properties {
    val local = Properties()
    val localProperties: File = rootProject.file("local.properties")
    if (localProperties.exists()) {
        localProperties.inputStream().use { local.load(it) }
    }
    return local
}

sealed class PublicationType {
    object Android : PublicationType()
    object Mpp : PublicationType()
    data class JavaLib(val java: JavaPluginExtension) : PublicationType()
}

fun MavenPom.config(project: Project) {
    name.set(project.requireProperty(name = "publication.groupId"))
    description.set(project.requireProperty("publication.description"))
    url.set(project.requireProperty("publication.url"))
    licenses {
        license {
            name.set(project.requireProperty("publication.license.name"))
            url.set(project.requireProperty("publication.license.url"))
        }
    }
    developers {
        developer {
            name.set(project.requireProperty("publication.developer.name"))
            email.set(project.requireProperty("publication.developer.email"))
            organization.set(project.requireProperty("publication.developer.email"))
            organizationUrl.set(project.requireProperty("publication.developer.email"))
        }
    }
    scm {
        connection.set(project.requireProperty("publication.scm.connection"))
        developerConnection.set(project.requireProperty("publication.scm.developerConnection"))
        url.set(project.requireProperty("publication.scm.url"))
    }
}

fun Project.publish(
    publishing: PublishingExtension,
    signing: SigningExtension,
    artifactId: String,
    publicationType: PublicationType
) {
    val groupId: String = project.requireProperty(name = "publication.groupId")
    val versionName: String = project.requireProperty(name = "publication.versionName")

    group = groupId
    version = versionName

    val localProperties = project.localProperties()

    extra["signing.keyId"] = localProperties.getProperty("publication.signing.keyId")
    extra["signing.password"] = localProperties.getProperty("publication.signing.password")
    extra["signing.secretKeyRingFile"] = "$rootDir/${localProperties.getProperty("publication.signing.secretKeyRingFileName")}"

    publishing.repositories {
        maven {
            name = "mavenCentral"
            setUrl(
                if (project.version.let { it as String }.endsWith("-SNAPSHOT"))
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            )

            credentials {
                username = localProperties.getProperty("publication.user.login")
                password = localProperties.getProperty("publication.user.password")
            }
        }
    }

    fun MavenPublication.metadata() {
        this.groupId = groupId
        this.artifactId = artifactId
        this.version = versionName
    }

    when (publicationType) {
        PublicationType.Mpp -> {
            publishing.publications.withType<MavenPublication>().configureEach {
                metadata()
                pom { config(project) }
            }
            signing.sign(publishing.publications)
        }

        PublicationType.Android -> {
            publishing.publications.register("mavenAndroid", MavenPublication::class.java) {
                metadata()
                from(project.components.getByName("release"))
                pom { config(project) }
            }
            signing.sign(publishing.publications["mavenAndroid"])
        }

        is PublicationType.JavaLib -> {
            @Suppress("UnstableApiUsage")
            with(publicationType.java) {
                withJavadocJar()
                withSourcesJar()
            }
            publishing.publications.create("mavenJava", MavenPublication::class.java) {
                metadata()
                from(project.components.getByName("java"))
                pom { config(project) }
            }
            signing.sign(publishing.publications["mavenJava"])
        }
    }
}
