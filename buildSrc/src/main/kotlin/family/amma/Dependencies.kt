@file:JvmMultifileClass

private const val kotlinVersion = "1.5.10"

object BuildPlugin {
    const val gradle = "com.android.tools.build:gradle:4.2.1"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
}

object Dependency {
    const val xmlpull = "xmlpull:xmlpull:1.1.3.1"
    const val kotlinpoet = "com.squareup:kotlinpoet:1.8.0"

    object Kotlin {
        object X {
            object Serialization {
                const val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.1"
            }

            object Coroutines {
                const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0"
            }
        }
    }
}
