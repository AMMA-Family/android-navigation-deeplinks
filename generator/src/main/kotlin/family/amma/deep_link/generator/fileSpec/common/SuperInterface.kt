package family.amma.deep_link.generator.fileSpec.common

import com.squareup.kotlinpoet.*
import family.amma.deep_link.generator.entity.Uri
import family.amma.deep_link.generator.entity.header
import family.amma.deep_link.generator.entity.pathSegments
import family.amma.deep_link.generator.entity.trimPartToParameters
import family.amma.deep_link.generator.ext.listProp
import family.amma.deep_link.generator.ext.overrideValProp
import family.amma.deep_link.generator.ext.toListCodeBlock
import family.amma.deep_link.generator.main.StringType
import family.amma.deep_link.generator.main.list
import family.amma.deep_link.generator.main.nullable

sealed class SuperInterface(val className: ClassName) {
    abstract fun fileSpec(): FileSpec
}

/** Common interface for all additional information. */
object DeepLinkAdditionalInfo : SuperInterface(className = ClassName("deep_link", "DeepLinkAdditionalInfo")) {

    private const val DEEP_LINK_PROTOCOL_PROP_NAME = "protocol"
    private const val DEEP_LINK_HOST_PROP_NAME = "host"
    private const val DEEP_LINK_PATH_SEGMENTS_PROP_NAME = "pathSegments"

    override fun fileSpec(): FileSpec {
        val nullableString = StringType.nullable
        return FileSpec
            .builder(className.packageName, className.simpleName)
            .addType(
                TypeSpec.interfaceBuilder(className.simpleName)
                    .addProperty(PropertySpec.builder(DEEP_LINK_PROTOCOL_PROP_NAME, nullableString).build())
                    .addProperty(PropertySpec.builder(DEEP_LINK_HOST_PROP_NAME, nullableString).build())
                    .addProperty(PropertySpec.builder(DEEP_LINK_PATH_SEGMENTS_PROP_NAME, StringType.list).build())
                    .build()
            )
            .indent(indent)
            .build()
    }

    /** @return list of props with additional info. */
    internal fun parsedUriProps(uri: Uri): List<PropertySpec> {
        val correctUri = uri.trimPartToParameters()
        val header = correctUri.header()
        return listOfNotNull(
            overrideValProp(DEEP_LINK_PROTOCOL_PROP_NAME, StringType, header.protocol()),
            overrideValProp(DEEP_LINK_HOST_PROP_NAME, StringType, header.host()),
            listProp(
                DEEP_LINK_PATH_SEGMENTS_PROP_NAME,
                StringType.list,
                toListCodeBlock(StringType, correctUri.pathSegments()), KModifier.OVERRIDE
            )
        )
    }
}

/** Common interface for all deep links. */
object GeneratedDeepLink : SuperInterface(className = ClassName("deep_link", "GeneratedDeepLink")) {

    private const val DEEP_LINK_URI_PROP_NAME = "uri"

    /** Common interface for all deep links. */
    override fun fileSpec(): FileSpec =
        FileSpec
            .builder(className.packageName, className.simpleName)
            .addType(
                TypeSpec.interfaceBuilder(className.simpleName)
                    .addProperty(PropertySpec.builder(DEEP_LINK_URI_PROP_NAME, String::class).build())
                    .build()
            )
            .indent(indent)
            .build()

}
