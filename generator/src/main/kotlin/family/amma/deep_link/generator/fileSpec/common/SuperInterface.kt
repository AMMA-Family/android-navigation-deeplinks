package family.amma.deep_link.generator.fileSpec.common

import com.squareup.kotlinpoet.*
import family.amma.deep_link.generator.entity.*
import family.amma.deep_link.generator.entity.StringTemplatesFormat
import family.amma.deep_link.generator.entity.Uri
import family.amma.deep_link.generator.entity.header
import family.amma.deep_link.generator.ext.listProp
import family.amma.deep_link.generator.ext.overrideValProp
import family.amma.deep_link.generator.ext.toListCodeBlock
import family.amma.deep_link.generator.main.StringType
import family.amma.deep_link.generator.main.list
import family.amma.deep_link.generator.main.nullable

private const val commonPackageName = "deep_link"

sealed class SuperInterface<T>(val className: ClassName) {
    abstract fun fileSpec(): FileSpec
    abstract fun props(uri: Uri): T
}

/** Common interface for all additional information. */
object DeepLinkAdditionalInfo : SuperInterface<List<PropertySpec>>(
    className = ClassName(commonPackageName, "DeepLinkAdditionalInfo")
) {
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

    override fun props(uri: Uri): List<PropertySpec> {
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
object GeneratedDeepLink : SuperInterface<PropertySpec>(
    className = ClassName(commonPackageName, "GeneratedDeepLink")
) {
    private const val DEEP_LINK_URI_PROP_NAME = "uri"

    override fun fileSpec(): FileSpec =
        FileSpec
            .builder(className.packageName, className.simpleName)
            .addType(
                TypeSpec.interfaceBuilder(className.simpleName)
                    .addProperty(PropertySpec.builder(DEEP_LINK_URI_PROP_NAME, StringType.typeName).build())
                    .build()
            )
            .indent(indent)
            .build()

    override fun props(uri: Uri): PropertySpec =
        PropertySpec
            .builder(DEEP_LINK_URI_PROP_NAME, StringType.typeName)
            .initializer(StringTemplatesFormat.value, uri)
            .addModifiers(KModifier.OVERRIDE)
            .build()
}
