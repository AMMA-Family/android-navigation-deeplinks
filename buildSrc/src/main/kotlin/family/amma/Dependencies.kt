@file:JvmMultifileClass

private const val kotlinVersion = "1.6.10"

object BuildPlugin {
    const val gradle = "com.android.tools.build:gradle:7.0.4"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
}

object Dependency {
    const val xmlpull = "xmlpull:xmlpull:1.1.3.1"
    const val kotlinpoet = "com.squareup:kotlinpoet:1.10.2"

    object Kotlin {
        object X {
            object Serialization {
                const val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.1"
            }

            object Coroutines {
                const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0"
            }
        }
    }

    object Test {
        const val mockk = "io.mockk:mockk:1.12.1"
    }
}
