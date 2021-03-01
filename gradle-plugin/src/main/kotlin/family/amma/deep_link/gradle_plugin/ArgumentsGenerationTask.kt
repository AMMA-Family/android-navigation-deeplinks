package family.amma.deep_link.gradle_plugin

import family.amma.deep_link.generator.ext.filterUnique
import family.amma.deep_link.generator.generateDeepLinks
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.InputChanges
import java.io.File

open class ArgumentsGenerationTask : DefaultTask() {
    /** Package of R file. */
    @get:Input
    lateinit var rFilePackage: String

    @get:Input
    lateinit var applicationId: String

    @get:InputFiles
    var navigationFiles: List<File> = emptyList()

    @get:OutputDirectory
    lateinit var buildDir: File

    @get:OutputDirectory
    lateinit var outputDir: File

    @get:OutputDirectory
    lateinit var pluginDir: File

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        val pluginExtension = project.extensions.findByName(DeepLinksPluginExtension.NAME) as DeepLinksPluginExtension
        // toSet() will not work here, because otherwise the order of files in the hierarchical version will always be different.
        val navFiles = navigationFiles.filterUnique()

        if (inputChanges.isIncremental && pluginExtension.isIncrementalEnabled) {
            @Suppress("UnstableApiUsage")
            val inputCollection = project.objects.fileCollection().also { it.setFrom(navFiles) }
            doIncrementalTaskAction(
                changedInputs = inputChanges.getFileChanges(inputCollection).map { it.file to it.changeType }.toMap(),
                pluginExtension
            )
        } else {
            doFullTaskAction(navFiles, pluginExtension)
        }

        copyToMainModule()
    }

    private fun doIncrementalTaskAction(changedInputs: Map<File, ChangeType>, pluginExtension: DeepLinksPluginExtension) {
        val removedFiles = changedInputs.filter { (_, status) -> status == ChangeType.REMOVED }.keys
        generateDeepLinks(navFiles = changedInputs.minus(removedFiles).keys, pluginExtension)
        removedFiles.forEach { it.delete() }
    }

    private fun doFullTaskAction(navFiles: Collection<File>, pluginExtension: DeepLinksPluginExtension) {
        if (buildDir.exists() && !buildDir.deleteRecursively()) {
            project.logger.warn("Failed to clear directory for deep links")
        }
        if (!buildDir.exists() && !buildDir.mkdirs()) {
            throw GradleException("Failed to create directory for deep links")
        }
        generateDeepLinks(navFiles, pluginExtension)
    }

    private fun generateDeepLinks(navFiles: Collection<File>, pluginExtension: DeepLinksPluginExtension) {
        generateDeepLinks(rFilePackage, applicationId, navFiles.toList(), buildDir, pluginExtension.toGeneratorParams())
    }

    private fun copyToMainModule() {
        buildDir.copyRecursively(outputDir, overwrite = true)
        buildDir.deleteRecursively()

        try {
            if (!pluginDir.exists()) pluginDir.mkdir()
            generateFiles(
                pluginDir = pluginDir,
                namesToContent = mapOf(
                    "build.gradle.kts" to gradleKtsFileText,
                    ".gitignore" to gitIgnoreFileText
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun generateFiles(pluginDir: File, namesToContent: Map<String, String>) {
    namesToContent.forEach { (name, content) ->
        val file = File(pluginDir, name)
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(content.trimIndent())
        }
    }
}

private const val gradleKtsFileText = """
    @file:Suppress("UNUSED_VARIABLE")
    
    plugins {
        id("java-library")
        id("kotlin")
    }
    
    sourceSets {
        val main by getting {
            java.srcDirs("src/main/kotlin")
        }
    }
    
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.31")
    }
"""

private const val gitIgnoreFileText = """
    /build
    /src/main/kotlin/*
    !src/main/kotlin/deep_link
"""
