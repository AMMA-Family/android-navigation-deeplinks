@file:JvmMultifileClass

private const val kotlinVersion = "1.4.31"

object BuildPlugin {
    const val gradle = "com.android.tools.build:gradle:4.1.2"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
}

object Dependency {
    const val xmlpull = "xmlpull:xmlpull:1.1.3.1"
    const val kotlinpoet = "com.squareup:kotlinpoet:1.7.2"

    object Kotlin {
        object X {
            object Serialization {
                const val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0"
            }

            object Coroutines {
                const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3"
            }
        }
    }
}
