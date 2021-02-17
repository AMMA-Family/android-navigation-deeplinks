package family.amma.deep_link.gradle_plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import groovy.util.XmlSlurper
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

private const val PLUGIN_DIRNAME = "navigation-deep-links"
private const val GENERATED_PATH = "generated/source/$PLUGIN_DIRNAME"
private const val PLUGIN_PATH = "$PLUGIN_DIRNAME/src/main/kotlin"

@Suppress("unused")
class DeepLinksPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(DeepLinksPluginExtension.NAME, DeepLinksPluginExtension::class.java)
        val baseExtension = project.extensions.findByType(BaseExtension::class.java)
            ?: throw GradleException("deep links plugin must be used with android plugin")
        val outputDir = File(project.rootDir, PLUGIN_PATH)
        forEachVariants(baseExtension) { variant: BaseVariant ->
            val buildDir = File(project.buildDir, GENERATED_PATH)
            val task = project
                .tasks
                .create("generateDeepLinks${variant.name.capitalize()}", ArgumentsGenerationTask::class.java) { task ->
                    task.rFilePackage = variant.rFilePackage()
                    task.applicationId = variant.applicationId
                    task.navigationFiles = navigationFiles(variant)
                    task.outputDir = outputDir
                    task.pluginDir = File(project.rootDir, PLUGIN_DIRNAME)
                    task.buildDir = buildDir
                }
            variant.registerJavaGeneratingTask(task, buildDir)
        }
    }
}

private fun forEachVariants(extension: BaseExtension, action: (BaseVariant) -> Unit) {
    when (extension) {
        is AppExtension -> extension.applicationVariants.all(action)
        is LibraryExtension -> extension.libraryVariants.all(action)
        else -> throw GradleException("deep links plugin must be used with android app or library")
    }
}

private fun navigationFiles(variant: BaseVariant): List<File> =
    variant.sourceSets
        .flatMap { it.resDirectories }
        .mapNotNull { resDir ->
            File(resDir, "navigation").takeIf { it.exists() && it.isDirectory }
        }
        .flatMap { navFolder -> navFolder.listFiles().orEmpty().asIterable() }
        .groupBy(File::getName)
        .values
        .map { it.last() }

private fun BaseVariant.rFilePackage(): String {
    val mainSourceSet = sourceSets.find { it.name == "main" }
    val sourceSet = mainSourceSet ?: sourceSets.first()
    val manifest = sourceSet.manifestFile
    val parsed = XmlSlurper(false, false).parse(manifest)
    return parsed.getProperty("@package").toString()
}
