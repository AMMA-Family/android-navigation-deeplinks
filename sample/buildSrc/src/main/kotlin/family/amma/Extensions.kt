package family.amma

import org.gradle.api.Project
import java.io.File

fun Project.localProperties(): java.util.Properties {
    val local = java.util.Properties()
    val localProperties: File = rootProject.file("local.properties")
    if (localProperties.exists()) {
        localProperties.inputStream().use { local.load(it) }
    }
    return local
}
