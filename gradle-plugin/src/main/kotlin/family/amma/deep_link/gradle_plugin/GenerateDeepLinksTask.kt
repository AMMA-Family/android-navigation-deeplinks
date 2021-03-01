package family.amma.deep_link.gradle_plugin

import family.amma.deep_link.generator.GeneratorParams
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

open class GenerateDeepLinksTask : DefaultTask() {
    /** Package of R file. */
    @get:Input
    lateinit var rFilePackage: String

    @get:Input
    lateinit var applicationId: String

    @get:Input
    lateinit var generatorParams: GeneratorParams

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
        val navFiles = navigationFiles.filterUnique()
        if (inputChanges.isIncremental) {
            @Suppress("UnstableApiUsage")
            val inputCollection = project.objects.fileCollection().also { it.setFrom(navFiles) }
            doIncrementalTaskAction(
                changedInputs = inputChanges.getFileChanges(inputCollection).map { it.file to it.changeType }.toMap(),
            )
        } else {
            doFullTaskAction(navFiles)
        }

        copyToMainModule()
    }

    private fun doIncrementalTaskAction(changedInputs: Map<File, ChangeType>) {
        val removedFiles = changedInputs.filter { (_, status) -> status == ChangeType.REMOVED }.keys
        generateDeepLinks(navFiles = changedInputs.minus(removedFiles).keys)
        removedFiles.forEach { it.delete() }
    }

    private fun doFullTaskAction(navFiles: Collection<File>) {
        if (buildDir.exists() && !buildDir.deleteRecursively()) {
            project.logger.warn("Failed to clear build directory for deep links")
        }
        if (!buildDir.exists() && !buildDir.mkdirs()) {
            throw GradleException("Failed to create directory for deep links")
        }
        if (outputDir.exists() && !outputDir.deleteRecursively()) {
            project.logger.warn("Failed to clear destination directory for deep links")
        }
        generateDeepLinks(navFiles)
    }

    private fun generateDeepLinks(navFiles: Collection<File>) {
        generateDeepLinks(rFilePackage, applicationId, navFiles.toList(), buildDir, generatorParams)
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
