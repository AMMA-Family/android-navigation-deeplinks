package family.amma.deep_link.generator.entity

import family.amma.deep_link.generator.main.NavType

internal object NavParserErrors {
    const val UNKNOWN_DESTINATION = "Destination with must contain 'id' attribute."

    fun invalidId(value: String) = "Failed to parse $value as id. 'id' must be in the format: @[+][package:]id/resource_name"

    fun sameSanitizedNameDeepLinks(sanitizedName: String, deepLinks: List<DeepLink>) =
        "Multiple same name deep links. The action ids: " +
            "[${deepLinks.joinToString(", ") { it.id.name }}] result in the " +
            "generator using the same name: '$sanitizedName'."

    fun invalidDefaultValueReference(value: String) = "Failed to parse defaultValue " +
        "'$value' as reference. Reference must be in format @[+][package:]res_type/resource_name"

    fun invalidDefaultValue(value: String, type: NavType) = "Failed to parse defaultValue " +
        "'$value' as $type"

    fun defaultNullButNotNullable(name: String?) = "android:defaultValue is @null, but '$name' " +
        "is not nullable. Add app:allowsNullable=\"true\" to the argument to make it nullable."

    fun typeIsNotNullable(typeName: String?) = "'$typeName' is a simple type " +
        "and cannot be nullable. Remove app:allowsNullable=\"true\" from the argument."

    fun deprecatedTypeAttrUsed(name: String) =
        "The 'type' attribute used by argument '$name' is deprecated. " +
            "Please change all instances of 'type' in navigation resources to 'argType'."
}