@file:JvmMultifileClass

private const val kotlinVersion = "1.4.31"
private const val navigationVersion = "2.3.3"

object BuildPlugin {
    const val gradle = "com.android.tools.build:gradle:4.1.2"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    const val safeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion"
}

object Dependency {
    object AndroidX {
        const val fragment = "androidx.fragment:fragment-ktx:1.3.1"
        const val coreKtx = "androidx.core:core-ktx:1.3.2"
        const val appcompat = "androidx.appcompat:appcompat:1.2.0"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.0.4"

        object Navigation {
            const val fragment = "androidx.navigation:navigation-fragment-ktx:$navigationVersion"
            const val ui = "androidx.navigation:navigation-ui-ktx:$navigationVersion"
        }
    }
}
