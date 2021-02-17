package family.amma.deep_link.generator.fileSpec.common

import family.amma.deep_link.generator.entity.DeepLink
import family.amma.deep_link.generator.ext.toCamelCase

internal val indent: String = List(size = 4) { ' ' }.joinToString(separator = "")

internal fun DeepLink.camelCaseName() = id.identifier.toCamelCase()
