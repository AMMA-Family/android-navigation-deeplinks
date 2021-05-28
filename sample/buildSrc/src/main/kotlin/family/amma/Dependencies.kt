@file:JvmMultifileClass

private const val kotlinVersion = "1.5.10"
private const val navigationVersion = "2.3.5"

object BuildPlugin {
    const val gradle = "com.android.tools.build:gradle:4.2.1"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val safeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion"
}

object Dependency {
    object AndroidX {
        const val fragment = "androidx.fragment:fragment-ktx:1.3.4"
        const val coreKtx = "androidx.core:core-ktx:1.5.0"
        const val appcompat = "androidx.appcompat:appcompat:1.3.0"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.4"

        object Navigation {
            const val fragment = "androidx.navigation:navigation-fragment-ktx:$navigationVersion"
            const val ui = "androidx.navigation:navigation-ui-ktx:$navigationVersion"
        }
    }
}
