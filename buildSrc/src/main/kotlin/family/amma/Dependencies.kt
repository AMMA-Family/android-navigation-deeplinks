@file:JvmMultifileClass

private const val kotlinVersion = "1.5.21"

object BuildPlugin {
    const val gradle = "com.android.tools.build:gradle:7.0.1"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
}

object Dependency {
    const val xmlpull = "xmlpull:xmlpull:1.1.3.1"
    const val kotlinpoet = "com.squareup:kotlinpoet:1.9.0"

    object Kotlin {
        object X {
            object Serialization {
                const val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.2"
            }

            object Coroutines {
                const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1"
            }
        }
    }

    object Test {
        const val mockk = "io.mockk:mockk:1.12.0"
    }
}
