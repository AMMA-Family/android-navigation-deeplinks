# Android navigation deep links
This library provides functionality for generating deep links.

This is achieved using:
1) Goes through your navigation files
2) It pulls out information about deep links
3) Generates code
4) Puts it in a separate module `navigation_deep_links`.

## Introduction
#### Add the library to your `build.gradle.kts` file.
```kotlin
buildscript {
    dependencies {
        ...
        classpath("family.amma:deeplinks-gradle-plugin:1.0.2")
        // not necessary, but handy for working with taking arguments
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.5")
    }
}
```
#### Add to modules that have deep links
```kotlin
plugins {
    ...
    id("family.amma.deepLinks")
    id("androidx.navigation.safeargs.kotlin")
}
```
#### After generating the module add it to the `settings.gradle.kts`:
The module automatically generated after the build, but for manual launch `:generateDeepLinks`
```kotlin
include(
    ...,
    ":navigation-deep-links"
)
```
#### To use the generated code: `build.gradle.kts`
```kotlin
dependencies {
    implementation(project(":navigation-deep-links"))
}
```
#### Advanced options
If you want additional behavior you can config the flags. 
```kotlin
configure<family.amma.deep_link.gradle_plugin.DeepLinksPluginExtension> {
    generateByDestinations = true // Generation of a separate file with deep links for each destination. 
    generateUriHierarchy = false // Generating a hierarchy of deep links based on their url.
    generateAdditionalInfo = false // Generation of additional information for all types of generation: names, protocol, host and path segments.
}
```

# Usage example
### XML
We declare a deep link and (optional) arguments to it
```xml
<fragment
    android:id="@+id/secondFragment"
    android:name="family.amma.module_b.SecondFragment"
    android:label="SecondFragment"
    tools:layout="@layout/fragment_second">

    <argument
        android:name="isEditMode"
        app:argType="boolean" />

    <argument
        android:name="id"
        app:argType="integer"
        android:defaultValue="10" />

    <deepLink
        android:id="@+id/barFoo"
        app:uri="http://www.example.com/users/{id}?isEditMode={isEditMode}" />

</fragment>
```
### Generated code
```kotlin
public sealed class SecondFragmentDeepLink : GeneratedDeepLink {
    public data class BarFoo(
        public val id: Int = 10,
        public val isEditMode: Boolean
    ) : SecondFragmentDeepLink() {
        public override val uri: String =
                """http://www.example.com/users/${id}?isEditMode=${isEditMode}"""
    }
}
```
If we activate the `generateAdditionalInfo` flag:
```kotlin
public sealed class SecondFragmentDeepLink : GeneratedDeepLink {
    public data class BarFoo(
        public val id: Int = 10,
        public val isEditMode: Boolean
    ) : SecondFragmentDeepLink() {
        public override val uri: String =
                """http://www.example.com/users/${id}?isEditMode=${isEditMode}"""

        public companion object {
            public const val protocol: String = "http"

            public const val host: String = "www.example.com"

            public val pathSegments: List<String> = listOf("users")

        }
    }
}
```

If we activate the `generateAdditionalInfo` flag:
```kotlin
public sealed class ModuleB : GeneratedDeepLink {
    public sealed class Http : ModuleB() {
        public sealed class WwwExampleCom : Http() {
            public data class UsersDeepLink(
                public val id: Int = 10,
                public val isEditMode: Boolean
            ) : WwwExampleCom() {
                public override val uri: String =
                        """http://www.example.com/users/${id}?isEditMode=${isEditMode}"""
            }
        }
    }
}
```
By analogy above for the flag `generateAdditionalInfo`.
```kotlin
public sealed class ModuleB : GeneratedDeepLink {
    public sealed class Http : ModuleB() {
        public companion object {
            public const val name: String = "http"
        }

        public sealed class WwwExampleCom : Http() {
            public companion object {
                public const val name: String = "www.example.com"
            }

            public data class UsersDeepLink(
                public val id: Int = 10,
                public val isEditMode: Boolean
            ) : WwwExampleCom() {
                public override val uri: String =
                        """http://www.example.com/users/${id}?isEditMode=${isEditMode}"""

                public companion object {
                    public const val protocol: String = "http"

                    public const val host: String = "www.example.com"

                    public val pathSegments: List<String> = listOf("users")

                }
            }
        }
    }
}
```

## Using from fragments
[Example of the start fragment](https://github.com/AMMA-Family/android-navigation-deeplinks/blob/master/sample/module-a/src/main/kotlin/family/amma/module_a/FirstFragment.kt)
```kotlin
class FirstFragment : Fragment(R.layout.fragment_first) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button).setOnClickListener {
            // deep links args
            val id = Random.nextInt()
            val isEditMode = Random.nextBoolean()

            // the same deep links - by name and hierarchical
            val deepLink = if (Random.nextBoolean()) {
                SecondFragmentDeepLink.BarFoo(id, isEditMode)
            } else {
                ModuleB.Http.WwwExampleCom.UsersDeepLink(id, isEditMode)
            }
            findNavController().navigate(deepLink.toNavDeepLinkRequest())
        }
    }
}

private inline fun GeneratedDeepLink.toNavDeepLinkRequest(block: NavDeepLinkRequest.Builder.() -> Unit = {}) =
    NavDeepLinkRequest.Builder.fromUri(Uri.parse(this.uri)).also(block).build()
```
[Example of the finish fragment](https://github.com/AMMA-Family/android-navigation-deeplinks/blob/master/sample/module-b/src/main/kotlin/family/amma/module_b/SecondFragment.kt)
```kotlin
class SecondFragment : Fragment(R.layout.fragment_second) {
    private val args by navArgs<SecondFragmentArgs>()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.argsTextView).text = "Args: id = ${args.id}, isEditMode = ${args.isEditMode}"
    }
}
```
