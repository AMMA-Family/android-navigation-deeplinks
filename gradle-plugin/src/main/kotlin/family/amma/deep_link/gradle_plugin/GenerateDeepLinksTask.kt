package family.amma.deep_link.gradle_plugin

import family.amma.deep_link.generator.main.generateDeepLinks
import family.amma.deep_link.generator.main.GeneratorParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.io.File

@CacheableTask
abstract class GenerateDeepLinksTask : DefaultTask() {
    /** Package of R file. */
    @get:Input
    abstract val rFilePackage: Property<String>

    @get:Input
    abstract val applicationId: Property<String>

    @get:PathSensitive(PathSensitivity.ABSOLUTE)
    @get:Incremental
    @get:InputFiles
    abstract val navigationFiles: ConfigurableFileCollection

    @get:Input
    lateinit var generatorParams: GeneratorParams

    @get:OutputDirectory
    lateinit var buildDir: File

    @get:OutputDirectory
    lateinit var outputDir: File

    @get:OutputDirectory
    lateinit var pluginDir: File

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        if (inputChanges.isIncremental) {
            doIncrementalTaskAction(
                changedInputs = inputChanges.getFileChanges(navigationFiles).map { it.file to it.changeType }.toMap(),
            )
        } else {
            doFullTaskAction(navigationFiles.files)
        }

        copyToMainModule()
    }

    private fun doIncrementalTaskAction(changedInputs: Map<File, ChangeType>) {
        /*
        This option is one of the most optimal in terms of performance.
        It is definitely not worth using the option that involves the use of `.keys`.
        Going through the map is two times cheaper than creating new sets.
        */
        generateDeepLinks(navFiles = changedInputs.mapNotNull { (file, status) -> if (status != ChangeType.REMOVED) file else null })
        changedInputs
            .mapNotNull { (file, status) -> if (status == ChangeType.REMOVED) file else null }
            .forEach(File::delete)
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
        val dispatcher = Dispatchers.IO
        runBlocking(dispatcher) {
            generateDeepLinks(rFilePackage.get(), applicationId.orNull ?: "", navFiles, buildDir, generatorParams, dispatcher)
        }
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
        named("main") {
            java.srcDirs("src/main/kotlin")
        }
    }
    
    dependencies {
        implementation(kotlin("stdlib"))
    }
"""

private const val gitIgnoreFileText = """
    /build
    /src/main/kotlin/*
    !src/main/kotlin/deep_link
"""
