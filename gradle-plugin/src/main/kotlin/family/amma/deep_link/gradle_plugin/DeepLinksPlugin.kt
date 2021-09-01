package family.amma.deep_link.gradle_plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.DynamicFeatureVariant
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import groovy.xml.XmlSlurper
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

private const val PLUGIN_DIRNAME = "navigation-deep-links"
private const val GENERATED_PATH = "generated/source/$PLUGIN_DIRNAME"
private const val PLUGIN_PATH = "$PLUGIN_DIRNAME/src/main/kotlin"

@Suppress("unused")
class DeepLinksPlugin @Inject constructor(val providerFactory: ProviderFactory) : Plugin<Project> {
    override fun apply(project: Project) {
        val pluginExtension = project.extensions.create(DeepLinksPluginExtension.NAME, DeepLinksPluginExtension::class.java)

        val baseExtension = project.extensions.findByType(BaseExtension::class.java)
            ?: throw GradleException("deep links plugin must be used with android plugin")
        val outputDir = File(project.rootDir, PLUGIN_PATH)
        val pluginDir = File(project.rootDir, PLUGIN_DIRNAME)
        val buildDir = File(project.buildDir, GENERATED_PATH)

        val applicationIds = mutableMapOf<String, Provider<String>>()
        val variantExtension =
            project.extensions.findByType(AndroidComponentsExtension::class.java)
                ?: throw GradleException("deeplinks plugin must be used with android plugin")
        variantExtension.onVariants { variant ->
            when (variant) {
                is ApplicationVariant, is DynamicFeatureVariant ->
                    // Using reflection for AGP 7.0+ cause it can't resolve that
                    // DynamicFeatureVariant implements GeneratesApk so the `applicationId`
                    // property is actually available. Once we upgrade to 7.0 we will use
                    // getNamespace().
                    variant::class.java.getDeclaredMethod("getApplicationId").let { method ->
                        method.trySetAccessible()
                        applicationIds.getOrPut(variant.name) {
                            @kotlin.Suppress("UNCHECKED_CAST")
                            method.invoke(variant) as Provider<String>
                        }
                    }
            }
        }

        forEachVariants(baseExtension) { variant: BaseVariant ->
            val generateDeepLinksTask = project
                .tasks
                .create("generateDeepLinks${variant.name.replaceFirstChar(Char::uppercaseChar)}", GenerateDeepLinksTask::class.java) { task ->
                    task.rFilePackage.set(variant.rFilePackage())
                    task.applicationId.set(
                        // this will only put in the case where the extension is a Library module
                        // and should be superseded by `getNamespace()` in agp 7.0+
                        applicationIds.getOrPut(variant.name) {
                            providerFactory.provider { variant.applicationId }
                        }
                    )
                    task.generatorParams = pluginExtension.toGeneratorParams()
                    task.navigationFiles.setFrom(navigationFiles(variant))
                    task.outputDir = outputDir
                    task.pluginDir = pluginDir
                    task.buildDir = buildDir
                }
            variant.registerJavaGeneratingTask(generateDeepLinksTask, buildDir)
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
}

private fun forEachVariants(extension: BaseExtension, action: (BaseVariant) -> Unit) {
    when (extension) {
        is AppExtension -> extension.applicationVariants.all(action)
        is LibraryExtension -> extension.libraryVariants.all(action)
        else -> throw GradleException("deep links plugin must be used with android app or library")
    }
}
