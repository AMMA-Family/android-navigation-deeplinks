package family.amma.deep_link.generator.entity

/**
 * Example: `package.R.id.resource_name`, where `package` is [packageName], `id` is [resType], `resource_name` is [name].
 */
internal data class ResReference(val packageName: String, val resType: String, val name: String) {
    // aapt allows and transforms dots and dashes into underscores
    val identifier = name.replace("[.\\-]".toRegex(), "_")

    /** merged [ResReference.packageName], [ResReference.resType] and [ResReference.identifier]. */
    val accessor = "$packageName.R.$resType.$identifier"

    fun isId() = resType == "id"

    override fun toString(): String = accessor
}
