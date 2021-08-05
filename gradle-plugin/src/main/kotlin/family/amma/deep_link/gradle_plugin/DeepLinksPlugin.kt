package family.amma.deep_link.gradle_plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import groovy.xml.XmlSlurper
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.ProviderFactory
import java.io.File

private const val PLUGIN_DIRNAME = "navigation-deep-links"
private const val GENERATED_PATH = "generated/source/$PLUGIN_DIRNAME"
private const val PLUGIN_PATH = "$PLUGIN_DIRNAME/src/main/kotlin"

@Suppress("unused")
abstract class DeepLinksPlugin protected constructor(val providerFactory: ProviderFactory) : Plugin<Project> {
    override fun apply(project: Project) {
        val pluginExtension = project.extensions.create(DeepLinksPluginExtension.NAME, DeepLinksPluginExtension::class.java)

        val baseExtension = project.extensions.findByType(BaseExtension::class.java)
            ?: throw GradleException("deep links plugin must be used with android plugin")
        val outputDir = File(project.rootDir, PLUGIN_PATH)
        val pluginDir = File(project.rootDir, PLUGIN_DIRNAME)
        val buildDir = File(project.buildDir, GENERATED_PATH)

        forEachVariants(baseExtension) { variant: BaseVariant ->
            val generateDeepLinksTask = project
                .tasks
                .create("generateDeepLinks${variant.name.capitalize()}", GenerateDeepLinksTask::class.java) { task ->
                    task.rFilePackage = variant.rFilePackage()
                    task.applicationId = variant.applicationId
                    task.generatorParams = pluginExtension.toGeneratorParams()
                    task.navigationFiles = navigationFiles(variant, project)
                    task.outputDir = outputDir
                    task.pluginDir = pluginDir
                    task.buildDir = buildDir
                }
            variant.applicationIdTextResource?.let { generateDeepLinksTask.dependsOn(it) }
            variant.registerJavaGeneratingTask(generateDeepLinksTask, buildDir)
        }
    }

    private fun navigationFiles(variant: BaseVariant, project: Project): FileCollection {
        val fileProvider = providerFactory.provider {
            variant.sourceSets
                .flatMap { it.resDirectories }
                .mapNotNull {
                    File(it, "navigation").let { navFolder ->
                        if (navFolder.exists() && navFolder.isDirectory) navFolder else null
                    }
                }
                .flatMap { navFolder -> navFolder.listFiles().asIterable() }
                .filter { file -> file.isFile }
                .groupBy { file -> file.name }
                .map { entry -> entry.value.last() }
        }
        return project.files(fileProvider)
    }

    private fun BaseVariant.rFilePackage() = providerFactory.provider {
        val mainSourceSet = sourceSets.find { it.name == "main" }
        val sourceSet = mainSourceSet ?: sourceSets[0]
        val manifest = sourceSet.manifestFile
        val parsed = XmlSlurper(false, false).parse(manifest)
        parsed.getProperty("@package").toString()
    }
}

private fun forEachVariants(extension: BaseExtension, action: (BaseVariant) -> Unit) {
    when (extension) {
        is AppExtension -> extension.applicationVariants.all(action)
        is LibraryExtension -> extension.libraryVariants.all(action)
        else -> throw GradleException("deep links plugin must be used with android app or library")
    }
}
