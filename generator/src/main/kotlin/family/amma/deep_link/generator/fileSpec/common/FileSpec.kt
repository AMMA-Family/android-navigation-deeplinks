package family.amma.deep_link.generator.fileSpec.common

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import family.amma.deep_link.generator.fileSpec.DEEP_LINK_URI_PROP_NAME
import family.amma.deep_link.generator.fileSpec.generatedDeepLink

/** Common interface for all deep links. */
internal fun generatedDeepLinkFileSpec(): FileSpec =
    FileSpec
        .builder(generatedDeepLink.packageName, generatedDeepLink.simpleName)
        .addType(
            TypeSpec.interfaceBuilder(generatedDeepLink.simpleName)
                .addProperty(PropertySpec.builder(DEEP_LINK_URI_PROP_NAME, String::class).build())
                .build()
        )
        .indent(indent)
        .build()
